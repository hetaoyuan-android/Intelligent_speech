package com.glens.speech.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.glens.speech.R;

import java.util.ArrayList;
import java.util.List;

/**
 *  车辆图片适配器
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> urls = new ArrayList<>();

    public ImageAdapter(Context mContext, List<String> urls) {
        this.mContext = mContext;
        this.urls = urls;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_img, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.iv_img);
            viewHolder.checkBox = convertView.findViewById(R.id.cb_item);
            Glide.with(mContext).load(urls.get(position)).centerCrop()
                    .placeholder(R.mipmap.ic_launcher).into(viewHolder.imageView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            if (urls != null && urls.size() > 0) {
                viewHolder.imageView = convertView.findViewById(R.id.iv_img);
                viewHolder.checkBox = convertView.findViewById(R.id.cb_item);
                Glide.with(mContext).load(urls.get(position)).centerCrop()
                        .placeholder(R.mipmap.ic_launcher).into(viewHolder.imageView);
            }
        }

        return convertView;
    }

    class ViewHolder{
        ImageView imageView;
        CheckBox checkBox;
    }
}
