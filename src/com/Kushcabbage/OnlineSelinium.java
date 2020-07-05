package com.Kushcabbage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class OnlineSelinium {

    public static void main(String[] args) {
        new OnlineSelinium().GetPage("https://www.youtube.com/results?search_query=21+savage+alot");
    }


    String content = null;
    URLConnection connection = null;
    WebDriver driver=null;



    public OnlineSelinium(){
        driver = new HtmlUnitDriver();
    }


    public void GetPage(String url) {


        driver.get(url);
        String sansundertale =driver.getPageSource();
        System.out.println();

    }
}
