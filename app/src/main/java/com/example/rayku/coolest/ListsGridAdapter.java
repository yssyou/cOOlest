package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

class ListsGridAdapter extends BaseAdapter {

    private Context context;
    private Typeface typeFace;
    private ArrayList<String> listsTitles;
    private String currList;

    ListsGridAdapter(Context context, ArrayList<String> listsTitles, Typeface typeFace, String currList){
        this.context = context;
        this.listsTitles = listsTitles;
        this.typeFace = typeFace;
        this.currList = currList;
        Collections.sort(listsTitles);
    }

    private class ViewHolder {
        TextView listTitle;
        ViewHolder(View view) {
            listTitle = view.findViewById(R.id.listTitle);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.list_title, viewGroup, false);
            viewHolder = new ViewHolder(view);
            viewHolder.listTitle.setTypeface(typeFace);
            view.setTag(viewHolder);

            if(listsTitles.get(i).equals(currList))
                view.setBackgroundColor(Color.argb(60, 0, 0, 0));

        } else {
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.listTitle.setText(listsTitles.get(i));
        return view;
    }

    @Override
    public int getCount() {
        return listsTitles.size();
    }

    @Override
    public Object getItem(int position) {
        return listsTitles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
