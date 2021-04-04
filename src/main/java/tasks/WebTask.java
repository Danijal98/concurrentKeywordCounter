package tasks;

import enums.ScanType;
import jobs.WebJob;
import jobsQueue.MyQueue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.WordCounter;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

public class WebTask implements Callable<Map<String, Integer>> {

    private final int hops;
    private final URL url;
    private final MyQueue queue;

    public WebTask(int hops, URL url, MyQueue queue) {
        this.hops = hops;
        this.url = url;
        this.queue = queue;
    }

    public URL getUrl() {
        return url;
    }

    public int getHops() {
        return hops;
    }

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> resultMap = null;
        try {
            Document doc = Jsoup.connect(url.toString()).get();
            Elements links = doc.select("a[href]");
            String text = doc.body().text();
            resultMap = WordCounter.wordFrequency(text);
            if(hops > 0) {
                for (Element link : links) {
                    WebJob wj = new WebJob(ScanType.WEB, new URL(link.attr("abs:href")), hops - 1);
                    queue.addJob(wj);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to get the provided link");
        } catch (InterruptedException e) {
            System.out.println("Adding sub link to job failed, falling back...");
        }
        return resultMap;
    }

}
