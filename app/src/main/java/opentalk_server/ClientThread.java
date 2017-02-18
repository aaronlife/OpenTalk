package opentalk_server;

import java.io.BufferedReader;
import java.io.IOException;
import opentalk_server.ServerMain.OfflineNotification;

class ClientThread extends Thread
{
    ClientData cd;

    public ClientThread(ClientData cd) {this.cd = cd;}

    @Override
    public void run()
    {
        Globals.log("::", cd.getId(), cd.getName() + " joined. (Online: " +
                                         ServerMain.sc.size() + ")");

        BufferedReader br = cd.getReader();
        String data;
        boolean isTargetOnline;

        try
        {
            while((data = br.readLine()) != null)
            {
                Globals.log("<=",  cd.getId(), data);

                OTProtocol.Command cmd =
                                OTProtocol.parseCmd(data, cd.getAesKeySet());

                if(cmd == null)
                {
                    Globals.log("::", cd.getId(), "Invalid format.");
                    continue;
                }

                String responProtocol = OTProtocol.NONE;

                switch(cmd.name)
                {
                case OTProtocol.C2S_INVITATION:
                    responProtocol = OTProtocol.S2C_INVITATION;
                    break;

                case OTProtocol.C2S_ACCEPT_INVITATION:
                    responProtocol = OTProtocol.S2C_ADD_FRIEND;
                    break;

                case OTProtocol.C2S_MESSAGE:
                    responProtocol = OTProtocol.S2C_MESSAGE;
                    break;

                case OTProtocol.C2S_STICKER:
                    responProtocol = OTProtocol.S2C_STICKER;
                    break;
                }

                switch(cmd.name)
                {
                case OTProtocol.C2S_OFFLINE_NOTI:
                    for(OfflineNotification n : ServerMain.on)
                    {
                        if(n.toUid.equals(cd.getId()))
                        {
                            Globals.log("=>", cd.getId(),
                                           "Offline Notification.");
                            cd.send(OTProtocol.makeCmd(n.protocol, n.fromUid,
                                                       n.content, n.datetime,
                                                       cd.getAesKeySet()));
                            ServerMain.on.remove(n);
                        }
                    }
                    break;

                case OTProtocol.C2S_REGISTER:
                    String uid =
                       String.format("%08d", (int)(Math.random() * 100000000));
                    cd.send(OTProtocol.makeCmd(
                            OTProtocol.S2C_REGISTER, uid,
                            OTProtocol.OK, cmd.datetime,
                            cd.getAesKeySet()));
                    cd.setId(uid);
                    cd.setName(cmd.data2);
                    break;

                case OTProtocol.C2S_FIND:
                    boolean found = false;

                    for(ClientData tmp : ServerMain.sc)
                    {
                        if(tmp.getId().equals(cmd.data1))
                        {
                            cd.send(OTProtocol.makeCmd(OTProtocol.S2C_FIND,
                                                       tmp.getId(),
                                                       tmp.getName(),
                                                       cmd.datetime,
                                                       cd.getAesKeySet()));
                            found = true;
                            break;
                        }
                    }

                    if(found == false)
                        cd.send(OTProtocol.makeCmd(
                          OTProtocol.S2C_FIND, cmd.data1,
                          OTProtocol.NOT_FOUND, cmd.datetime,
                          cd.getAesKeySet()));

                    break;

                case OTProtocol.C2S_INVITATION:
                case OTProtocol.C2S_ACCEPT_INVITATION:
                    isTargetOnline = false;

                    for(ClientData tc : ServerMain.sc)
                    {
                        if(tc.getId().equals(cmd.data1))
                        {
                            tc.send(OTProtocol.makeCmd(
                                                responProtocol, cd.getId(),
                                                cd.getName(), cmd.datetime,
                                                cd.getAesKeySet()));
                            isTargetOnline = true;
                            break;
                        }
                    }

                    if(!isTargetOnline)
                        ServerMain.on.add(new OfflineNotification(
                                cd.getId(), cmd.data1, responProtocol,
                                cd.getName(), cmd.datetime));

                    break;

                case OTProtocol.C2S_MESSAGE:
                case OTProtocol.C2S_STICKER:
                    isTargetOnline = false;

                    for(ClientData tc : ServerMain.sc)
                    {
                        if(tc.getId().equals(cmd.data1))
                        {
                            tc.send(OTProtocol.makeCmd(
                                                responProtocol, cd.getId(),
                                                cmd.data2, cmd.datetime,
                                                cd.getAesKeySet()));
                            isTargetOnline = true;
                            break;
                        }
                    }

                    if(!isTargetOnline)
                        ServerMain.on.add(new OfflineNotification(
                                cd.getId(), cmd.data1, responProtocol,
                                cmd.data2, cmd.datetime));

                    break;
                }
            }
        }
        catch(IOException e)
        {
            Globals.log("::", cd.getId(), "Exception：" + e.getMessage());
        }

        cd.close();

        ServerMain.sc.remove(cd);

        Globals.log("::", cd.getId(), "Disconnected.");
    }
}