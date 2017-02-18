package opentalk_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.spec.SecretKeySpec;

public class ServerMain extends Thread
{
    public static CopyOnWriteArrayList<ClientData> sc =
                                                new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<OfflineNotification> on =
                                                new CopyOnWriteArrayList<>();

    public static void main(String[] argc)
    {
        System.out.println("=========================================");
        System.out.println("= OpenTalk Server - Version: " +
                                                    Globals.SERVER_VERSION);
        System.out.println("=                   Release: 2016-10-01 =");
        System.out.println("=========================================");

        new ServerMain().start();
    }

    public static class OfflineNotification
    {
        public OfflineNotification(String fromUid, String toUid,
                                   String protocol, String content, String dt)
        {
            this.fromUid = fromUid;
            this.toUid = toUid;
            this.protocol = protocol;
            this.content = content;
            this.datetime = dt;

            Globals.log("::", toUid, "Offline. Create: " + fromUid + ", " + 
                        protocol + ", " + content + ", " + dt);
        }

        public String fromUid;
        public String toUid;
        public String protocol;
        public String content;
        public String datetime;
    }

    protected class HeartBeatTask extends TimerTask
    {
        @Override
        public void run()
        {
            for(ClientData cd : sc)
            {
                cd.send(OTProtocol.makeCmd(OTProtocol.KEEP_ALIVE,
                                           "-", "-",
                                           OTProtocol.nowStr(),
                                           cd.getAesKeySet()));
            }
        }
    }

    @Override
    public void run()
    {
        super.run();

        Security.RSAKeyPair rsaKeyPair = Security.generateRSAKeyPair();

        ServerSocket ss = null;

        Timer timer = new Timer();

        try
        {
            ss = new ServerSocket(Globals.SERVERPORT);
            Globals.err("OpenTalk Server Started at " + Globals.SERVERPORT);
        }
        catch (IOException e)
        {
            Globals.err("OpenTalk Server Exception: " + e.getMessage());
            return;
        }

        timer.schedule(new HeartBeatTask(), 0, OTProtocol.HEART_BEAT);

        while(true)
        {
            Globals.log("::", "--------", "Waiting for connection.....");
            Socket c;
            String data;
            BufferedReader r;
            PrintWriter w;

            Security.AESKeySet aesKeySet;
            OTProtocol.Command firstCmd;

            try
            {
                c = ss.accept();
            }
            catch(IOException e)
            {
                Globals.err("ServerSocket Exception: " + e.getMessage());
                break;
            }

            try
            {
                c.setSoTimeout(OTProtocol.READ_TIMEOUT);
                c.setTcpNoDelay(true);

                Globals.log("::", "--------", "New connection from " +
                                                c.getRemoteSocketAddress());

                r = new BufferedReader(
                                new InputStreamReader(c.getInputStream()));
                w = new PrintWriter(c.getOutputStream());

                // 1. 傳送版本號碼
                w.println(Globals.SERVER_VERSION);
                w.flush();

                // 2. 等待回應
                String rsp = r.readLine();
                if(!rsp.equals(OTProtocol.OK))
                {
                    Globals.log("::", "--------", "Client doesn't response OK");
                    c.close();
                    continue;
                }

                // 3. 將公鑰傳送給Client
                w.println(Globals.bytesToHexString(
                                        rsaKeyPair.publicKey.getEncoded()));
                w.flush();
                Globals.log("::", "--------", "Send PublicKey: " +
                  Globals.bytesToHexString(rsaKeyPair.publicKey.getEncoded()));

                // 4. 接收金鑰
                String aesKey = r.readLine();
                String iv = r.readLine();

                if(aesKey != null && iv != null)
                {
                    String deAesKey =
                            Security.decryptRSA(rsaKeyPair.privateKey, aesKey);
                    String deIv =
                            Security.decryptRSA(rsaKeyPair.privateKey, iv);

                    Globals.log("::", "--------", "Received AES: " + deAesKey);
                    Globals.log("::", "--------", "Received IV: " + deIv);

                    byte[] deAesKeyArray = Globals.hexStringToBytes(deAesKey);
                    aesKeySet = new Security.AESKeySet();
                    aesKeySet.secretKey =
                            new SecretKeySpec(deAesKeyArray, 0,
                                              deAesKeyArray.length, "AES");
                    aesKeySet.iv = Globals.hexStringToBytes(deIv);
                }
                else
                {
                    Globals.log("::", "--------", "Cannot received client key");
                    c.close();
                    continue;
                }

                // 5. 接收Client第一條LOGIN訊息
                data = r.readLine();

                Globals.log("<=", "--------", data);

                if(data == null)
                {
                    Globals.log("::", "--------", "Remove disconnected.");
                    continue;
                }

                firstCmd = OTProtocol.parseCmd(data, aesKeySet);

                if(firstCmd == null ||
                   !firstCmd.name.equals(OTProtocol.C2S_LOGIN))
                {
                    Globals.log("::", "--------", "Wrong protocol, force kill");
                    c.close();
                    continue;
                }
            }
            catch(Exception e)
            {
                Globals.log("::", "--------", "Exception: " + e.getMessage());
                continue;
            }

            ClientData cd = new ClientData(firstCmd.data1,
                                           firstCmd.data2,
                                           c, r, w, aesKeySet);

            for(ClientData tc : sc)
            {
                if(!cd.getId().equals(OTProtocol.NONE) &&
                   tc.getId().equals(cd.getId()))
                {
                    Globals.log("::", cd.getId(), "Remove pervious dead.");
                    tc.close();
                    sc.remove(tc);
                }
            }

            sc.add(cd);

            // 6. 回應Client一切就緒
            cd.send(OTProtocol.makeCmd(
                    OTProtocol.S2C_WELCOME, "-", "OK", OTProtocol.nowStr(),
                    aesKeySet));

            new ClientThread(cd).start();
        }

        timer.cancel();
        timer.purge();

        Globals.err("OpenTalk Server stopped.");
    }
}