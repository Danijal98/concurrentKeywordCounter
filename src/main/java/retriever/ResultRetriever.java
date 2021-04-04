package retriever;

import enums.ScanType;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultRetriever implements Retriever {

    //maps with futures
    private final Map<String, Future<Map<String, Integer>>> fileFutures = new ConcurrentHashMap<>();
    private final Map<String, Future<Map<String, Integer>>> webFutures = new ConcurrentHashMap<>();

    //maps with results
    private final Map<String, Map<String, Integer>> fileResults = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> webResults = new ConcurrentHashMap<>();

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
    public Map<String, Integer> queryWebResult(String corpus) {
        return null;
    }

    @Override
    public void clearSummary(ScanType summaryType) {
        switch (summaryType) {
            case FILE -> {
                fileResults.clear();
                System.out.println("File summary cleared");
            }
            case WEB -> {
                webResults.clear();
                System.out.println("Web summary cleared");
            }
        }
    }

    @Override
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
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
        webFutures.put(url.getHost(), corpusResult);
    }


}
