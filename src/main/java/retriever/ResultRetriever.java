package retriever;

import enums.ScanType;
import tasks.WebRetrieverTask;
import utils.ConfigurationReader;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ResultRetriever implements Retriever {

    private List<String> visitedUrls;
    private URLRefresh urlRefresh;

    //maps with futures
    private final Map<String, Future<Map<String, Integer>>> fileFutures = new ConcurrentHashMap<>();
    private final Map<String, Future<Map<String, Integer>>> webFutures = new ConcurrentHashMap<>();

    //maps with results
    private final Map<String, Map<String, Integer>> fileResults = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> webResults = new ConcurrentHashMap<>();

    //add summary maps
    private final Map<String, Integer> fileSummary = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> webSummary = new ConcurrentHashMap<>();

    //add pools
    private final ExecutorService webService = Executors.newCachedThreadPool();
    private final ExecutorCompletionService<Map<String, Integer>> webPool = new ExecutorCompletionService<>(webService);

    public ResultRetriever() {
        urlRefresh = new URLRefresh();
        Thread urlRefreshThread = new Thread(urlRefresh);
        urlRefreshThread.start();
    }

    @Override
    public Map<String, Integer> getFileResult(String corpus) {
        Map<String, Integer> result = fileResults.get(corpus);
        if (result != null) {
            System.out.println("Returned from cache");
            return result;
        }
        Future<Map<String, Integer>> future = fileFutures.get(corpus);
        if (future == null) {
            System.out.println("File never added");
            return null;
        }
        try {
            result = future.get();
            fileResults.put(corpus, result);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error while getting file result...");
        }
        System.out.println("Returned from future");
        return result;
    }

    @Override
    public Map<String, Integer> getWebResult(String url) {
        //TODO get when not finished
        Map<String, Integer> cache = webResults.get(url);
        if (cache != null) return cache;

        WebRetrieverTask webRetrieverTask = new WebRetrieverTask(findAllDomainsForHost(url));
        webPool.submit(webRetrieverTask);
        try {
            Map<String, Integer> result = webPool.take().get();
            webResults.put(url, result);
            return result;
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error in getWebResult");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Integer> queryFileResult(String corpus) {
        boolean finished = true;
        for (Map.Entry<String, Future<Map<String, Integer>>> entry : fileFutures.entrySet()) {
            if (entry.getKey().equals(corpus) && !entry.getValue().isDone()) finished = false;
        }
        if (finished) return getFileResult(corpus);
        return null;
    }

    @Override
    public Map<String, Integer> queryWebResult(String url) {
        boolean finished = true;
        for (Map.Entry<String, Future<Map<String, Integer>>> entry : webFutures.entrySet()) {
            if (entry.getKey().equals(url) && !entry.getValue().isDone()) finished = false;
        }
        if (finished) return getWebResult(url);
        getWebResult(url);
        return null;
    }

    @Override
    public void clearSummary(ScanType summaryType) {
        switch (summaryType) {
            case FILE -> {
                fileSummary.clear();
                System.out.println("File summary cleared");
            }
            case WEB -> {
                webSummary.clear();
                System.out.println("Web summary cleared");
            }
        }
    }

    @Override
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
        //make list of domain and sub domain urls with results
        //checks cache
        //starts task in pool for all
        if (summaryType.equals(ScanType.FILE)) {

        }else if (summaryType.equals(ScanType.WEB)) {

        }
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        return null;
    }

    @Override
    public void addCorpusResult(File corpus, Future<Map<String, Integer>> corpusResult) {
        fileFutures.put(corpus.getName(), corpusResult);
    }

    @Override
    public void addWebResult(URL url, Future<Map<String, Integer>> corpusResult) {
        if (webResults.get(url.getHost()) != null) {
            webResults.remove(url.getHost());
        }
        webFutures.put(url.toString(), corpusResult);
    }

    @Override
    public void shutdown() {
        webService.shutdown();
        urlRefresh.shutdown();
    }

    private Map<String, Map<String, Integer>> findAllDomainsForHost(String url) {
        Map<String, Map<String, Integer>> mapResult = new HashMap<>();
        for (Map.Entry<String, Future<Map<String, Integer>>> entry : webFutures.entrySet()) {
            if (entry.getKey().contains(url)) {
                try {
                    Map<String, Integer> result = entry.getValue().get();
                    if(result != null) {
                        if(!result.isEmpty()) {
                            mapResult.put(entry.getKey(), result);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error in finding sub domains for url");
                    e.printStackTrace();
                }
            }
        }
        return mapResult;
    }

    @Override
    public void setVisitedUrls(List<String> visitedUrls) {
        this.visitedUrls = visitedUrls;
    }

    class URLRefresh implements Runnable {

        private volatile boolean run = true;

        @Override
        public void run() {
            while (run) {
                try {
                    synchronized (this) {
                        wait(ConfigurationReader.getInstance().getUrlRefreshTime());
                    }
                    visitedUrls.clear();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void shutdown() {
            run = false;
            notify();
        }

    }

}