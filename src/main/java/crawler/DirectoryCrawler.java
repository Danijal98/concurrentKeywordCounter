package crawler;

import enums.ScanType;
import jobs.FileJob;
import jobs.ScanningJob;
import jobsQueue.MyQueue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DirectoryCrawler implements Crawler, Runnable {

    private final Map<File, Long> filesMap = new ConcurrentHashMap<>();
    private final Queue<File> dirs = new ConcurrentLinkedQueue<>();
    private final long sleepTime;
    private final String corpusPrefix;
    private final MyQueue jobsQueue;

    private volatile boolean run = true;

    public DirectoryCrawler(long sleepTime, String corpusPrefix, MyQueue jobsQueue) {
        this.sleepTime = sleepTime;
        this.corpusPrefix = corpusPrefix;
        this.jobsQueue = jobsQueue;
    }

    @Override
    public void run() {
        while(run) {
            for (File dir : dirs) {
                parseDirectory(dir);
            }
            if(!filesMap.isEmpty())
            try {
                synchronized (this) {
                    wait(sleepTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Stopping crawler...");
    }

    public synchronized void shutdown() {
        run = false;
        notify();
    }

    @Override
    public void parseDirectory(File root) {
        File[] list = root.listFiles();
        if (list == null) return;
        for ( File f : list ) {
            if ( f.isDirectory() ) {
                parseDirectory(f);
            }
            else {
                // if parent dir starts with prefix then check, otherwise return
                if (f.getParentFile().getName().startsWith(corpusPrefix)) {
                    // if file changed send file parent to job queue
                    Long oldLastModified = filesMap.get(f);
                    if(oldLastModified != null) {
                        Long newLastModified = f.lastModified();
                        if(!oldLastModified.equals(newLastModified)) {
//                            filesMap.put(f, newLastModified);
                            addSubDirsToFileMap(f.getParentFile());
                            makeJobAndSendToQueue(f.getParentFile());
                        }
                    }else {
//                        filesMap.put(f, f.lastModified());
                        addSubDirsToFileMap(f.getParentFile());
                        makeJobAndSendToQueue(f.getParentFile());
                    }
                }
            }
        }
    }

    @Override
    public void addSubDirsToFileMap(File parentDir) {
        File[] list = parentDir.listFiles();
        for (File f: list != null ? list : new File[0]) {
            filesMap.put(f, f.lastModified());
        }
    }

    @Override
    public void makeJobAndSendToQueue(File dir) {
        ScanningJob fileJob = new FileJob(ScanType.FILE, dir);
        try {
            jobsQueue.addJob(fileJob);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Failed to put job to queue");
        }
    }

    @Override
    public void addDir(File dir) throws FileNotFoundException {
        if (! (dir.exists() && dir.isDirectory())) {
            throw new FileNotFoundException();
        }
        boolean flag = false;
        for(File d: dirs) {
            if (d.equals(dir)) {
                flag = true;
                break;
            }
        }
        if(!flag) {
            dirs.add(dir);
            System.out.println("Directory added");
        }else {
            System.out.println("Directory already saved");
        }
    }
}
