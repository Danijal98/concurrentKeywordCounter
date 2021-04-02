package utils;

import main.ConfigurationReader;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordCounter {

    public static Map<String, Integer> wordFrequency(String text) {
        // This will match all non-word characters, i.e. characters that are
        // not in [a-zA-Z_0-9]. This should match whitespaces and interpunction.
        String nonWordDelimiter="[\\W]+";

        String[] words = text.split(nonWordDelimiter);

        Map<String, Integer> frequencies = new LinkedHashMap<>();
        for (String word : words) {
            if(Arrays.asList(ConfigurationReader.getInstance().getKeywords()).contains(word)){
                if (!word.isEmpty()) {
                    Integer frequency = frequencies.get(word);

                    if (frequency == null) {
                        frequency = 0;
                    }

                    ++frequency;
                    frequencies.put(word, frequency);
                }
            }
        }
        return frequencies;
    }

    public static Map<String, Integer> wordFrequency(File file) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try{
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return wordFrequency(sb.toString());
    }

}
