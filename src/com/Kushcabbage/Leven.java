package com.Kushcabbage;


import java.io.*;
import java.net.URISyntaxException;
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
    String output="";
    String itunesmusicdir ="E:/Music/iTunes/iTunes Media/Music";
    String itunesprogdir="C:\\Program Files\\iTunes\\iTunes.exe";
    File[] artistlist;
    List<Song> possiblesongs=new ArrayList<>();
    String inputsong,inputartist;
    Timeout timeout;

    public File itunesmusicfolder;

    SerialThread serialthread;

    public static void main(String[] args) {



        //comPort = SerialPort.getCommPorts()[3];
        //comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);



        new Leven();
    }
    /**
     * Constructor
     */
    public Leven() {


        serialthread = new SerialThread();
        serialthread.start();

        try {
            Getpathsfromfile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("leven method");


        timeout=new Timeout();
        Thread thread = new Thread(timeout);
        thread.start();



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


                //Get the response from Google Cloud
                output = googleResponse.getResponse();
                System.out.println(output);
                if (output != null && googleResponse.isFinalResponse()) {
                    makeDecision(output,googleResponse);
                }
            }
        });

        try {
            //Send("3");
            serialthread.Send("3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        startSpeechRecognition();


    }

    /**
     * This method makes a decision based on the given text of the Speech Recognition
     *
     * @param
     */
    public void makeDecision(String output,GoogleResponse googleResponse) {




        output = output.trim();
        float outputconfidence=Float.parseFloat(googleResponse.getConfidence());
        if (!oldText.equals(output)){
            oldText = output;}
        else{
            return;}
        output=output.toLowerCase();

        if(output.contains("cancel")){
            System.out.println("Voice control cancelled by user");
            try {
                serialthread.Send("5");
                //Send("5");
                TimeUnit.SECONDS.sleep(1);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }


        if(output.contains("turn the lights")){
            try {
                if(output.contains("on")){
                    Send("5");
                    System.exit(0);
                }
                if(output.contains("off")){
                    Send("0");
                    System.exit(0);}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if(output.contains("play")&&output.contains("by")) {


            int diff=output.indexOf("by")+2-output.length();
            if(diff==0){
                return;
            }


            inputsong = googleResponse.getResponse().substring(output.indexOf("play") + 4, output.indexOf("by")).trim();
            inputartist = googleResponse.getResponse().substring(output.indexOf("by") + 2, output.length()).trim();

            if (!inputsong.equals("") && !inputartist.equals("")) {
                try {
                    serialthread.Send("5");
                    //Send("5");
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

                    int score = calculateLeven(removetextinbrackets(trackname), inputsong);  //levenshtein distance score
                    score += calculateLeven(artistname.replace(" ",""), inputartist.replace(" ",""));   //potentially multiply this score by some value before adding to introduce weighting

                    track.score = score;
                }

                Collections.sort(possiblesongs, new LevenComparator());

                for (int i = 0; i < possiblesongs.size(); i++) {

                    Song track=possiblesongs.get(i);
                    String trackname = track.trackname.toLowerCase();
                    String artistname = track.artist.toLowerCase().trim();

                    //if track name isnt an exact match multiply score by 150%
                    if (!trackname.toLowerCase().equals(inputsong.toLowerCase())) {
                        track.score+=(track.score/2);
                    }

                    //if first letter of track==first letter of track said. maybe should use first 2 letters //Im thinking about removing this condition
                    if (trackname.length() > 1 && trackname.substring(0, 2).toLowerCase().equals(inputsong.substring(0, 2).toLowerCase())) {
                        //track.score -= 18;
                    }


                    if (artistname.toLowerCase().equals(inputartist.toLowerCase())) {     ///Exact title and artist match!  (should probs be worth a lot of points)
                        //track.score -= 40;
                    }


                    for (String word : inputsong.split(" ")) {
                        //minus 20 eveerytime a word in input matches a word in trackname
                        if (trackname.contains(word.toLowerCase())) {
                            //track.score -= 20;
                            //return;
                        }
                    }
                    //if track artist name contains input artist
                    if (track.artist.toLowerCase().contains(inputartist.toLowerCase())) {         //if artist contains   should match word not substring
                        //track.score -= 20;
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

                ///if the right song probably wasnt found just search youtube
                //(This also has the advantage of spell check)
                int onlineThreshold=10;
                if(possiblesongs.get(0).score>onlineThreshold){                                               ///THRESHOLD
                    System.out.println("Opening on YouTube");
                    System.out.println("top score from local was "+possiblesongs.get(0).toString()+"\t Threshold: "+onlineThreshold);
                    for(int i=1;i<10;i++){
                        System.out.println(possiblesongs.get(i).toString());
                    }
                    try {
                        new Online().OpenYoutube(inputsong,inputartist);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    System.out.println("returning to hotword recog...");
                    System.exit(0);


                }else{
                    if(tightlist.size()==1) {
                        openfile(tightlist.get(0));
                    }else{

                        //some ninja code to make ultra sure the right song is played
                        /*for(Song item:tightlist){
                            item.score=0;
                            item.score=item.track.getName().length()-inputsong.length();
                        }*/
                        Collections.sort(tightlist, new LevenComparator());
                        /*try {
                            Send("5");
                            TimeUnit.SECONDS.sleep(1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/

                        openfile(tightlist.get(0));
                    }




                }


            }
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

    void Send(String c) throws IOException {
        //3 for listening colour, 5 for reset

        String command = "powershell.exe  G:\\JAVA\\ItunesVoiceControl\\send"+c+".ps1";

        // Executing the command
        Process powerShellProcess = Runtime.getRuntime().exec(command);
        // Getting the results
        powerShellProcess.getOutputStream().close();
        String line;
        System.out.println("Standard Output:");
        BufferedReader stdout = new BufferedReader(new InputStreamReader(
                powerShellProcess.getInputStream()));
        while ((line = stdout.readLine()) != null) {
            System.out.println(line);
        }
        stdout.close();
        System.out.println("Standard Error:");
        BufferedReader stderr = new BufferedReader(new InputStreamReader(
                powerShellProcess.getErrorStream()));
        while ((line = stderr.readLine()) != null) {
            System.out.println(line);
        }
        stderr.close();
        //System.out.println("Done");

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

    static int calculateLeven(String x, String y) {

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
