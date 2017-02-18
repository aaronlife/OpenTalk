package com.aaronlife.opentalk.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aaronlife.opentalk.R;
import com.aaronlife.opentalk.client.SocketClientService;

public class RegisterActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        IntentFilter intentFilter =
                new IntentFilter(SocketClientService.RSP_REGISTER_RESULT);
        intentFilter.addAction(SocketClientService.RSP_NO_CONNECTION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(receiver);

        super.onDestroy();
    }

    public void onRegister(View v)
    {
        TextView txtName = (TextView)findViewById(R.id.txtName);

        if(txtName.length() <= 0)
        {
            Toast.makeText(this, "暱稱不能為空", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Intent it = new Intent(SocketClientService.ACTION_REGISTER);
            it.putExtra("NAME", txtName.getText().toString());
            it.setClass(this, SocketClientService.class);
            startService(it);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            switch(intent.getAction())
            {
            case SocketClientService.RSP_REGISTER_RESULT:
                TextView txtName = (TextView)findViewById(R.id.txtName);

                MainActivity.name = txtName.getText().toString();
                getSharedPreferences(MainActivity.PREF_NAME, MODE_PRIVATE)
                        .edit()
                        .putString("USER_NAME", MainActivity.name).commit();

                MainActivity.uid = intent.getStringExtra("UID");
                getSharedPreferences(MainActivity.PREF_NAME, MODE_PRIVATE)
                        .edit()
                        .putString("USER_ID", MainActivity.uid).commit();

                Intent it = new Intent();
                it.setClass(RegisterActivity.this, ContactListActivity.class);
                startActivity(it);
                finish();
                Toast.makeText(RegisterActivity.this, "註冊成功",
                               Toast.LENGTH_LONG).show();
                break;

            case SocketClientService.RSP_NO_CONNECTION:
                Toast.makeText(RegisterActivity.this, "網路連線問題",
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
}