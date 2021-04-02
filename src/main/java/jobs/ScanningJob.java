package jobs;

import enums.ScanType;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ScanningJob extends Callable<Map<String, Integer>> {
    ScanType getType();
    String getQuery();
    Future<Map<String, Integer>> initiate();
}