package com.Kushcabbage;

import com.fazecast.jSerialComm.*;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SerialDevice {

    static SerialPort comPort;
    static String portname="COM3";
    //static SerialPort comPort;

    SerialDevice(String pn){
        portname=pn;
        SerialPort comPort = SerialPort.getCommPorts()[3];
        //comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 10, 10);

        comPort.openPort();


    }

    public static void main(String[] args) throws InterruptedException {
        for(SerialPort sp:SerialPort.getCommPorts()){
            System.out.println(sp.getDescriptivePortName());

        }
        SerialDevice s = new SerialDevice("c");

        while (true) {
            s.Send("3");
            TimeUnit.MILLISECONDS.sleep(500);
            s.Send("5");
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }



    void Send(String c){
        //3 for listening colour, 5 for reset
        //3 reads as 51, 255 or 240

        //5 reads as 53, 251 or 243

        //SerialPort comPort = SerialPort.getCommPorts()[3];
        //comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 10, 10);


        try {

                byte[] msg= c.getBytes();
                comPort.writeBytes(msg,1);

        } catch (Exception e) { e.printStackTrace(); }
        comPort.closePort();


    }
}
