package jobs;

import enums.ScanType;

import java.util.Map;
import java.util.concurrent.Future;

public class Job implements ScanningJob {

    private final ScanType scanType;

    public Job(ScanType scanType) {
        this.scanType = scanType;
    }

    @Override
    public ScanType getType() {
        return scanType;
    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public Future<Map<String, Integer>> initiate() {
        return null;
    }
}
