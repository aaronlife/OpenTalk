package com.aaronlife.opentalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aaronlife.opentalk.R;
import com.aaronlife.opentalk.client.ClientDatabaseHelper;

import java.util.ArrayList;

public class ContactListAdapter extends BaseAdapter
{
    private LayoutInflater li;
    private ClientDatabaseHelper cm;

    private ArrayList<ClientDatabaseHelper.Contact> contacts;

    public ContactListAdapter(Context context)
    {
        this.li = LayoutInflater.from(context);
        this.cm = ClientDatabaseHelper.getInstance(context);
        contacts = cm.queryAllContacts();
    }

    @Override
    public int getCount()
    {
        return contacts.size();
    }

    @Override
    public ClientDatabaseHelper.Contact getItem(int position)
    {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ClientDatabaseHelper.Contact contact = getItem(position);

        convertView = li.inflate(R.layout.list_contact_item, parent, false);

        ((TextView) convertView.findViewById(R.id.contact_name))
                               .setText(contact.name);
        ((TextView) convertView.findViewById(R.id.contact_last_talk))
                               .setText(contact.uid);
        ((TextView) convertView.findViewById(R.id.contact_last_talk_time))
                               .setText(contact.datetime);

        return convertView;
    }
}