package crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DirectoryCrawler implements Crawler, Runnable {

    private Map<File, Long> filesMap = new ConcurrentHashMap<>();
    private Queue<File> dirs = new ConcurrentLinkedDeque<>();
    private long sleepTime;
    private String corpusPrefix;

    public DirectoryCrawler(long sleepTime, String corpusPrefix) {
        this.sleepTime = sleepTime;
        this.corpusPrefix = corpusPrefix;
    }

    @Override
    public void run() {
        // loop through queue
        while(true) {
            for (File dir : dirs) {
                parseDirectory(dir);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
                            //System.out.println(f.getParentFile().getName());
                            filesMap.put(f, f.lastModified());
                            makeJobAndSendToQueue(f);
                        }
                    }else {
                        //System.out.println(f.getAbsolutePath());
                        filesMap.put(f, f.lastModified());
                        makeJobAndSendToQueue(f);
                    }
                }else {
                    return;
                }
            }
        }
    }

    @Override
    public void makeJobAndSendToQueue(File dir) {
        // make job from dir
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
