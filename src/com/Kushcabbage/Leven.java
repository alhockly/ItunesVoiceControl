package com.Kushcabbage;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
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
    Timeout timeout;

    public File itunesmusicfolder;
    boolean delaythreadstarted;
    Delayclass delayclass;

    public static void main(String[] args) {



        new Leven();
    }
    /**
     * Constructor
     */
    public Leven() {
        try {
            Getpathsfromfile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        delayclass=new Delayclass();
            timeout=new Timeout();
            Thread thread = new Thread(timeout);
            thread.start();

            delaythreadstarted =false;

        System.out.println("leven method");
        try {
            recursiveFiles(new File(itunesmusicdir), possiblesongs);
        }catch (NullPointerException e){
            System.out.println("Error in resource/paths.txt");
            System.exit(-1);
        }

        itunesmusicfolder=new File(itunesmusicdir);


        File folder=new File(itunesmusicdir);
        artistlist= folder.listFiles();
        //Duplex Configuration
        duplex.setLanguage("en");

        duplex.addResponseListener(new GSpeechResponseListener() {
            Waitclass test=new Waitclass();
            Thread threadtest=new Thread(test);


            public void onResponse(GoogleResponse googleResponse) {
                String output = "";

                //Get the response from Google Cloud
                output = googleResponse.getResponse();
                System.out.println(output);
                if (output != null) {
                    makeDecision(output,googleResponse);
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
    public void makeDecision(String output,GoogleResponse googleResponse) {

        if (delaythreadstarted == false) {
            Thread thread = new Thread(delayclass);
            thread.start();
            delaythreadstarted =true;
        }


        output = output.trim();
        float outputconfidence=Float.parseFloat(googleResponse.getConfidence());
        if (!oldText.equals(output)){
            oldText = output;}
        else{
            return;}
        output=output.toLowerCase();

        if(output.contains("cancel")){
            System.exit(0);
        }

        if(output.contains("play")&&output.contains("by")) {
            int diff=output.indexOf("by")+2-output.length();
            if(diff==0){
                return;
            }

            //should probs start another thread to wait for full response


            inputsong = googleResponse.getResponse().substring(output.indexOf("play") + 4, output.indexOf("by")).trim();
            inputartist = googleResponse.getResponse().substring(output.indexOf("by") + 2, output.length()).trim();

            if (!inputsong.equals("") && !inputartist.equals("")) {
                boolean listready=false;
                while(!listready){
                    try{
                        possiblesongs.add(new Song(itunesmusicfolder,itunesmusicfolder));
                        listready=true;
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                }
                for (Song track : possiblesongs) {



                    String trackname = track.trackname.toLowerCase();

                    String artistname = track.artist.toLowerCase().trim();
                    if (artistname.contains("feat")) {
                        artistname = artistname.substring(0, artistname.indexOf("feat"));
                    }
                    if (artistname.contains("ft")) {
                        artistname = artistname.substring(0, artistname.indexOf("ft"));
                    }

                    int score = calculate(removetextinbrackets(trackname), inputsong);  //levenshtein distance score
                    score += calculate(artistname, inputartist)*1.2;        //potentially multiply this score by some value before adding to introduce weighting

                    track.score = score;
                }

                Collections.sort(possiblesongs, new LevenComparator());

                for (int i = 0; i < possiblesongs.size(); i++) {

                    Song track=possiblesongs.get(i);
                    String trackname = track.trackname.toLowerCase();
                    String artistname = track.artist.toLowerCase().trim();

                    //if first letter of track==first letter of track said. maybe should use first 2 letters
                    if (trackname.length() > 1 && trackname.substring(0, 2).toLowerCase().equals(inputsong.substring(0, 2).toLowerCase())) {
                        track.score -= 35;  //this is such a random value, i really dk what to say
                    }
                    if (trackname.toLowerCase().equals(inputsong.toLowerCase())) {
                        track.score -= 40;
                    }
                    if (artistname.toLowerCase().equals(inputartist.toLowerCase())) {     ///Exact title and artist match!  (should probs be worth a lot of points)
                        track.score -= 40;
                    }


                    for (String word : inputsong.split(" ")) {
                        String name = removetextinbrackets(possiblesongs.get(i).track.getName().toLowerCase());
                        //minus 20 eveerytime a word in input matches a word in trackname
                        if (name.contains(word.toLowerCase())) {
                            possiblesongs.get(i).score -= 20;
                            //return;
                        }
                    }
                    //if track artist name contains input artist
                    if (possiblesongs.get(i).artist.toLowerCase().contains(inputartist.toLowerCase())) {         //if artist contains   should match word not substring
                        possiblesongs.get(i).score -= 20;
                    }

                }

                Collections.sort(possiblesongs, new LevenComparator());

                    /////if there is is more than one song with the lowest score more refinement is needed
                    List<Song> tightlist= new ArrayList<>();
                    float lowest=possiblesongs.get(0).score;
                    for(Song song:possiblesongs){
                        if(song.score==lowest){
                            tightlist.add(song);
                        }
                        if(song.score>lowest){
                            break;
                        }
                    }

                    if(tightlist.size()==1) {
                        openfile(tightlist.get(0));
                    }else{


                        //some ninja code to make ultra sure the right song is played
                        for(Song item:tightlist){
                            item.score=0;
                            item.score=item.track.getName().length()-inputsong.length();
                        }
                        Collections.sort(tightlist, new LevenComparator());
                        openfile(tightlist.get(0));
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
        return name.trim();
    }


    void openfile(Song track){
        System.out.println("\u001B[36m"+"Playing "+track.track.getName().substring(0,track.track.getName().length()-4)+" by "+track.track.getParentFile().getParentFile().getName()+"\u001B[0m"+" score: "+track.score);
        ProcessBuilder pb = new ProcessBuilder(itunesprogdir, track.track.getPath());
        pb.directory(new File("C:/"));
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            System.out.println("exiting in 3 seconds");
            TimeUnit.SECONDS.sleep(3);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("could not find "+song+" closing in 6 seconds");


    }

    void Getpathsfromfile() throws FileNotFoundException {
        Scanner scan = new Scanner(new File("resource/paths.txt"));
        ArrayList<String> lines=new ArrayList<>();
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            lines.add(line);
        }
        itunesmusicdir=lines.get(0);
        itunesprogdir=lines.get(1);
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
        List<Song> listofar=new ArrayList<>();
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
