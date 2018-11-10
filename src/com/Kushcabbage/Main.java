package com.Kushcabbage;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.GSpeechDuplex;
import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;


import net.sourceforge.javaflacencoder.FLACFileWriter;


public class Main {

    //private final TextToSpeech tts = new TextToSpeech();
    private final Microphone mic = new Microphone(FLACFileWriter.FLAC);
    private final GSpeechDuplex duplex = new GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw");
    String oldText = "";

    String itunesmusicdir ="E:/Music/iTunes/iTunes Media/Music";
    String itunesprogdir="C:\\Program Files\\iTunes\\iTunes.exe";
    File[] artistlist;
    List<File> possiblesongs;
    Waitclass waitclass;
    private boolean threadstarted;

    public static void main(String[] args) {
        new Main();
    }
    /**
     * Constructor
     */
    public Main() {
        waitclass = new Waitclass();



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

        //---------------MaryTTS Configuration-----------------------------

        // Setting the Current Voice
        //tts.setVoice("cmu-slt-hsmm");

        //JetPilotEffect
        //JetPilotEffect jetPilotEffect = new JetPilotEffect(); //epic fun!!!
        //jetPilotEffect.setParams("amount:100");

        //Apply the effects
        //tts.getMarytts().setAudioEffects(jetPilotEffect.getFullEffectAsString());// + "+" + stadiumEffect.getFullEffectAsString());

        //Start the Speech Recognition
        startSpeechRecognition();

    }

    /**
     * This method makes a decision based on the given text of the Speech Recognition
     *
     * @param
     */
    public void makeDecision(String output) {
        if (threadstarted == false) {
        Thread thread = new Thread(waitclass);
        thread.start();
        threadstarted=true;
        }
        output = output.trim();
        //System.out.println(output.trim());

        //We don't want duplicate responses
        if (!oldText.equals(output)){
            oldText = output;}
        else{
            return;}
        output=output.toLowerCase();



        if(output.contains("play")&&output.contains("by")) {
            String song = output.substring(output.indexOf("play")+4,output.indexOf("by")).trim();
            String artist = output.substring(output.indexOf("by")+2,output.length()).trim();
            waitclass.song=song;
            if(song.equals("") || artist.equals("")) {
            }
            else{

                for(int i=0;i<artistlist.length;i++){
                    //System.out.println(artistlist[i].getName());
                    if(artist.equals(artistlist[i].getName().toLowerCase())){
                        List<File> songlist=new ArrayList<>();
                        System.out.println("playing " + song + " by " + artist);
                        File[] albums = artistlist[i].listFiles();
                        for(File album: albums){
                            File[] songs = album.listFiles();
                            for(File track:songs){
                                songlist.add(track);
                                if(track.getName().toLowerCase().contains(song)){
                                    System.out.println("Defo matches "+track.getName());
                                    waitclass.possiblesongs.add(track);
                                    waitclass.openfile(track);
                                    return;
                                }
                            }
                        }
                        ///choose the closest out of the list?
                        for(File maybe:songlist){
                            String test=maybe.getName().toLowerCase().replace(" ","");
                            String test2=song.replace(" ","");
                            if(maybe.getName().toLowerCase().replace(" ","").contains(song.replace(" ",""))){
                                System.out.println("could be "+maybe);
                                waitclass.possiblesongs.add(maybe);
                            }
                        }
                    }
                        //artist doesnt match


                        ///Make full list of all songs?
                        CompareAgainstall(song);

                }

            }
        }
        else{
            //System.out.println("Not entered on any else if statement");
        }

    }

    void CompareAgainstall(String song){
        List<File> allsongs= ListOfAllSongs(new File(itunesmusicdir));
        //System.out.println(allsongs);
        for(File track:allsongs){
            if(track.getName().toLowerCase().replace(" ","").contains(song.toLowerCase().replace(" ",""))){
                //System.out.println("out of all songs maybe "+track.getName());
                waitclass.possiblesongs.add(track);
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




    /**
     * Calls the MaryTTS to say the given text
     *
     * @param text

    public void speak(String text) {
        System.out.println(text);
        //Check if it is already speaking
        if (!tts.isSpeaking())
            new Thread(() -> tts.speak(text, 2.0f, true, false)).start();

    }
    */

    /**
     * Starts the Speech Recognition
     */
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
