package com.aaronlife.opentalk.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.aaronlife.opentalk.R;
import com.aaronlife.opentalk.client.SocketClientService;

import opentalk_server.OTProtocol;
import opentalk_server.ServerMain;

public class MainActivity extends AppCompatActivity
{
    public static final String PREF_NAME = "UserData";
    public static final String KEY_UID = "UID";
    public static final String KEY_NAME = "NAME";

    public static String uid = "NONE";
    public static String name = "NONE";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        //new ServerMain().start();

        new Thread()
        {
            @Override
            public void run()
            {
                // 檢查是否註冊過
                // 跨activity分享資料不能使用getPreferences
                uid = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .getString("USER_ID", OTProtocol.NONE);
                name = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .getString("USER_NAME", OTProtocol.NONE);

                Log.d("aarontest", "User ID=" + uid);
                Log.d("aarontest", "User Name=" + name);

                Intent itService =
                        new Intent(SocketClientService.ACTION_CONNECT);
                itService.setClass(MainActivity.this, SocketClientService.class);
                itService.putExtra("UID", uid);
                itService.putExtra("NAME", name);
                startService(itService);

                try
                {
                    Thread.sleep(2000);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }

                Intent it = new Intent();

                if(uid.equals(OTProtocol.NONE))
                {
                    it.setClass(MainActivity.this, RegisterActivity.class);
                }
                else
                {
                    it.setClass(MainActivity.this, ContactListActivity.class);
                }

                startActivity(it);

                super.run();

                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        finish();
                    }
                }, 2000);

            }
        }.start();
    }

    Handler handler = new Handler();
}