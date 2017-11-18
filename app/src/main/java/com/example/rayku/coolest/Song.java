package com.example.rayku.coolest;

class Song{
    private long id;
    private String title;
    private String artist;
    private int duration;

    Song(long id, String title, String artist, int duration){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    Song(){} // null song for MyListsFragment

    long getId(){ return id; }
    String getTitle(){return title;}
    String getArtist(){return artist;}
    int getDuration(){ return duration; }
}
