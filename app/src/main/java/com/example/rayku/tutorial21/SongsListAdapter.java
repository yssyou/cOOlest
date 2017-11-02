package com.example.rayku.tutorial21;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class SongsListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Song> arrayList;

    private AssetManager assetManager;
    private Typeface typeFace;

    SongsListAdapter(Context context, ArrayList<Song> arrayList){
        this.context = context;
        this.arrayList = arrayList;
        assetManager = context.getAssets();
        typeFace = Typeface.createFromAsset(assetManager, "Amatic-Bold.ttf");
    }

    private class ViewHolder { // this is an amazing piece of code :D
        TextView songTitle;
        TextView songArtist;
        ImageView imageView;

        ViewHolder(View view) {
            songTitle = view.findViewById(R.id.song_title);
            songArtist = view.findViewById(R.id.song_artist);
            imageView = view.findViewById(R.id.imageView);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.song, viewGroup, false);
            viewHolder = new ViewHolder(view);

            viewHolder.songTitle.setTypeface(typeFace);
            viewHolder.songArtist.setTypeface(typeFace);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        Song currentSong = (Song)getItem(i);

        viewHolder.songTitle.setText(currentSong.getTitle());
        viewHolder.songArtist.setText(currentSong.getArtist());
        viewHolder.imageView.setImageResource(R.drawable.musicnote);

        return view;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
