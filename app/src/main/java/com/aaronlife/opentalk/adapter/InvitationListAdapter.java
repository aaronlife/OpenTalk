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

public class InvitationListAdapter extends BaseAdapter
{
    private LayoutInflater li;
    ArrayList<ClientDatabaseHelper.Contact> invitations;

    public InvitationListAdapter(Context context,
                         ArrayList<ClientDatabaseHelper.Contact> invitations)
    {
        this.li = LayoutInflater.from(context);
        this.invitations = invitations;
    }

    @Override
    public int getCount()
    {
        return invitations.size();
    }

    @Override
    public ClientDatabaseHelper.Contact getItem(int position)
    {
        return invitations.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = li.inflate(R.layout.list_invitations, parent, false);

        ((TextView)convertView.findViewById(R.id.txtInName))
                .setText(getItem(position).name);

        convertView.findViewById(R.id.btnInAdd).setTag(getItem(position));
        convertView.findViewById(R.id.btnInDel).setTag(getItem(position));

        return convertView;
    }
}