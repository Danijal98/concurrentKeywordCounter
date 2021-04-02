package main;

import crawler.DirectoryCrawler;
import exceptions.FileCorrupted;
import jobDispatcher.JobDispatcher;
import jobsQueue.JobsQueue;
import jobsQueue.MyQueue;

import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;

public class MainCLI {

    private static DirectoryCrawler directoryCrawler;
    private static MyQueue jobsQueue;
    private static JobDispatcher jobDispatcher;

    public static void main(String[] args) {
        // Parse properties file
        File file = new File("app.properties");
        ConfigurationReader reader = new ConfigurationReader(file);
        try {
            reader.readConfiguration();
        } catch (FileNotFoundException | FileCorrupted e) {
            e.printStackTrace();
            System.exit(0);
        }

        // Init components
        jobsQueue = new JobsQueue();
        directoryCrawler = new DirectoryCrawler(reader.getCrawlerSleepTime(), reader.getPrefix(), jobsQueue);
        jobDispatcher = new JobDispatcher(jobsQueue);

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
        while(true) {
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
                case "stop":
                    // todo stop all threads
                    System.out.println("Closing app...");
                    System.exit(0);
                    break;
            }

        }
    }

}
