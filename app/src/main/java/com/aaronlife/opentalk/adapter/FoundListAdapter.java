package com.aaronlife.opentalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aaronlife.opentalk.R;

import java.util.ArrayList;

public class FoundListAdapter extends BaseAdapter
{
    public class FoundItem
    {
        public FoundItem(String name, String uid)
        {
            this.name = name;
            this.uid = uid;
        }

        public String name;
        public String uid;
    }

    private LayoutInflater li;
    ArrayList<FoundItem> foundItems = new ArrayList<>();

    public FoundListAdapter(Context context)
    {
        this.li = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return foundItems.size();
    }

    @Override
    public FoundItem getItem(int position)
    {
        return foundItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = li.inflate(R.layout.list_found_item, parent, false);

        ((TextView) convertView.findViewById(R.id.txtFoundName)).setText(getItem(position).name + "(" + getItem(position).uid + ")");

        return convertView;
    }

    public void addItem(String name, String uuid)
    {
        foundItems.add(new FoundItem(name, uuid));
    }
}