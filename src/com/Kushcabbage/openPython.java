package com.Kushcabbage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class openPython {


    public static void main(String[] args) {
        try {
            openPython op = new openPython();

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("done_bitch java dont gaf");
    }



    public openPython() throws IOException {
        String command = "cmd.exe /c G:\\JAVA\\ItunesVoiceControl\\out\\artifacts\\ItunesVoiceControl_jar\\start_porcupine.bat";

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
        //System.out.println("Standard Error:");
        BufferedReader stderr = new BufferedReader(new InputStreamReader(
                powerShellProcess.getErrorStream()));
        while ((line = stderr.readLine()) != null) {
            System.out.println(line);
        }
        stderr.close();


    }

}
