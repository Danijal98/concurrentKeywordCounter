package jobs;

import enums.ScanType;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.Future;

public class WebJob implements ScanningJob {

    private final int hops;
    private final ScanType type;
    private final URL url;

    public WebJob(ScanType type, URL url, int hops) {
        this.type = type;
        this.url = url;
        this.hops = hops;
    }

    public int getHops() {
        return hops;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public ScanType getType() {
        return this.type;
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
    public Map<String, Integer> call() {
        return null;
    }
}
