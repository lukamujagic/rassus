package hr.fer.tel.rassus.lab2.data;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class DataManager {
    private final Map<Long, Integer> receivedData = new TreeMap<>();
    private final Map<Long, Integer> measuredData = new TreeMap<>();
    private final Map<Long, Integer> accumulatedReceivedData = new TreeMap<>();
    private final Map<Long, Integer> accumulatedMeasuredData = new TreeMap<>();

    public void addReading(long activeMillis, Integer no2Value){
        measuredData.put(activeMillis, no2Value);
    }

    public void addReceivedData(String data){
        String[] splitData = data.split(",");

        System.out.println("received Before " + splitData[0]);

        long millis;
        try{
            millis = Long.parseLong(splitData[0]);
        } catch (NumberFormatException e){
            millis = 0L;
        }

        System.out.println("received After " + millis);

        Integer no2Value;
        try{
            no2Value = Integer.parseInt(splitData[1]);
        } catch (NumberFormatException e){
            no2Value = null;
        }

        receivedData.put(millis, no2Value);
    }

    public Map<Long, Integer> getAccumulatedData(){
        Map<Long, Integer> accumulated = new TreeMap<>();
        accumulated.putAll(accumulatedMeasuredData);
        accumulated.putAll(accumulatedReceivedData);

        return accumulated;
    }

    public Map<Long, Integer> accumulateData(){
        Map<Long, Integer> accumulated = new TreeMap<>();
        accumulated.putAll(measuredData);
        accumulated.putAll(receivedData);

        accumulatedMeasuredData.putAll(measuredData);
        accumulatedReceivedData.putAll(receivedData);

        measuredData.clear();
        receivedData.clear();

        return accumulated;
    }

    public Double calculateAverage(Map<Long, Integer> data){
        return data.values().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum() / (1.0 * data.size());
    }

    public Double calculateReceivedAverage() {
        return calculateAverage(accumulatedReceivedData);
    }

    public Double calculateMeasuredAverage() {
        return calculateAverage(accumulatedMeasuredData);
    }

    public Double calculateAccumulatedAverage() {
        return calculateAverage(getAccumulatedData());
    }
}
