package com.Kushcabbage;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.LineUnavailableException;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.GSpeechDuplex;
import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;


import net.sourceforge.javaflacencoder.FLACFileWriter;


public class Leven {

    //private final TextToSpeech tts = new TextToSpeech();
    private final Microphone mic = new Microphone(FLACFileWriter.FLAC);
    private final GSpeechDuplex duplex = new GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw");
    String oldText = "";

    String itunesmusicdir ="E:/Music/iTunes/iTunes Media/Music";
    String itunesprogdir="C:\\Program Files\\iTunes\\iTunes.exe";
    File[] artistlist;
    List<Song> possiblesongs=new ArrayList<>();
    Waitclass waitclass;
    private boolean threadstarted;

    public static void main(String[] args) {
        new Leven();
    }
    /**
     * Constructor
     */
    public Leven() {
        waitclass = new Waitclass();
        System.out.println("leven method");


                //ListOfAllSongs(new File(itunesmusicdir));
        recursiveFiles(new File(itunesmusicdir),possiblesongs);
        File folder=new File(itunesmusicdir);
        artistlist= folder.listFiles();
        //Duplex Configuration
        duplex.setLanguage("en");

        duplex.addResponseListener(new GSpeechResponseListener() {

            public void onResponse(GoogleResponse googleResponse) {
                String output = "";

                //Get the response from Google Cloud
                output = googleResponse.getResponse();
                System.out.println(output);
                if (output != null) {
                    makeDecision(output);
                } else
                    System.out.println("Output was null");
            }
        });


        startSpeechRecognition();

    }

    /**
     * This method makes a decision based on the given text of the Speech Recognition
     *
     * @param
     */
    public void makeDecision(String output) {

        output = output.trim();

        if (!oldText.equals(output)){
            oldText = output;}
        else{
            return;}
        output=output.toLowerCase();



        if(output.contains("play")&&output.contains("by")) {
            String song = output.substring(output.indexOf("play")+4,output.indexOf("by")).trim();
            String artist = output.substring(output.indexOf("by")+2,output.length()).trim();

            if(song.equals("") || artist.equals("")) {
            }
            else{

                for(Song track:possiblesongs){
                    float score=0;
                    String trackname = track.track.getName().toLowerCase().substring(0, track.track.getName().length() - 4).trim().replace(".","");
                    String artistname = track.artist.getName().toLowerCase().trim();

                    score=calculate(removetextinbrackets(trackname),song);
                    score+=calculate(track.artist.getName(),artist);
                    if(trackname.length()>1){
                    if(trackname.substring(0,1).toLowerCase().equals(song.substring(0,1).toLowerCase()) && artistname.equals(artist.toLowerCase())){
                        score-=35;
                    }
                    }
                    track.score=score;
                    //System.out.println("");
                }

                System.out.println("done");
                Collections.sort(possiblesongs, new LevenComparator());
                System.out.println("");

                for(int i=0;i<possiblesongs.size();i++){
                    for(String word:song.split(" ")){
                        String name=removetextinbrackets(possiblesongs.get(i).track.getName().toLowerCase());
                        if(name.contains(word.toLowerCase())){
                            //System.out.println("this song by correct artist and matching a single word in track name");
                            possiblesongs.get(i).score-=20;
                            //return;
                        }
                    }
                    if(possiblesongs.get(i).artist.getName().toLowerCase().contains(artist.toLowerCase())){         //if artist contains
                        possiblesongs.get(i).score-=20;
                    }

                }

                Collections.sort(possiblesongs, new LevenComparator());
                System.out.println("");
                openfile(possiblesongs.get(0).track);

            }
        }
        else{
            //System.out.println("Not entered on any else if statement");
        }

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

    static int calculate(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }
    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

    List<Song> ListOfAllSongs(File dir){
        int i=0;
        File lastalbum=null;
        List<Song> songlist = new ArrayList<>();
        try {
            File[] artists = dir.listFiles();
            for (File artist : artists) {
                File[] albums = artist.listFiles();
                for (File album : albums) {
                    File[] songs = album.listFiles();
                    lastalbum=album;
                    for (i=0;i<songs.length;i++) {

                        songlist.add(new Song(songs[i], songs[i].getParentFile().getParentFile(), 0));

                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(i+lastalbum.getName());

        }
        return  songlist;
    }


    void recursiveFiles(File start, List<Song> possiblesongs){
        File[] dir=start.listFiles();
        for(File item:dir){
            if(item.isDirectory()){
                recursiveFiles(item,possiblesongs);
            }
            if(item.isFile()){
                if(item.getName().substring(item.getName().length()-4).contains("png") || item.getName().substring(item.getName().length()-4).contains("jpg")){

                }else {
                    possiblesongs.add(new Song(item, item.getParentFile().getParentFile(), 0));
                }
            }
        }
    }


    public void startSpeechRecognition() {
        //Start a new Thread so our application don't lags
        new Thread(() -> {
            try {
                duplex.recognize(mic.getTargetDataLine(), mic.getAudioFormat());
            } catch (LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Stops the Speech Recognition
     */
    public void stopSpeechRecognition() {
        mic.close();
        System.out.println("Stopping Speech Recognition...." + " , Microphone State is:" + mic.getState());
    }



}
