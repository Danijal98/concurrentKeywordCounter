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
import utils.ConfigurationReader;

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
                case "ad" -> {
                    if (split.length < 2) {
                        System.out.println("This function requires attribute");
                        break;
                    }
                    try {
                        directoryCrawler.addDir(new File(split[1]));
                    } catch (FileNotFoundException e) {
                        System.out.println("Provided file path is not valid");
                    }
                }
                case "aw" -> {
                    if (split.length < 2) {
                        System.out.println("This function requires attribute");
                        break;
                    }
                    try {
                        ScanningJob webJob = new WebJob(ScanType.WEB, new URL(split[1]), reader.getHopCount());
                        jobsQueue.addJob(webJob);
                    } catch (MalformedURLException e) {
                        System.out.println("Passed url is not valid");
                    } catch (InterruptedException e) {
                        System.out.println("Failed to put job to queue");
                    }
                }
                case "get" -> {
                    if (split.length < 2) {
                        System.out.println("This function requires attribute");
                        break;
                    }
                    String[] parts = split[1].split("\\|");
                    if (parts.length < 2) {
                        System.out.println("Incorrect attribute format for get");
                        break;
                    }
                    if (parts[1].equals("summary")) {
                        switch (parts[0]) {
                            case "file" -> System.out.println(resultRetriever.getSummary(ScanType.FILE));
                            case "web" -> System.out.println(resultRetriever.getSummary(ScanType.WEB));
                            default -> System.out.println("Wrong first part of argument");
                        }
                    }else {
                        switch (parts[0]) {
                            case "file" -> System.out.println(resultRetriever.getFileResult(parts[1]));
                            case "web" -> System.out.println(resultRetriever.getWebResult(parts[1]));
                            default -> System.out.println("Wrong first part of argument");
                        }
                    }
                }
                case "query" -> {
                    if (split.length < 2) {
                        System.out.println("This function requires attribute");
                        break;
                    }
                    String[] parts = split[1].split("\\|");
                    if (parts.length < 2) {
                        System.out.println("Incorrect attribute format for query");
                        break;
                    }
                    if (parts[1].equals("summary")) {
                        switch (parts[0]) {
                            case "file" -> {
                                Map<String, Map<String, Integer>> res = resultRetriever.querySummary(ScanType.FILE);
                                if (res == null) {
                                    System.out.println("Result is not ready yet...");
                                }else {
                                    System.out.println(res);
                                }
                            }
                            case "web" -> {
                                Map<String, Map<String, Integer>> res = resultRetriever.querySummary(ScanType.WEB);
                                if (res == null) {
                                    System.out.println("Result is not ready yet...");
                                }else {
                                    System.out.println(res);
                                }
                            }
                            default -> System.out.println("Wrong first part of argument");
                        }
                    }else {
                        switch (parts[0]) {
                            case "file" -> {
                                Map<String, Integer> res = resultRetriever.queryFileResult(parts[1]);
                                if (res == null) {
                                    System.out.println("Result is not ready yet...");
                                }else {
                                    System.out.println(res);
                                }
                            }
                            case "web" -> {
                                Map<String, Integer> res = resultRetriever.queryWebResult(parts[1]);
                                if (res == null || res.isEmpty()) {
                                    System.out.println("Result is not ready yet...");
                                }else {
                                    System.out.println(res);
                                }
                            }
                            default -> System.out.println("Wrong first part of argument");
                        }
                    }
                }
                case "cws" -> resultRetriever.clearSummary(ScanType.WEB);
                case "cfs" -> resultRetriever.clearSummary(ScanType.FILE);
                case "stop" -> shutdownThreads();
                default -> System.out.println("Unknown command");
            }
        }
        System.out.println("Closing app...");
    }

    private static void shutdownThreads() {
        try {
            jobsQueue.addJob(new PoisonJob(ScanType.POISON));
            directoryCrawler.shutdown();
            resultRetriever.shutdown();
            run = false;
        } catch (InterruptedException e) {
            System.out.println("Failed to put job to queue");
            e.printStackTrace();
        }

    }

}
