package tasks;

import com.sun.jdi.event.ThreadStartEvent;
import utils.ConfigurationReader;
import utils.WordCounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class FileTask extends RecursiveTask<Map<String, Integer>> {

    private final File[] fileList;

    public FileTask(File[] fileList) {
        this.fileList = fileList;
    }

    @Override
    protected Map<String, Integer> compute() {
        int i;
        long size = 0;
        long fileLimit = ConfigurationReader.getInstance().getFileSizeLimit();
        File[] compute;
        File[] delegate;
        if(fileList.length == 0) return new HashMap<>();
        size += fileList[0].length();
        for(i=1; i<fileList.length; i++) {
            if(size > fileLimit) break;
            size += fileList[i].length();
        }
        compute = Arrays.copyOfRange(fileList, 0, i);
        delegate = Arrays.copyOfRange(fileList, i, fileList.length);

        FileTask delegateTask = new FileTask(delegate);
        delegateTask.fork();
        Map<String, Integer> thisWords = wordsInCorpus(compute);
        Map<String, Integer> delegatedWords = delegateTask.join();

        return mergeMaps(thisWords, delegatedWords);
    }

    private Map<String, Integer> wordsInCorpus(File[] corpus) {
        Map<String, Integer> finalMap = new HashMap<>();
        for(File file: corpus) {
            Map<String, Integer> fileMap;
            try {
                fileMap = WordCounter.wordFrequency(file);
                finalMap = mergeMaps(finalMap, fileMap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return finalMap;
    }

    /***
     * https://howtodoinjava.com/java/collections/hashmap/merge-two-hashmaps/
     * @param map1
     * @param map2
     * @return
     */
    private static Map<String, Integer> mergeMaps(Map<String, Integer> map1, Map<String, Integer> map2) {
        map1.forEach((key, value) -> map2.merge(key, value, Integer::sum));
        return map2;
    }

}
