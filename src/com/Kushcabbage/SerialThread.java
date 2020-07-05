package com.Kushcabbage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SerialThread extends Thread {


    public void run(){


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

}
