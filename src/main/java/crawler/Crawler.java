package crawler;

import java.io.File;
import java.io.FileNotFoundException;

public interface Crawler {
    void parseDirectory(File dir);
    void makeJobAndSendToQueue(File dir);
    void addDir(File dir) throws FileNotFoundException;
}
