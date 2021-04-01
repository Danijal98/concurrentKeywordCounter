package retriever;

import enums.ScanType;

import java.util.Map;
import java.util.concurrent.Future;

public interface Retriever {
    Map<String, Integer> getResult(String query);
    Map<String, Integer> queryResult(String query);
    void clearSummary(ScanType summaryType);
    Map<String, Map<String, Integer>> getSummary(ScanType summaryType);
    Map<String, Map<String, Integer>> querySummary(ScanType summaryType);
    void addCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult);
}
