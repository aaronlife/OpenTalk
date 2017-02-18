package com.aaronlife.opentalk.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aaronlife.opentalk.R;
import com.aaronlife.opentalk.adapter.StickerListAdapter;
import com.aaronlife.opentalk.client.ClientDatabaseHelper;
import com.aaronlife.opentalk.client.SocketClientService;

import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity
{
    LinearLayout ll;
    ScrollView scrollChat;

    TextView txtChatInput;
    String toUuid;

    StickerListAdapter stickerListAdapter;
    ArrayList<ClientDatabaseHelper.ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar()
                .setTitle("與 " + getIntent().getStringExtra("NAME") + " 聊天");

        toUuid = getIntent().getStringExtra("UID");

        // 這是副本
        chatMessages = (ArrayList<ClientDatabaseHelper.ChatMessage>)getIntent()
                        .getSerializableExtra("CHAT_MESSAGE");

        ll = (LinearLayout)findViewById(R.id.chatHistory);
        scrollChat = (ScrollView)findViewById(R.id.scrollChat);
        txtChatInput = (TextView)findViewById(R.id.txtChatInput);

        ListView listSticker = (ListView)findViewById(R.id.listSticker);
        stickerListAdapter = new StickerListAdapter(this);
        listSticker.setAdapter(stickerListAdapter);

        for(ClientDatabaseHelper.ChatMessage cm : chatMessages)
        {
            if(cm.type == ClientDatabaseHelper.ChatMessage.TYPE_MESSAGE)
            {
                View vv = getLayoutInflater()
                        .inflate(R.layout.list_chat_content, null);
                ((TextView)vv.findViewById(R.id.txtId))
                        .setText(getContactName(cm.fromUid) + " 說：");
                ((TextView) vv.findViewById(R.id.txtChat))
                        .setText(cm.content);
                ((TextView) vv.findViewById(R.id.txtTime))
                        .setText(cm.chatTime);
                ll.addView(vv);
            }
            else
            {
                View vv = getLayoutInflater()
                        .inflate(R.layout.list_chat_sticker, null);
                ((TextView)vv.findViewById(R.id.txtId))
                        .setText(getContactName(cm.fromUid) + " 說：");
                ((ImageView) vv.findViewById(R.id.imgSticker))
                        .setImageResource(Integer.parseInt(cm.content));
                ((TextView) vv.findViewById(R.id.txtTime))
                        .setText(cm.chatTime);
                ll.addView(vv);
            }
        }

        scrollChat.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollChat.fullScroll(View.FOCUS_DOWN);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketClientService.RSP_WELCOME);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_MESSAGE);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_SELF_MESSAGE);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_STICKER);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_SELF_STICKER);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void onStickerPanel(View v)
    {
        LinearLayout stickerPanel =
                (LinearLayout)findViewById(R.id.stickerPanel);

        if(stickerPanel.getVisibility() == View.VISIBLE)
            stickerPanel.setVisibility(View.GONE);
        else

            stickerPanel.setVisibility(View.VISIBLE);
    }

    public void onCloseSticker(View v)
    {
        LinearLayout stickerPanel =
                (LinearLayout)findViewById(R.id.stickerPanel);

        if(stickerPanel.getVisibility() == View.VISIBLE)
            stickerPanel.setVisibility(View.GONE);
    }

    public void onSendSticker(View v)
    {
        LinearLayout stickerPanel =
                (LinearLayout)findViewById(R.id.stickerPanel);
        stickerPanel.setVisibility(View.GONE);

        Intent it = new Intent(SocketClientService.ACTION_SEND_STICKER);
        it.putExtra("STICKER_ID",
            String.valueOf(stickerListAdapter.getItem((Integer)v.getTag())));
        it.putExtra("UID", toUuid);
        it.setClass(this, SocketClientService.class);
        startService(it);
    }

    public void onSend(View v)
    {
        String chatContent = txtChatInput.getText().toString();

        if(chatContent.length() <= 0) return;

        Intent it = new Intent(SocketClientService.ACTION_SEND_MESSAGE);
        it.putExtra("MESSAGE", txtChatInput.getText().toString());
        it.putExtra("UID", toUuid);
        it.setClass(this, SocketClientService.class);
        startService(it);
    }

    public String getContactName(String uid)
    {
        if(uid.equals("ME")) return MainActivity.name;

        ArrayList<ClientDatabaseHelper.Contact> contacts =
                ClientDatabaseHelper.getInstance(this).queryAllContacts();

        for(ClientDatabaseHelper.Contact c : contacts)
        {
            if(c.uid.equals(uid)) return c.name;
        }

        return "NONE";
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(SocketClientService.RSP_WELCOME))
            {
                Intent it =
                        new Intent(SocketClientService.ACTION_OFFLINE_NOTI);
                it.setClass(ChatActivity.this, SocketClientService.class);
                startService(it);
                return;
            }

            View vv;
            String content, uid, datetime;

            switch(intent.getAction())
            {
            case SocketClientService.RSP_RECEIVED_MESSAGE:
                uid = intent.getStringExtra("UID");

                if(uid.equals(toUuid))
                {
                    content = intent.getStringExtra("MESSAGE");
                    datetime = intent.getStringExtra("DATETIME");

                    vv = getLayoutInflater()
                            .inflate(R.layout.list_chat_content, null);
                    ((TextView)vv.findViewById(R.id.txtId))
                            .setText(getContactName(uid) + " 說：");
                    ((TextView)vv.findViewById(R.id.txtChat))
                            .setText(content);
                    ((TextView)vv.findViewById(R.id.txtTime))
                            .setText(datetime);
                    ll.addView(vv);
                }
                break;

            case SocketClientService.RSP_RECEIVED_SELF_MESSAGE:
                uid = intent.getStringExtra("UID");

                if(uid.equals(toUuid))
                {
                    content = intent.getStringExtra("MESSAGE");
                    datetime = intent.getStringExtra("DATETIME");

                    vv = getLayoutInflater()
                            .inflate(R.layout.list_chat_content, null);
                    ((TextView)vv.findViewById(R.id.txtId))
                            .setText(MainActivity.name + " 說：");
                    ((TextView)vv.findViewById(R.id.txtChat))
                            .setText(content);
                    ((TextView)vv.findViewById(R.id.txtTime))
                            .setText(datetime);
                    ll.addView(vv);
                }
                break;

            case SocketClientService.RSP_RECEIVED_STICKER:
                uid = intent.getStringExtra("UID");

                if(uid.equals(toUuid))
                {
                    content = intent.getStringExtra("STICKER_ID");
                    datetime = intent.getStringExtra("DATETIME");

                    vv = getLayoutInflater()
                            .inflate(R.layout.list_chat_sticker, null);
                    ((TextView)vv.findViewById(R.id.txtId))
                            .setText(getContactName(uid) + " 說：");
                    ((ImageView)vv.findViewById(R.id.imgSticker))
                            .setImageResource(Integer.parseInt(content));
                    ((TextView)vv.findViewById(R.id.txtTime))
                            .setText(datetime);
                    ll.addView(vv);
                }
                break;

            case SocketClientService.RSP_RECEIVED_SELF_STICKER:
                uid = intent.getStringExtra("UID");

                if(uid.equals(toUuid))
                {
                    content = intent.getStringExtra("STICKER_ID");
                    datetime = intent.getStringExtra("DATETIME");

                    vv = getLayoutInflater()
                            .inflate(R.layout.list_chat_sticker, null);
                    ((TextView)vv.findViewById(R.id.txtId))
                            .setText(MainActivity.name + " 說：");
                    ((ImageView)vv.findViewById(R.id.imgSticker))
                            .setImageResource(Integer.parseInt(content));
                    ((TextView)vv.findViewById(R.id.txtTime))
                            .setText(datetime);
                    ll.addView(vv);
                }
                break;
            }

            scrollChat.post(new Runnable()
            {
                @Override
                public void run()
                {
                    scrollChat.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    };
}