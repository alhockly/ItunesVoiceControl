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
    String inputsong,inputartist;


    public File itunesmusicfolder;


    public static void main(String[] args) {
        new Leven();
    }
    /**
     * Constructor
     */
    public Leven() {

        System.out.println("leven method");

        itunesmusicfolder=new File(itunesmusicdir);

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
            try {
                TimeUnit.MILLISECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            inputsong = output.substring(output.indexOf("play") + 4, output.indexOf("by")).trim();
            inputartist = output.substring(output.indexOf("by") + 2, output.length()).trim();

            if (!inputsong.equals("") && !inputartist.equals("")) {

                for (Song track : possiblesongs) {
                    float score = 0;
                    //sanitised values - you cant say ( . & % )
                    String trackname = track.track.getName().toLowerCase().substring(0, track.track.getName().length() - 4).trim();
                    trackname = trackname.replace(".", "").replace("&", "and").replace("%", "percent");
                    String artistname = track.artist.toLowerCase().trim();
                    if (artistname.contains("feat")) {
                        artistname = artistname.substring(0, artistname.indexOf("feat"));
                    }
                    if (artistname.contains("ft")) {
                        artistname = artistname.substring(0, artistname.indexOf("ft"));
                    }

                    score = calculate(removetextinbrackets(trackname), inputsong);  //levenshtein distance score
                    score += calculate(artistname, inputartist) * 1.5;        //potentially multiply this score by some value before adding to introduce weighting

                    //if first letter of track==first letter of track said. maybe should use first 2 letters
                    if (trackname.length() > 1 && trackname.substring(0, 2).toLowerCase().equals(inputsong.substring(0, 2).toLowerCase())) {
                        score -= 35;  //this is such a random value, i really dk what to say
                    }
                    if (trackname.equals(inputsong)) {
                        score -= 40;
                    }
                    if (artistname.equals(inputartist.toLowerCase())) {     ///Exact title and artist match!  (should probs be worth a lot of points)
                        score -= 40;
                    }


                    track.score = score;


                    for (int i = 0; i < possiblesongs.size(); i++) {
                        for (String word : inputsong.split(" ")) {
                            String name = removetextinbrackets(possiblesongs.get(i).track.getName().toLowerCase());
                            //minus 20 eveerytime a word in input matches a word in trackname
                            if (name.contains(word.toLowerCase())) {
                                possiblesongs.get(i).score -= 20;
                                //return;
                            }
                        }
                        //if track artist name contains input artist
                        if (possiblesongs.get(i).artist.toLowerCase().contains(inputartist.toLowerCase())) {         //if artist contains
                            possiblesongs.get(i).score -= 20;
                        }

                    }

                    Collections.sort(possiblesongs, new LevenComparator());
                    if (possiblesongs.get(0).score > 0) {                           //by this point there should be some negative values in list
                        System.out.println("\u001B[31m" + "not sure... maybe" + possiblesongs.get(0));
                        try {
                            TimeUnit.SECONDS.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    } else {
                        openfile(possiblesongs.get(0));
                    }
                }
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


    void openfile(Song track){
        System.out.println("\u001B[36m"+"Playing "+track.track.getName().substring(0,track.track.getName().length()-4)+" by "+track.track.getParentFile().getParentFile().getName()+"\u001B[0m"+" score: "+track.score);
        ProcessBuilder pb = new ProcessBuilder(itunesprogdir, track.track.getPath());
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


    ////////completely copied algorithms for levnstehin vals/////

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


    ////////////////////////////////////////////////////////////////////////////////////////


    void recursiveFiles(File start, List<Song> possiblesongs){
        File[] dir=start.listFiles();
        for(File item:dir){
            if(item.isDirectory()){
                recursiveFiles(item,possiblesongs);
            }
            if(item.isFile()){
                ////ignore .png and .jpgs
                if(item.getName().toLowerCase().contains(".png") || item.getName().toLowerCase().contains(".jpg")){

                }else {
                    possiblesongs.add(new Song(item,itunesmusicfolder));
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
