package com.aaronlife.opentalk.client;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.aaronlife.opentalk.activity.MainActivity;

import opentalk_server.Globals;
import opentalk_server.OTProtocol;
import opentalk_server.Security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


public class SocketClient extends Thread
{
    class WrongClientVersionException extends IOException
    {

    }

    class HeartBeat extends Thread
    {
        @Override
        public void run()
        {
            while(isConnecting())
            {
                // 送出心跳
                send(OTProtocol.makeCmd(OTProtocol.KEEP_ALIVE,
                        "0", "0",
                        OTProtocol.nowStr(),
                        getAesKeySet()));

                try
                {
                    Thread.sleep(OTProtocol.HEART_BEAT);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    Context context;

    Socket s = null;          // 客戶端網路連線Socket
    PrintWriter out = null;   // 客戶端輸出資料用物件
    BufferedReader br = null; // 客戶端讀取資料用物件

    String ip;                // 伺服器位址
    int port;                 // 伺服器通訊埠
    String name;              // 聊天時要出現的名字
    String uid;

    HeartBeat heartBeat;

    Security.AESKeySet aesKeySet = null;

    public SocketClient(Context context, String ip, int port, String uid, String name)
    {
        this.context = context;
        this.ip = ip;
        this.port = port;

        this.uid =  uid;
        this.name = name;
    }

    public boolean isConnecting()
    {
        return s != null && s.isConnected() && !s.isClosed();
    }

    // 連接伺服器與握手
    protected boolean connect() throws WrongClientVersionException
    {
        try
        {
            // 建立Socket物件，並傳入要連線的伺服器ＩＰ和通訊埠
            s = new Socket(ip, port);
            s.setSoTimeout(OTProtocol.READ_TIMEOUT);
            s.setTcpNoDelay(true);

            // 向伺服器讀取訊息的物件
            InputStreamReader isr = null;
            isr = new InputStreamReader(s.getInputStream());
            br = new BufferedReader(isr);

            // 建立要用來輸入訊息到伺服器的物件
            OutputStreamWriter osr = new OutputStreamWriter(s.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osr);
            out = new PrintWriter(bw);

            PublicKey publicKey = null;

            // 1. 接收Server版本
            String serverVer = br.readLine();
            Log.d(Globals.LOGTAG, "Server version: " + serverVer);

            int serverVerInt = 0;
            if(serverVer != null)
            {
                try
                {
                    serverVerInt = Integer.parseInt(serverVer);
                }
                catch(NumberFormatException e)
                {
                    throw new WrongClientVersionException();
                }
            }
            else
                throw new WrongClientVersionException();

            // 2. 回應
            PackageInfo pInfo = null;
            try
            {
                pInfo = context.getPackageManager()
                               .getPackageInfo(context.getPackageName(), 0);
            }
            catch(PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }

            if(pInfo.versionCode < serverVerInt)
                throw new WrongClientVersionException();

            out.println(OTProtocol.OK);
            out.flush();

            // 3. 讀取Server Public Key
            String publicKeyStr = null;
            if((publicKeyStr = br.readLine()) != null)
            {
                Log.d(Globals.LOGTAG, "Received Public Key: " + publicKeyStr);

                byte[] publicKeyBytes = Globals.hexStringToBytes(publicKeyStr);
                try
                {
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    EncodedKeySpec publicKeySpec =
                                    new X509EncodedKeySpec(publicKeyBytes);
                    publicKey = keyFactory.generatePublic(publicKeySpec);

                }
                catch(NoSuchAlgorithmException | InvalidKeySpecException e)
                {
                    e.printStackTrace();
                }

            }
            else return false;

            // 4. 建立送出對稱金鑰
            aesKeySet = Security.generateAESKeySet();

            String aesKeyStr =
                    Globals.bytesToHexString(aesKeySet.secretKey.getEncoded());
            String encodedAESKey = Security.encryptRSA(publicKey, aesKeyStr);
            send(encodedAESKey);

            String ivStr = Globals.bytesToHexString(aesKeySet.iv);
            String encodedIV = Security.encryptRSA(publicKey, ivStr);
            send(encodedIV);

            // 5. 如果到這裡沒產生例外，表示連線成功，通知伺服器登入的使用者名字
            String data = OTProtocol.makeCmd(OTProtocol.C2S_LOGIN,
                                             MainActivity.uid,
                                             MainActivity.name,
                                             OTProtocol.nowStr(),
                                             aesKeySet);
            send(data);

            Log.d("aarontest", "Connect OK");
        }
        catch(WrongClientVersionException e)
        {
            throw new WrongClientVersionException();
        }
        catch (IOException e)
        {
            Log.d("aarontest", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void run()
    {
        super.run();

        while(true)
        {
            try
            {
                if(!connect())
                {
                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    continue;
                }
            }
            catch(WrongClientVersionException e)
            {
                context.sendBroadcast(
                        new Intent(SocketClientService.WRONG_CLIENT_VERSION));
                break;
            }

            String data = "";

            try
            {
                while((data = br.readLine()) != null)
                {
                    Log.d("aarontest", "ClientThread received " + data);

                    OTProtocol.Command cmd = OTProtocol.parseCmd(data, aesKeySet);
                    
                    Intent it;

                    switch(cmd.name)
                    {
                    // 6. 握手完成（開始心跳發送）
                    case OTProtocol.S2C_WELCOME:
                        it = new Intent(SocketClientService.RSP_WELCOME);
                        it.putExtra("DATETIME", cmd.datetime);
                        context.sendBroadcast(it);
                        heartBeat = new HeartBeat();
                        heartBeat.start();
                        Log.d("aarontest", "Handshake completed.");
                        break;

                    case OTProtocol.S2C_REGISTER:
                        if(cmd.data2.equals(OTProtocol.OK))
                        {
                            name = MainActivity.name;
                            uid = MainActivity.uid;
                            it = new Intent(SocketClientService
                                                       .RSP_REGISTER_RESULT);
                            it.putExtra("DATETIME", cmd.datetime);
                            it.putExtra("RESULT", SocketClientService
                                                       .RESISTER_OK);
                            it.putExtra("UID", cmd.data1);
                            context.sendBroadcast(it);
                        }
                        break;

                    case OTProtocol.S2C_FIND:
                        it = new Intent(SocketClientService.RSP_FOUND_ITEM);
                        it.putExtra("DATETIME", cmd.datetime);
                        it.putExtra("UID", cmd.data1);
                        it.putExtra("NAME", cmd.data2);
                        context.sendBroadcast(it);
                        break;

                    case OTProtocol.S2C_INVITATION:
                        it = new Intent(SocketClientService.RSP_NEW_INVITATION);
                        it.putExtra("DATETIME", cmd.datetime);
                        it.putExtra("UID", cmd.data1);
                        it.putExtra("NAME", cmd.data2);
                        context.sendBroadcast(it); // app如果是關閉會收不到
                        break;

                    case OTProtocol.S2C_ADD_FRIEND:
                        it = new Intent(SocketClientService.RSP_ADD_FRIEND);
                        it.putExtra("DATETIME", cmd.datetime);
                        it.putExtra("UID", cmd.data1);
                        it.putExtra("NAME", cmd.data2);
                        context.sendBroadcast(it);
                        break;

                    case OTProtocol.S2C_MESSAGE:
                        it = new Intent(SocketClientService
                                                      .RSP_RECEIVED_MESSAGE);
                        it.putExtra("DATETIME", cmd.datetime);
                        it.putExtra("UID", cmd.data1);
                        it.putExtra("MESSAGE", cmd.data2);
                        context.sendBroadcast(it);
                        break;

                    case OTProtocol.S2C_STICKER:
                        it = new Intent(SocketClientService
                                                      .RSP_RECEIVED_STICKER);
                        it.putExtra("DATETIME", cmd.datetime);
                        it.putExtra("UID", cmd.data1);
                        it.putExtra("STICKER_ID", cmd.data2);
                        context.sendBroadcast(it);
                        break;
                    }

                }
            }
            catch(IOException e)
            {
                Log.d("aarontest", "SocketClient Exception: " + e.getMessage());
            }

            Log.d("aarontest", "Disconnected from server");

            try
            {
                s.close();
                s = null;
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    // 傳送聊天訊息給伺服器
    public synchronized void send(String str)
    {
        while(!isConnecting())
        {
            try
            {
                Thread.sleep(3000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        out.println(str);
        out.flush();
    }

    public Security.AESKeySet getAesKeySet()
    {
        return aesKeySet;
    }
}