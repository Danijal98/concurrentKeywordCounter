package jobs;

import enums.ScanType;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

public class FileJob implements ScanningJob {

    private final File dir;
    private final ScanType scanType;

    public FileJob(ScanType scanType, File dir) {
        this.dir = dir;
        this.scanType = scanType;
    }

    public File getDir() {
        return dir;
    }

    @Override
    public ScanType getType() {
        return scanType;
    }

    @Override
    public String getQuery() {
        return null;
    }

    //TODO
    @Override
    public Future<Map<String, Integer>> initiate() {
        return null;
    }

    @Override
    public Map<String, Integer> call() {
        return null;
    }
}
