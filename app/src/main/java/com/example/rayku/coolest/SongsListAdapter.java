package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class SongsListAdapter extends BaseAdapter implements Filterable{
    private Context context;
    private ArrayList<Song> originalData;
    private ArrayList<Song> filteredData;
    private Typeface typeFace;
    private int theme;

    SongsListAdapter(Context context, ArrayList<Song> originalData, Typeface typeFace, int theme){
        this.context = context;
        this.originalData = originalData;
        filteredData = originalData;
        this.typeFace = typeFace;
        this.theme = theme;
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

            if(theme==0 || theme==1) {
                viewHolder.songTitle.setTextColor(Color.BLACK);
                viewHolder.songArtist.setTextColor(Color.BLACK);
                viewHolder.imageView.setBackgroundResource(R.drawable.musicnote);
            }
            else{
                viewHolder.songTitle.setTextColor(Color.WHITE);
                viewHolder.songArtist.setTextColor(Color.WHITE);
                viewHolder.imageView.setBackgroundResource(R.drawable.musicnote_white);
            }

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
    public int getCount() { return filteredData.size(); }

    @Override
    public Object getItem(int position) { return filteredData.get(position); }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                FilterResults filterResults = new FilterResults();

                if(charSequence==null || charSequence.length()==0){
                    filterResults.values = originalData;
                    filterResults.count = originalData.size();
                } else{
                    ArrayList<Song> filterResultsData = new ArrayList<>();
                    for(Song song : originalData){
                        if(song.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase())){
                            filterResultsData.add(song);
                        }
                    }
                    filterResults.values = filterResultsData;
                    filterResults.count = filterResultsData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList<Song>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
