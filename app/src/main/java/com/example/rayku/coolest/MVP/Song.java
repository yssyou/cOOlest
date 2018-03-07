package com.example.rayku.coolest.MVP;

class Song{
    private long id;
    private String title;
    private String artist;
    private int duration;
    boolean selected;

    Song(long id, String title, String artist, int duration){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.selected = false;

    }

    Song(){} // null song for MyListsFragment

    long getId(){ return id; }
    String getTitle(){return title;}
    String getArtist(){return artist;}
    int getDuration(){ return duration; }

}
