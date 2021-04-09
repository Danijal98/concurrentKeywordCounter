package retriever;

import enums.ScanType;
import tasks.FileSummaryTask;
import tasks.WebRetrieverTask;
import utils.ConfigurationReader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ResultRetriever implements Retriever {

    private List<String> visitedUrls;
    private final URLRefresh urlRefresh;

    //maps with futures
    private final Map<String, Future<Map<String, Integer>>> fileFutures = new ConcurrentHashMap<>();
    private final Map<String, Future<Map<String, Integer>>> webFutures = new ConcurrentHashMap<>();

    //maps with results
    private final Map<String, Map<String, Integer>> fileResults = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> webResults = new ConcurrentHashMap<>();

    //add summary maps
    private Map<String, Integer> fileSummary = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> webSummary = new ConcurrentHashMap<>();

    //add pools
    private final ExecutorService webService = Executors.newCachedThreadPool();
    private final ExecutorCompletionService<Map<String, Integer>> pool = new ExecutorCompletionService<>(webService);

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
        Map<String, Integer> cache = webResults.get(url);
        if (cache != null) return cache;

        WebRetrieverTask webRetrieverTask = new WebRetrieverTask(findAllDomainsForHost(url));
        pool.submit(webRetrieverTask);
        try {
            Map<String, Integer> result = pool.take().get();
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
        Map<String, Map<String, Integer>> result = new HashMap<>();
        List<Map<String, Map<String, Integer>>> list;
        if (summaryType.equals(ScanType.FILE)) {
            if (!fileSummary.isEmpty()) {
                System.out.println("Returned from cache");
                result.put("All files", fileSummary);
                return result;
            }
            List<Map<String, Integer>> fileTaskList = new ArrayList<>();
            for (Map.Entry<String, Future<Map<String, Integer>>> entry : fileFutures.entrySet()) {
                try {
                    fileTaskList.add(entry.getValue().get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            FileSummaryTask fst = new FileSummaryTask(fileTaskList);
            pool.submit(fst);
            try {
                fileSummary = pool.take().get();
                result.put("All files", fileSummary);
                return result;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }else if (summaryType.equals(ScanType.WEB)) {
            if(!webSummary.isEmpty()) {
                System.out.println("Returned from cache");
                return webSummary;
            }

            list = groupUrlResults();
            List<WebRetrieverTask> tasks = new ArrayList<>();
            list.forEach(element -> {
                WebRetrieverTask webRetrieverTask = new WebRetrieverTask(element);
                tasks.add(webRetrieverTask);
                pool.submit(webRetrieverTask);
            });

            for (int i=0; i<tasks.size(); i++) {
                try {
                    String url = new URL(list.get(i).entrySet().iterator().next().getKey()).getHost();
                    result.put(url, pool.take().get());
                } catch (InterruptedException | ExecutionException | MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            webSummary = result;
        }
        return result;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        if (summaryType.equals(ScanType.FILE)) {
            boolean finished = true;
            for (Map.Entry<String, Future<Map<String, Integer>>> entry : fileFutures.entrySet()) {
                if (!entry.getValue().isDone()) finished = false;
            }
            if (finished) return getSummary(ScanType.FILE);
        }else if (summaryType.equals(ScanType.WEB)) {
            boolean finished = true;
            for (Map.Entry<String, Future<Map<String, Integer>>> entry : webFutures.entrySet()) {
                if (!entry.getValue().isDone()) finished = false;
            }
            if (finished) return getSummary(ScanType.WEB);
            getSummary(ScanType.WEB);
        }
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

    private List<Map<String, Map<String, Integer>>> groupUrlResults() {
        List<Map<String, Map<String, Integer>>> res = new ArrayList<>();
        for (Map.Entry<String, Future<Map<String, Integer>>> entry : webFutures.entrySet()) {
            try {
                int pos = doesItExist(res, new URL(entry.getKey()).getHost());
                if(pos == -1) {
                    String host = new URL(entry.getKey()).getHost();
                    var maps = findAllDomainsForHost(host);
                    if(!maps.isEmpty())
                        res.add(maps);
                }
            } catch (MalformedURLException e) {
                System.out.println("Couldn't parse url");
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     *
     * @return -1 if not found or index position if found
     */
    private int doesItExist(List<Map<String, Map<String, Integer>>> list, String host) throws MalformedURLException {
        for(int i=0; i<list.size(); i++) {
            Map<String, Map<String, Integer>> map = list.get(i);
            if(!map.isEmpty()){
                var entry = map.entrySet().iterator().next();
                if(entry.getKey().contains(host)) return i;
            }
        }
        return -1;
    }

    @Override
    public void setVisitedUrls(List<String> visitedUrls) {
        this.visitedUrls = visitedUrls;
    }

    class URLRefresh implements Runnable {

        private volatile boolean run = true;
        private long waitTime = ConfigurationReader.getInstance().getUrlRefreshTime();

        @Override
        public void run() {
            while (run) {
                try {
                    long start = System.currentTimeMillis();
                    synchronized (this) {
                        wait(waitTime);
                    }
                    long end = System.currentTimeMillis();
                    waitTime = end - start;
                    if(waitTime < ConfigurationReader.getInstance().getUrlRefreshTime()) continue;
                    waitTime = ConfigurationReader.getInstance().getUrlRefreshTime();
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