package com.word.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.word.music.R;
import com.word.music.model.MusicData;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {
    private List<MusicData> list = null;
    private Context context = null;

    public MusicListAdapter(List<MusicData> list, Context context) {
        super();
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder item = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list, null);
            item = new ViewHolder();
            item.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            item.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
            convertView.setTag(item);
        } else {
            item = (ViewHolder) convertView.getTag();
        }
        item.tv_name.setText(list.get(position).getMusicName());
        item.tv_author.setText(list.get(position).getMusicAuthor());

        return convertView;
    }


    public class ViewHolder {
        public TextView tv_name;
        public TextView tv_author;
    }
}
