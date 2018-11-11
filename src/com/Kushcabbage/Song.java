package com.Kushcabbage;
import java.io.File;


public class Song {
    public File track;
    String artist;
    public float score;
    String trackname;
    public Song(File track,File itunesmusicfolder){
        this.track=track;

        if(track.getParentFile().getParentFile().getPath().equals(itunesmusicfolder)){
            artist=track.getParentFile().getName();
        }
        else {
            this.artist = track.getParentFile().getParentFile().getName();
        }

        trackname=track.getName();




    }

    @Override
     public String toString(){
        return track.getName()+" by "+artist+" score: "+score;
    }


    String removetextinbrackets(String name){

        if(name.contains("(")){
            if(name.contains(")")) {
                String sub = name.substring(name.indexOf("("), name.indexOf(")")+1);
                name = name.replace(sub, "");
            }
            else{
                name=name.substring(0,name.indexOf("("));
            }
        }
        return name;
    }
}
