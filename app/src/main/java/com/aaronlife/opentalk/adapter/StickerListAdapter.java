package com.aaronlife.opentalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aaronlife.opentalk.R;


public class StickerListAdapter extends BaseAdapter
{
    private LayoutInflater li;

    static private Integer[] imgArray = new Integer[24];

    static
    {
        imgArray[0] = new Integer(R.mipmap.sticker_01);
        imgArray[1] = new Integer(R.mipmap.sticker_02);
        imgArray[2] = new Integer(R.mipmap.sticker_03);
        imgArray[3] = new Integer(R.mipmap.sticker_04);
        imgArray[4] = new Integer(R.mipmap.sticker_05);
        imgArray[5] = new Integer(R.mipmap.sticker_06);
        imgArray[6] = new Integer(R.mipmap.sticker_07);
        imgArray[7] = new Integer(R.mipmap.sticker_08);
        imgArray[8] = new Integer(R.mipmap.sticker_09);
        imgArray[9] = new Integer(R.mipmap.sticker_10);
        imgArray[10] = new Integer(R.mipmap.sticker_11);
        imgArray[11] = new Integer(R.mipmap.sticker_12);
        imgArray[12] = new Integer(R.mipmap.sticker_13);
        imgArray[13] = new Integer(R.mipmap.sticker_14);
        imgArray[14] = new Integer(R.mipmap.sticker_15);
        imgArray[15] = new Integer(R.mipmap.sticker_16);
        imgArray[16] = new Integer(R.mipmap.sticker_17);
        imgArray[17] = new Integer(R.mipmap.sticker_18);
        imgArray[18] = new Integer(R.mipmap.sticker_19);
        imgArray[19] = new Integer(R.mipmap.sticker_20);
        imgArray[20] = new Integer(R.mipmap.sticker_21);
        imgArray[21] = new Integer(R.mipmap.sticker_22);
        imgArray[22] = new Integer(R.mipmap.sticker_23);
        imgArray[23] = new Integer(R.mipmap.sticker_24);
    }

    public StickerListAdapter(Context context)
    {
        this.li = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return imgArray.length / 4;
    }

    @Override
    public Integer getItem(int position)
    {
        return imgArray[position];
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = li.inflate(R.layout.list_sticker_row, parent, false);

        ((ImageView) convertView.findViewById(R.id.sticker1))
                .setImageResource(imgArray[position * 4 + 0]);
        convertView.findViewById(R.id.sticker1).setTag(position * 4 + 0);

        ((ImageView) convertView.findViewById(R.id.sticker2))
                .setImageResource(imgArray[position * 4 + 1]);
        convertView.findViewById(R.id.sticker2).setTag(position * 4 + 1);

        ((ImageView) convertView.findViewById(R.id.sticker3))
                .setImageResource(imgArray[position * 4 + 2]);
        convertView.findViewById(R.id.sticker3).setTag(position * 4 + 2);

        ((ImageView) convertView.findViewById(R.id.sticker4))
                .setImageResource(imgArray[position * 4 + 3]);
        convertView.findViewById(R.id.sticker4).setTag(position * 4 + 3);

        return convertView;
    }
}