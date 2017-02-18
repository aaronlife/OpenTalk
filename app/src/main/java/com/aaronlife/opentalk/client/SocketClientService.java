package com.aaronlife.opentalk.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import opentalk_server.Globals;
import opentalk_server.OTProtocol;

public class SocketClientService extends Service
{
    public static final String WRONG_CLIENT_VERSION =
                                            "com.aaron.WRONG_CLIENT_VERSION";
    public static final String ACTION_CONNECT =
                                              "com.aaronlife.ACTION_CONNECT";
    public static final String ACTION_REGISTER =
                                             "com.aaronlife.ACTION_REGISTER";
    public static final String ACTION_OFFLINE_NOTI =
                                         "com.aaronlife.ACTION_OFFLINE_NOTI";
    public static final String ACTION_FIND = "com.aaronlife.ACTION_FIND";
    public static final String ACTION_SEND_INVITATION =
                                      "com.aaronlife.ACTION_SEND_INVITATION";
    public static final String ACTION_ACCEPT_INVITATION =
                                    "com.aaronlife.ACTION_ACCEPT_INVITATION";
    public static final String ACTION_DELETE_INVITATION =
                                    "com.aaronlife.ACTION_DELETE_INVITATION";
    public static final String ACTION_SEND_MESSAGE =
                                         "com.aaronlife.ACTION_SEND_MESSAGE";
    public static final String ACTION_SEND_STICKER =
                                         "com.aaronlife.ACTION_SEND_STICKER";

    public static final String RSP_WELCOME = "com.aaronlife.RSP_WELCOME";
    public static final String RSP_REGISTER_RESULT =
                                         "com.aaronlife.RSP_REGISTER_RESULT";
    public static final String RSP_FOUND_ITEM =
                                              "com.aaronlife.RSP_FOUND_ITEM";
    public static final String RSP_NEW_INVITATION =
                                          "com.aaronlife.RSP_NEW_INVITATION";
    public static final String RSP_ADD_FRIEND =
                                              "com.aaronlife.RSP_ADD_FRIEND";
    public static final String RSP_RECEIVED_MESSAGE =
                                        "com.aaronlife.RSP_RECEIVED_MESSAGE";
    public static final String RSP_RECEIVED_SELF_MESSAGE =
                                   "com.aaronlife.RSP_RECEIVED_SELF_MESSAGE";
    public static final String RSP_RECEIVED_STICKER =
                                        "com.aaronlife.RSP_RECEIVED_STICKER";
    public static final String RSP_RECEIVED_SELF_STICKER =
                                   "com.aaronlife.RSP_RECEIVED_SELF_STICKER";
    public static final String RSP_NO_CONNECTION =
                                           "com.aaronlife.RSP_NO_CONNECTION";

    public static final int RESISTER_OK = 0;
    public static final int RESISTER_EXISTING = 1;

    public SocketClientService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    SocketClient rc;

    @Override
    public void onCreate()
    {
        //new ServerMain().start();

        try
        {
            Thread.sleep(1000);
        } catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("aarontest", "onStartCommand:" + intent.getAction());

        String cmd;
        Intent it;

        intent.putExtra("DATETIME", OTProtocol.nowStr());

        switch(intent.getAction())
        {
        case ACTION_CONNECT:
            if(rc == null || !rc.isConnecting())
            {
                rc = new SocketClient(this,
                                      Globals.SERVERIP,
                                      Globals.SERVERPORT,
                                      intent.getStringExtra("UID"),
                                      intent.getStringExtra("NAME"));
                rc.start();
            }
            break;

        case ACTION_REGISTER:
            if(!connectionTest()) break;

            cmd = OTProtocol.makeCmd(OTProtocol.C2S_REGISTER,
                                     "0",
                                     intent.getStringExtra("NAME"),
                                     intent.getStringExtra("DATETIME"),
                                     rc.getAesKeySet());
            rc.send(cmd);
            break;

        case ACTION_FIND:
            if(!connectionTest()) break;

            cmd = OTProtocol.makeCmd(OTProtocol.C2S_FIND,
                                     intent.getStringExtra("UID"),
                                     "0",
                                     intent.getStringExtra("DATETIME"),
                                     rc.getAesKeySet());
            rc.send(cmd);
            break;

        case ACTION_SEND_INVITATION:
            if(!connectionTest()) break;

            cmd = OTProtocol.makeCmd(OTProtocol.C2S_INVITATION,
                                     intent.getStringExtra("UID"),
                                     "0",
                                     intent.getStringExtra("DATETIME"),
                                     rc.getAesKeySet());
            rc.send(cmd);
            break;

        case ACTION_ACCEPT_INVITATION:
            if(!connectionTest()) break;

            cmd = OTProtocol.makeCmd(OTProtocol.C2S_ACCEPT_INVITATION,
                                     intent.getStringExtra("UID"),
                                     "0",
                                     intent.getStringExtra("DATETIME"),
                                     rc.getAesKeySet());
            rc.send(cmd);
            break;

        case ACTION_OFFLINE_NOTI:
            if(!connectionTest()) break;

            cmd = OTProtocol.makeCmd(OTProtocol.C2S_OFFLINE_NOTI,
                                     "0", "0", "0", rc.getAesKeySet());
            rc.send(cmd);
            break;

        case ACTION_SEND_MESSAGE:
            if(!connectionTest()) break;

            it = new Intent(SocketClientService.RSP_RECEIVED_SELF_MESSAGE);
            it.putExtra("UID", intent.getStringExtra("UID"));
            it.putExtra("MESSAGE", intent.getStringExtra("MESSAGE"));
            it.putExtra("DATETIME", intent.getStringExtra("DATETIME"));
            sendBroadcast(it);

            cmd = OTProtocol.makeCmd(OTProtocol.C2S_MESSAGE,
                                     intent.getStringExtra("UID"),
                                     intent.getStringExtra("MESSAGE"),
                                     intent.getStringExtra("DATETIME"),
                                     rc.getAesKeySet());
            rc.send(cmd);
            break;

        case ACTION_SEND_STICKER:
            if(!connectionTest()) break;

            it = new Intent(SocketClientService.RSP_RECEIVED_SELF_STICKER);
            it.putExtra("UID", intent.getStringExtra("UID"));
            it.putExtra("STICKER_ID", intent.getStringExtra("STICKER_ID"));
            it.putExtra("DATETIME", intent.getStringExtra("DATETIME"));
            sendBroadcast(it);

            cmd = OTProtocol.makeCmd(OTProtocol.C2S_STICKER,
                                     intent.getStringExtra("UID"),
                                     intent.getStringExtra("STICKER_ID"),
                                     intent.getStringExtra("DATETIME"),
                                     rc.getAesKeySet());
            rc.send(cmd);
            break;
        }

        return Service.START_NOT_STICKY;
    }

    protected boolean connectionTest()
    {
        if(rc != null && !rc.isConnecting())
        {
            sendBroadcast(new Intent(SocketClientService.RSP_NO_CONNECTION));

            return false;
        }

        return true;
    }
}