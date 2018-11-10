package com.Kushcabbage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Waitclass implements Runnable {
    String itunesmusicdir ="E:/Music/iTunes/iTunes Media/Music";
    String itunesprogdir="C:\\Program Files\\iTunes\\iTunes.exe";
    String song;
    Set<File> possiblesongs = new HashSet<>();
    @Override
    public void run() {
        int retrycount=0;

        try {
            TimeUnit.SECONDS.sleep(5);
            System.out.println("WAITTHREAD: looking for " + song);
            while(true) {
                if(retrycount>6){
                    break;
                }
                if (possiblesongs.size() == 1) {
                    openfile((File) possiblesongs.toArray()[0]);
                    System.out.println("only one song matching");
                    return;
                }

                for (File track : possiblesongs) {
                    String trackname = track.getName().toLowerCase();
                    trackname = trackname.substring(0, trackname.length() - 4);
                    trackname = trackname.replaceAll("[^a-z ]", "").trim();
                    System.out.println(trackname+"?");
                }

                //searches for exact name matches
                for (File track : possiblesongs) {
                    String trackname = track.getName().toLowerCase();
                    trackname = trackname.substring(0, trackname.length() - 4);
                    trackname = trackname.replaceAll("[^a-z ]", "").trim();
                    if (song.equals(trackname.trim())) {
                        System.out.println("WAITHREAD: " + song + " matches " + trackname);
                        openfile(track);
                        return;
                    }
                }
                ///pick closest length song title
                int lowestdiff=100;
                File bestmatch=null;
                for (File track : possiblesongs) {
                    String trackname = track.getName().toLowerCase().substring(0, track.getName().length() - 4).replaceAll("[^a-z ]", "").trim();
                    if(trackname.equals(song.replace(" ",""))){
                        openfile(track);
                    }
                    int diff=Math.abs(trackname.length()-song.length());
                    if(diff<lowestdiff){
                        bestmatch=track;
                    }
                }
                openfile(bestmatch);

                retrycount++;
            }
            System.out.println("found nothing");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void openfile(File track){
        System.out.println("\u001B[36m"+"Playing "+track.getName().substring(0,track.getName().length()-4)+" by "+track.getParentFile().getParentFile().getName()+"\u001B[0m");
        ProcessBuilder pb = new ProcessBuilder(itunesprogdir, track.getPath());
        pb.directory(new File("C:/"));
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            System.out.println("exiting in 5 seconds");
            TimeUnit.SECONDS.sleep(5);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("could not find "+song+" closing in 6 seconds");


    }
}
