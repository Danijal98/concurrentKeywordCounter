package main;

import exceptions.FileCorrupted;

import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;

public class ConfigurationReader {

    private String[] keywords;
    private String prefix;
    private long crawlerSleepTime;
    private long fileSizeLimit;
    private int hopCount;
    private long urlRefreshTime;

    private final File configFile;

    public ConfigurationReader(File configFile) {
        this.configFile = configFile;
    }

    public void readConfiguration() throws FileNotFoundException, FileCorrupted {
        Scanner sc = new Scanner(configFile);
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            String[] split = line.split("=");
            if(split.length < 2) throw new FileCorrupted();
            String attribute = split[0];
            String value = split[1];
            switch (attribute) {
                case "keywords" -> {
                    keywords = value.split(",");
                    if (keywords.length == 0) throw new FileCorrupted();
                }
                case "file_corpus_prefix" -> prefix = value;
                case "dir_crawler_sleep_time" -> crawlerSleepTime = Long.parseLong(value);
                case "file_scanning_size_limit" -> fileSizeLimit = Long.parseLong(value);
                case "hop_count" -> hopCount = Integer.parseInt(value);
                case "url_refresh_time" -> urlRefreshTime = Long.parseLong(value);
            }
        }

    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getCrawlerSleepTime() {
        return crawlerSleepTime;
    }

    public void setCrawlerSleepTime(long crawlerSleepTime) {
        this.crawlerSleepTime = crawlerSleepTime;
    }

    public long getFileSizeLimit() {
        return fileSizeLimit;
    }

    public void setFileSizeLimit(long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public long getUrlRefreshTime() {
        return urlRefreshTime;
    }

    public void setUrlRefreshTime(long urlRefreshTime) {
        this.urlRefreshTime = urlRefreshTime;
    }
}
