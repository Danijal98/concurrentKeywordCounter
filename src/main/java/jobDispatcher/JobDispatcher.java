package jobDispatcher;

import enums.ScanType;
import jobs.FileJob;
import jobs.ScanningJob;
import jobsQueue.MyQueue;
import retriever.Retriever;
import tasks.FileTask;
import tasks.Task;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

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
                    Task fileTask = new FileTask(((FileJob) job).getDir().listFiles());
                    Future<Map<String, Integer>> resultFuture =  filePool.submit(fileTask);
                    Map<String, Integer> result = resultFuture.get();
                    System.out.println(result);
                } else if (job.getType().equals(ScanType.WEB)) {
                    System.out.println("Web");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
