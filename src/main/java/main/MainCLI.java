package main;

import crawler.DirectoryCrawler;
import exceptions.FileCorrupted;
import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;

public class MainCLI {

    private static DirectoryCrawler directoryCrawler;

    public static void main(String[] args) {
        File file = new File("app.properties");
        ConfigurationReader reader = new ConfigurationReader(file);
        try {
            reader.readConfiguration();
        } catch (FileNotFoundException | FileCorrupted e) {
            e.printStackTrace();
            System.exit(1);
        }

        directoryCrawler = new DirectoryCrawler(reader.getCrawlerSleepTime(), reader.getPrefix());
        Thread threadCrawler = new Thread(directoryCrawler);
        threadCrawler.start();

        parseUserInput();
    }

    private static void parseUserInput() {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print("Enter command: ");
            String line = scanner.nextLine();
            String[] split = line.split(" ");
            String function = split[0].trim();
            String attribute;
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
            }

        }
    }

}
