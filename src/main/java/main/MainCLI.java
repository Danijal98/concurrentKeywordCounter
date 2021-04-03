package main;

import crawler.DirectoryCrawler;
import enums.ScanType;
import exceptions.FileCorrupted;
import jobDispatcher.JobDispatcher;
import jobs.PoisonJob;
import jobs.ScanningJob;
import jobs.WebJob;
import jobsQueue.JobsQueue;
import jobsQueue.MyQueue;
import retriever.ResultRetriever;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import java.io.File;
import java.io.FileNotFoundException;

public class MainCLI {

    private static DirectoryCrawler directoryCrawler;
    private static MyQueue jobsQueue;
    private static JobDispatcher jobDispatcher;
    private static ConfigurationReader reader;
    private static ResultRetriever resultRetriever;

    private static volatile boolean run = true;

    public static void main(String[] args) {
        // Parse properties file
        File file = new File("app.properties");
        reader = ConfigurationReader.getInstance();
        try {
            reader.readConfiguration(file);
        } catch (FileNotFoundException | FileCorrupted e) {
            e.printStackTrace();
            System.exit(0);
        }

        // Init components
        jobsQueue = new JobsQueue();
        resultRetriever = new ResultRetriever();
        directoryCrawler = new DirectoryCrawler(reader.getCrawlerSleepTime(), reader.getPrefix(), jobsQueue);
        jobDispatcher = new JobDispatcher(jobsQueue, resultRetriever);

        // Starting threads
        Thread threadCrawler = new Thread(directoryCrawler);
        Thread threadDispatcher = new Thread(jobDispatcher);
        threadCrawler.start();
        threadDispatcher.start();

        // Handle user commands
        parseUserInput();
    }

    private static void parseUserInput() {
        Scanner scanner = new Scanner(System.in);
        while(run) {
            System.out.print("Enter command: ");
            String line = scanner.nextLine();
            String[] split = line.split(" ");
            String function = split[0].trim();
            switch (function) {
                case "ad":
                    if(split.length < 2) {
                        System.out.println("This function requires attribute");
                        break;
                    }
                    try {
                        directoryCrawler.addDir(new File(split[1]));
                    } catch (FileNotFoundException e) {
                        System.out.println("Provided file path is not valid");
                        e.printStackTrace();
                    }
                    break;
                case "aw":
                    if(split.length < 2) {
                        System.out.println("This function requires attribute");
                        break;
                    }
                    try {
                        ScanningJob webJob = new WebJob(ScanType.WEB, new URL(split[1]), reader.getHopCount());
                        jobsQueue.addJob(webJob);
                    } catch (MalformedURLException e) {
                        System.out.println("Passed url is not valid");
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        System.out.println("Failed to put job to queue");
                        e.printStackTrace();
                    }
                    break;
                case "stop":
                    shutdownThreads();
                    break;
            }

        }
        System.out.println("Closing app...");
    }

    private static void shutdownThreads() {
        try {
            jobsQueue.addJob(new PoisonJob(ScanType.POISON));
            directoryCrawler.shutdown();
            run = false;
        } catch (InterruptedException e) {
            System.out.println("Failed to put job to queue");
            e.printStackTrace();
        }

    }

}
