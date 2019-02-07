package com.Kushcabbage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class Timeout implements Runnable {

    public Timeout(){}
    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(20);
            System.out.println("...jar time out");
            Send("5");
            TimeUnit.SECONDS.sleep(1);
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void Send(String c) throws IOException {
        //3 for listening colour, 5 for reset
        //3 reads as 51, 255 or 240

        //5 reads as 53, 251 or 243

        //SerialPort comPort = SerialPort.getCommPorts()[3];
        //comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 10, 10);


        /*try {

            byte[] msg= c.getBytes();
            comPort.writeBytes(msg,1);

        } catch (Exception e) { e.printStackTrace(); }
        comPort.closePort();*/

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
        System.out.println("Done");

    }
}
