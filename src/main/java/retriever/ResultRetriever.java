package retriever;

import enums.ScanType;

import java.util.Map;
import java.util.concurrent.Future;

public class ResultRetriever implements Retriever {


    @Override
    public Map<String, Integer> getResult(String query) {
        return null;
    }

    @Override
    public Map<String, Integer> queryResult(String query) {
        return null;
    }

    @Override
    public void clearSummary(ScanType summaryType) {

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
    public void addCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult) {

    }
}
