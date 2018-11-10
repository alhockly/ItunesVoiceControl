package com.Kushcabbage;

import java.io.File;

public class Song {
    public File track, artist;
    public float score;
    String trackname;
    public Song(File track, File artist, float score){
        this.track=track;
        this.artist=artist;
        this.score=score;




    }

    @Override
     public String toString(){
        return track.getName()+" by "+artist.getName();
    }

}
