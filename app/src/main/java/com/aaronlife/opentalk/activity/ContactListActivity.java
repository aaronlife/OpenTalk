package com.aaronlife.opentalk.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaronlife.opentalk.R;
import com.aaronlife.opentalk.adapter.ContactListAdapter;
import com.aaronlife.opentalk.adapter.FoundListAdapter;
import com.aaronlife.opentalk.adapter.InvitationListAdapter;
import com.aaronlife.opentalk.client.ClientDatabaseHelper;
import com.aaronlife.opentalk.client.SocketClientService;

import opentalk_server.OTProtocol;

public class ContactListActivity extends AppCompatActivity
{
    ContactListAdapter ca;
    ClientDatabaseHelper cm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        // 顯示版本名稱
        TextView txtVersion = (TextView)findViewById(R.id.version);
        PackageInfo pInfo = null;
        try
        {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch(PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        txtVersion.setText(pInfo.versionName);

        ca = new ContactListAdapter(this);
        cm = ClientDatabaseHelper.getInstance(this);

        getSupportActionBar().setTitle("UID: " + MainActivity.uid);

        ListView listContact = (ListView)findViewById(R.id.listContact);
        listContact.setAdapter(ca);
        listContact.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id)
            {
                Intent it = new Intent();
                it.setClass(ContactListActivity.this, ChatActivity.class);
                it.putExtra("NAME", ca.getItem(position).name);
                it.putExtra("UID", ca.getItem(position).uid);
                it.putExtra("CHAT_MESSAGE",
                            cm.queryChatMessage(ca.getItem(position).uid));
                startActivity(it);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketClientService.RSP_WELCOME);
        intentFilter.addAction(SocketClientService.RSP_FOUND_ITEM);
        intentFilter.addAction(SocketClientService.RSP_NEW_INVITATION);
        intentFilter.addAction(SocketClientService.RSP_ADD_FRIEND);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_MESSAGE);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_SELF_MESSAGE);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_STICKER);
        intentFilter.addAction(SocketClientService.RSP_RECEIVED_SELF_STICKER);
        intentFilter.addAction(SocketClientService.WRONG_CLIENT_VERSION);
        registerReceiver(receiver, intentFilter);

        Intent it = new Intent(SocketClientService.ACTION_OFFLINE_NOTI);
        it.setClass(this, SocketClientService.class);
        startService(it);
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    View dlgView;
    AlertDialog dlg;
    FoundListAdapter foundListAdapter;
    InvitationListAdapter invitationListAdapter;

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setNegativeButton("Close", null);

        switch(item.getItemId())
        {
        case R.id.menu_add_friend:
            dlgView = inflater.inflate(R.layout.dialog_find_friend, null);
            ListView listFound =
                            (ListView)dlgView.findViewById(R.id.listFound);
            foundListAdapter = new FoundListAdapter(this);
            listFound.setAdapter(foundListAdapter);
            listFound.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent,
                                        View view,
                                        int position,
                                        long id)
                {
                    Intent itService =
                        new Intent(SocketClientService.ACTION_SEND_INVITATION);
                    itService.setClass(ContactListActivity.this,
                                       SocketClientService.class);
                    itService.putExtra("UID",
                                       foundListAdapter.getItem(position).uid);
                    startService(itService);

                    Toast.makeText(ContactListActivity.this,
                                   "Send invitation to " +
                                   foundListAdapter.getItem(position).name,
                                   Toast.LENGTH_SHORT).show();
                    dlg.dismiss();
                }
            });

            builder.setView(dlgView);
            dlg = builder.show();
            break;
        case R.id.menu_invitations:
            dlgView = inflater.inflate(R.layout.dialog_invitations, null);
            ListView listInvitation =
                    (ListView)dlgView.findViewById(R.id.listInvitations);
            invitationListAdapter =
                    new InvitationListAdapter(this, cm.queryAllInvitation());
            listInvitation.setAdapter(invitationListAdapter);
            builder.setView(dlgView);
            dlg = builder.show();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            switch(intent.getAction())
            {
            case SocketClientService.RSP_WELCOME:
                Intent it =
                        new Intent(SocketClientService.ACTION_OFFLINE_NOTI);
                it.setClass(ContactListActivity.this,
                            SocketClientService.class);
                startService(it);
                break;

            case SocketClientService.RSP_FOUND_ITEM:
                if(intent.getStringExtra("NAME").equals(OTProtocol.NOT_FOUND))
                {
                    Toast.makeText(ContactListActivity.this,
                                   "不存在的UID",
                                   Toast.LENGTH_SHORT).show();
                }
                else
                {
                    foundListAdapter.addItem(intent.getStringExtra("NAME"),
                                             intent.getStringExtra("UID"));
                    ListView listFound =
                            (ListView)dlgView.findViewById(R.id.listFound);
                    listFound.setAdapter(foundListAdapter);
                }
                break;

            case SocketClientService.RSP_NEW_INVITATION:
                cm.insertInvitation(new ClientDatabaseHelper.Contact(
                                        intent.getStringExtra("UID"),
                                        intent.getStringExtra("NAME"), ""));
                break;

            case SocketClientService.RSP_ADD_FRIEND:
                cm.insertContact(new ClientDatabaseHelper.Contact(
                                        intent.getStringExtra("UID"),
                                        intent.getStringExtra("NAME"), ""));
                ListView listContact =
                            (ListView)findViewById(R.id.listContact);
                listContact.setAdapter(ca);
                break;

            case SocketClientService.RSP_RECEIVED_MESSAGE:
                cm.insertChatMessage(new ClientDatabaseHelper.ChatMessage(
                        intent.getStringExtra("UID"),
                        "ME",
                        intent.getStringExtra("MESSAGE"),
                        intent.getStringExtra("DATETIME"),
                        ClientDatabaseHelper.ChatMessage.TYPE_MESSAGE));
                break;
            case SocketClientService.RSP_RECEIVED_SELF_MESSAGE:
                cm.insertChatMessage(new ClientDatabaseHelper.ChatMessage(
                        "ME",
                        intent.getStringExtra("UID"),
                        intent.getStringExtra("MESSAGE"),
                        intent.getStringExtra("DATETIME"),
                        ClientDatabaseHelper.ChatMessage.TYPE_MESSAGE));
                break;
            case SocketClientService.RSP_RECEIVED_STICKER:
                cm.insertChatMessage(new ClientDatabaseHelper.ChatMessage(
                        intent.getStringExtra("UID"),
                        "ME",
                        intent.getStringExtra("STICKER_ID"),
                        intent.getStringExtra("DATETIME"),
                        ClientDatabaseHelper.ChatMessage.TYPE_STICKER));
                break;
            case SocketClientService.RSP_RECEIVED_SELF_STICKER:
                cm.insertChatMessage(new ClientDatabaseHelper.ChatMessage(
                        "ME",
                        intent.getStringExtra("UID"),
                        intent.getStringExtra("STICKER_ID"),
                        intent.getStringExtra("DATETIME"),
                        ClientDatabaseHelper.ChatMessage.TYPE_STICKER));
                break;
            case SocketClientService.WRONG_CLIENT_VERSION:
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactListActivity.this);
                builder.setMessage("Client version too low, please update from Google Play Store");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ContactListActivity.this.finish();
                    }
                });
                builder.show();
                break;
            }
        }
    };

    public void onFind(View v)
    {
        String uid = ((EditText)dlgView.findViewById(R.id.txtFindName))
                                                    .getText().toString();

        if(uid.length() != 8)
        {
            Toast.makeText(this, "UID為８位數數字", Toast.LENGTH_SHORT).show();
            return;
        }

        for(ClientDatabaseHelper.Contact c : cm.queryAllContacts())
        {
            if(c.uid.equals(uid))
            {
                Toast.makeText(this, "已經是好友了", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent itService = new Intent(SocketClientService.ACTION_FIND);
        itService.setClass(this, SocketClientService.class);
        itService.putExtra("UID", uid);
        startService(itService);
    }

    public void onInvitationAccept(View v)
    {
        ClientDatabaseHelper.Contact contact =
                                    (ClientDatabaseHelper.Contact)v.getTag();
        Toast.makeText(this, contact.name + " is accepted your request.",
                       Toast.LENGTH_SHORT).show();

        Intent itService =
                new Intent(SocketClientService.ACTION_ACCEPT_INVITATION);
        itService.setClass(this, SocketClientService.class);
        itService.putExtra("UID", contact.uid);
        startService(itService);

        // 刪除邀請
        cm.deleteInvitation(contact.uid);
        ListView listInvitation =
                    (ListView)dlgView.findViewById(R.id.listInvitations);
        listInvitation.setAdapter(invitationListAdapter);

        // 新增好友
        cm.insertContact(contact);
        ListView listContact = (ListView)findViewById(R.id.listContact);
        listContact.setAdapter(ca);
    }

    public void onInvitationDelete(View v)
    {
        ClientDatabaseHelper.Contact contact =
                            (ClientDatabaseHelper.Contact)v.getTag();
        Toast.makeText(this, contact.name, Toast.LENGTH_SHORT).show();

        Intent itService =
                new Intent(SocketClientService.ACTION_DELETE_INVITATION);
        itService.setClass(this, SocketClientService.class);
        itService.putExtra("UID", contact.uid);
        startService(itService);

        cm.deleteInvitation(contact.uid);
        ListView listInvitation =
                    (ListView)dlgView.findViewById(R.id.listInvitations);
        listInvitation.setAdapter(invitationListAdapter);
    }
}