package jobs;

import enums.ScanType;

import java.util.Map;
import java.util.concurrent.Future;

public class PoisonJob implements ScanningJob {

    private ScanType scanType;

    public PoisonJob(ScanType scanType) {
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

    @Override
    public Map<String, Integer> call() throws Exception {
        return null;
    }
}
