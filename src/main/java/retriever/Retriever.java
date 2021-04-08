package retriever;

import enums.ScanType;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface Retriever {
    Map<String, Integer> getFileResult(String corpus);
    Map<String, Integer> getWebResult(String url);
    Map<String, Integer> queryFileResult(String corpus);
    Map<String, Integer> queryWebResult(String url);
    void clearSummary(ScanType summaryType);
    Map<String, Map<String, Integer>> getSummary(ScanType summaryType);
    Map<String, Map<String, Integer>> querySummary(ScanType summaryType);
    void addCorpusResult(File corpus, Future<Map<String, Integer>> corpusResult);
    void addWebResult(URL url, Future<Map<String, Integer>> corpusResult);
    void shutdown();
    void setVisitedUrls(List<String> visitedUrls);
}
