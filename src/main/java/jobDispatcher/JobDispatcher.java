package jobDispatcher;

import enums.ScanType;
import jobs.ScanningJob;
import jobsQueue.MyQueue;
import retriever.Retriever;

import java.util.concurrent.ForkJoinPool;

public class JobDispatcher implements Dispatcher {

    //thread pools
    private ForkJoinPool filePool;
    private ForkJoinPool webPool;

    private final MyQueue jobsQueue;
    private Retriever retriever;

    public JobDispatcher(MyQueue jobsQueue) {
        this.jobsQueue = jobsQueue;
        filePool = new ForkJoinPool();
        webPool = new ForkJoinPool();
    }

    @Override
    public void run() {
        ScanningJob job;
        while (true) {
            try {
                job = jobsQueue.getJobs().take();
                System.out.println();
                if (job.getType().equals(ScanType.FILE)) {
                    System.out.println("File");
                } else if (job.getType().equals(ScanType.WEB)) {
                    System.out.println("Web");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
