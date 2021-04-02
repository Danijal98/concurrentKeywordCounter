package jobsQueue;

import jobs.ScanningJob;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JobsQueue implements MyQueue {

    private final BlockingQueue<ScanningJob> jobs = new LinkedBlockingQueue<>();

    @Override
    public BlockingQueue<ScanningJob> getJobs() {
        return jobs;
    }

    @Override
    public void addJob(ScanningJob job) throws InterruptedException {
        jobs.put(job);
    }

}
