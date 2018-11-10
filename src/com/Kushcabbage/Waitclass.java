package com.Kushcabbage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Waitclass implements Runnable {
    String itunesmusicdir ="E:/Music/iTunes/iTunes Media/Music";
    String itunesprogdir="C:\\Program Files\\iTunes\\iTunes.exe";
    String song;
    String artist;
    Set<File> possiblesongs = new HashSet<>();
    @Override
    public void run() {
        int retrycount=0;

        try {
            TimeUnit.SECONDS.sleep(6);
            System.out.println("WAITTHREAD: looking for " + song+" by "+artist);
            while(true) {
                if(retrycount>7){
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
                    System.out.println(track.getName()+"?");
                }

                //searches for exact name matches
                for (File track : possiblesongs) {
                    String trackname = track.getName().toLowerCase();
                    trackname = trackname.substring(0, trackname.length() - 4);
                    trackname = trackname.replaceAll("[^a-z ]", "").trim();
                    if (song.equals(trackname.trim())) {
                        System.out.println("WAITHREAD: exact name match");
                        openfile(track);
                        return;
                    }
                }
                ///check items in possible songs list by seeing is the song's artist contains input artist
                for(File track: possiblesongs){
                    String by=track.getParentFile().getParentFile().toString().toLowerCase();
                    if(by.contains(artist)){
                        openfile(track);
                        System.out.println("found matching song by matching artist");
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
                if(bestmatch!=null) {
                    openfile(bestmatch);
                }
                retrycount++;
            }
            System.out.println("found nothing looking for "+song+" by "+artist);
            ///check all songs by artist and match super loosely
            possiblesongs.addAll(allsongsbyartist(artist));

            for(File track:possiblesongs){      //same code as above where code searches for exact name matches
                String trackname = track.getName().toLowerCase().substring(0, track.getName().length() - 4).replaceAll("[^a-z ]", "").trim();
                if (song.equals(trackname.trim())) {
                    System.out.println("WAITHREAD: " + song + " matches " + trackname);
                    openfile(track);
                    return;
                }
            }

            for(File track:possiblesongs){
                String trackname = track.getName().toLowerCase().substring(0, track.getName().length() - 4).replaceAll("[^a-z ]", "").trim();
                //implement super-loose condition
                for(String word:song.split(" ")){
                    if(trackname.contains(word.toLowerCase())){
                        System.out.println("this song by correct artist and matching a word in track name");
                        openfile(track);
                        return;
                    }
                }
            }

            //match against all songs??
            CompareAgainstall(song);    //populate possiblesongs list
            if(!possiblesongs.isEmpty()){
                for(File track:possiblesongs){
                    String trackname = track.getName().toLowerCase().substring(0, track.getName().length() - 4).replaceAll("[^a-z ]", "").trim();
                    if(trackname.contains(song)){
                        System.out.println("matched from all songs");
                        openfile(track);
                        return;
                    }

                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void CompareAgainstall(String song){
        List<File> allsongs= ListOfAllSongs(new File(itunesmusicdir));
        //System.out.println(allsongs);
        for(File track:allsongs){
            if(track.getName().toLowerCase().replace(" ","").contains(song.toLowerCase().replace(" ",""))){
                //System.out.println("out of all songs maybe "+track.getName());
                possiblesongs.add(track);
                //openfile(track);
                //return;
            }
        }
    }

    List<File> ListOfAllSongs(File dir){
        List<File> songlist = new ArrayList<>();

        File[] artists = dir.listFiles();
        for(File artist:artists){
            File[] albums=artist.listFiles();
            for(File album:albums){
                File[] songs=album.listFiles();
                try {
                    songlist.addAll(Arrays.asList(songs));
                }catch (Exception e){

                }
            }
        }

        return  songlist;
    }

    List<File> allsongsbyartist(String artist){
        List<File> songsbyartist=new ArrayList<>();
        File[] artistpaths= new File(itunesmusicdir).listFiles();
        for(File artistpath: artistpaths){
            if(artistpath.getName().toLowerCase().equals(artist.toLowerCase())){
                File[] albums=artistpath.listFiles();
                for(File album:albums){
                    File[] songs = album.listFiles();
                    songsbyartist.addAll(Arrays.asList(songs));
                }
                return  songsbyartist;
            }
        }
        return new ArrayList<>();
    }

    void openfile(File track){
        System.out.println("\u001B[36m"+"Playing "+track.getName().substring(0,track.getName().length()-4)+" by "+track.getParentFile().getParentFile().getName()+"\u001B[0m");
        ProcessBuilder pb = new ProcessBuilder(itunesprogdir, track.getPath());
        pb.directory(new File("C:/"));
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            System.out.println("exiting in 10 seconds");
            TimeUnit.SECONDS.sleep(10);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("could not find "+song+" closing in 6 seconds");


    }
}
