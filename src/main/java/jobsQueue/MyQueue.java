package jobsQueue;

import jobs.ScanningJob;

import java.util.concurrent.BlockingQueue;

public interface MyQueue {
    BlockingQueue<ScanningJob> getJobs();
    void addJob(ScanningJob job) throws InterruptedException;
}
