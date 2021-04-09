package tasks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class FileSummaryTask implements Callable<Map<String, Integer>> {

    private List<Map<String, Integer>> list;

    public FileSummaryTask(List<Map<String, Integer>> list) {
        this.list = list;
    }

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> res = new ConcurrentHashMap<>();
        for (Map<String, Integer> map: list) {
             res = mergeMaps(res, map);
        }
        return res;
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
