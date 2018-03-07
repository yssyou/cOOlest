package com.example.rayku.coolest.MVP;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rayku.coolest.R;

import java.util.ArrayList;
import java.util.Collections;

class AdapterListsGrid extends BaseAdapter {

    private Context context;
    private Typeface typeFace;
    private ArrayList<String> listsTitles;
    private String currList;
    private int theme;

    AdapterListsGrid(Context context, ArrayList<String> listsTitles, Typeface typeFace, String currList, int theme){
        this.context = context;
        this.listsTitles = listsTitles;
        this.typeFace = typeFace;
        this.currList = currList;
        this.theme = theme;
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

            if(theme==0 || theme==1) viewHolder.listTitle.setTextColor(Color.BLACK);
            else viewHolder.listTitle.setTextColor(Color.WHITE);

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.listTitle.setText(listsTitles.get(i));

        CharSequence title = viewHolder.listTitle.getText();
        if(title.equals(currList))
            view.setBackgroundColor(Color.argb(40, 128, 128, 128));
        else view.setBackground(null);

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

    void select(String clickedList) {
        if(clickedList.equals("+"))
            return;
        if(clickedList.equals(this.currList)){
            this.currList = "MAIN";
            notifyDataSetChanged();
            return;
        }
        this.currList = clickedList;
        notifyDataSetChanged();
    }
}
