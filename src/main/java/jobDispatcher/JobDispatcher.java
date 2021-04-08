package jobDispatcher;

import enums.ScanType;
import jobs.FileJob;
import jobs.ScanningJob;
import jobs.WebJob;
import jobsQueue.MyQueue;
import retriever.ResultRetriever;
import retriever.Retriever;
import tasks.FileTask;
import tasks.WebTask;

import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class JobDispatcher implements Dispatcher {

    //thread pools
    private final ForkJoinPool filePool;
    private final ExecutorService service;
    private final ExecutorCompletionService<Map<String, Integer>> webPool;

    private final MyQueue jobsQueue;
    private final Retriever retriever;
    private final List<String> visitedUrls;
    private volatile boolean run = true;

    public JobDispatcher(MyQueue jobsQueue, Retriever retriever) {
        this.jobsQueue = jobsQueue;
        filePool = new ForkJoinPool();
        service = Executors.newCachedThreadPool();
        webPool = new ExecutorCompletionService<>(service);
        visitedUrls = Collections.synchronizedList(new ArrayList<>());
        this.retriever = retriever;
        this.retriever.setVisitedUrls(visitedUrls);
    }

    @Override
    public void run() {
        ScanningJob job;
        while (run) {
            try {
                job = jobsQueue.getJobs().take();
                if (job.getType().equals(ScanType.FILE)) {
                    FileJob fj = (FileJob) job;
                    FileTask fileTask = new FileTask(fj.getDir().listFiles());
                    Future<Map<String, Integer>> fileFuture = filePool.submit(fileTask);
                    retriever.addCorpusResult(fj.getDir(), fileFuture);
                } else if (job.getType().equals(ScanType.WEB)) {
                    WebJob wj = (WebJob) job;
                    if (visitedUrls.contains(wj.getUrl().toString())) continue;
                    visitedUrls.add(wj.getUrl().toString());
                    WebTask webTask = new WebTask(wj.getHops(), wj.getUrl(), jobsQueue);
                    webPool.submit(webTask);
                    Future<Map<String, Integer>> webFuture = webPool.take();
                    retriever.addWebResult(wj.getUrl(), webFuture);
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
