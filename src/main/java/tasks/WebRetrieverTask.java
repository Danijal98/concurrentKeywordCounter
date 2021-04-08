package tasks;

import java.util.Map;
import java.util.concurrent.*;

public class WebRetrieverTask implements Callable<Map<String, Integer>> {

    private Map<String, Map<String, Integer>> resultList;

    public WebRetrieverTask(Map<String, Map<String, Integer>> resultList) {
        this.resultList = resultList;
    }

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> res = new ConcurrentHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : resultList.entrySet()) {
            res = mergeMaps(res, entry.getValue());
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
