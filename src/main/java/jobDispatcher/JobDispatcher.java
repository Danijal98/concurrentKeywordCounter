package jobDispatcher;

import enums.ScanType;
import jobs.FileJob;
import jobs.ScanningJob;
import jobs.WebJob;
import jobsQueue.MyQueue;
import retriever.Retriever;
import tasks.FileTask;
import tasks.WebTask;

import java.util.Map;
import java.util.concurrent.*;

public class JobDispatcher implements Dispatcher {

    //thread pools
    private ForkJoinPool filePool;
    private ExecutorService service;
    private ExecutorCompletionService<Map<String, Integer>> webPool;

    private final MyQueue jobsQueue;
    //todo make pool of this
    private Retriever retriever;
    private volatile boolean run = true;

    public JobDispatcher(MyQueue jobsQueue, Retriever retriever) {
        this.jobsQueue = jobsQueue;
        filePool = new ForkJoinPool();
        service = Executors.newCachedThreadPool();
        webPool = new ExecutorCompletionService<>(service);
        this.retriever = retriever;
    }

    @Override
    public void run() {
        ScanningJob job;
        while (run) {
            try {
                job = jobsQueue.getJobs().take();
                if (job.getType().equals(ScanType.FILE)) {
                    FileTask fileTask = new FileTask(((FileJob) job).getDir().listFiles());
                    // todo send this future to result retriever
                    Future<Map<String, Integer>> fileFuture =  filePool.submit(fileTask);
                    //TODO
                } else if (job.getType().equals(ScanType.WEB)) {
                    WebJob wj = (WebJob) job;
                    WebTask webTask = new WebTask(wj.getHops(), wj.getUrl(), jobsQueue);
                    webPool.submit(webTask);
                    // todo send this future to result retriever
                    Future<Map<String, Integer>> webFuture = webPool.take();
                    //TODO
                } else if(job.getType().equals(ScanType.POISON)) {
                    run = false;
                    shutdown();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Stopping dispatcher...");
    }

    @Override
    public void shutdown() {
        service.shutdown();
        filePool.shutdown();
    }
}
