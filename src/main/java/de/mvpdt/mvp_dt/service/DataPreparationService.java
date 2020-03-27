package de.mvpdt.mvp_dt.service;

import de.mvpdt.mvp_dt.exception.EmptyTokenException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Service
public class DataPreparationService {
    /**
     * The method gets raw data from csv file and splits each column of it (e.g. columns <HIGH> <LOW> <CLOSE> <TICKVOL>)
     * by the columnIndex into portions with given amount of rows (each row contains timestamp + value).
     * This makes it possible to let the neural network consume not the whole amount of the raw data at once but
     * in portions and make a prediction after each portion which can be used to see how changes the prediction accuracy with
     * amount of consumed raw data.
     *
     * @param rawDataFilePath path to the *.csv file with raw data
     * @param amountOfRows    in each portion of the raw data
     * @param columnIndex     of one or more columns
     * @return LinkedHashMap with a LinkedHashMap for each single raw data column
     * @throws EmptyTokenException when the first value in a column is null
     */
    public LinkedHashMap prepareRawDataFromFile(final String rawDataFilePath, final int amountOfRows, final int... columnIndex) throws EmptyTokenException {
        // return
        final LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>>>> splitedRowNumberWithRawDataListWithIndex = new LinkedHashMap<>();
        //final LinkedList<LinkedHashMap<Integer, LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>>>> splitedRowNumberWithRawDataListWithIndex = new LinkedList<>();
        for (int index : columnIndex) {
            try (BufferedReader reader = new BufferedReader(new FileReader(rawDataFilePath));) {
                String line;
                String lastToken = "";
                int rowNumber = 0;
                LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>> rowNumberWithRawDataList = new LinkedList<>();
                LinkedHashMap<Integer, LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>>> splitedRowNumberWithRawDataList = new LinkedHashMap<>();
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("<")) { // die Überschriftszeile soll nicht genommen werden
                        rowNumber++;
                        String[] tokens = line.split("\t");
                        String dataAndTime = tokens[0] + " " + tokens[1];
                        String token = tokens[index];

                        // if the current token is null take the previous one (from the last day)
                        if (StringUtils.isEmpty(token)) {
                            token = lastToken;
                        } else {
                            lastToken = token;
                            if (StringUtils.isBlank(lastToken)) {
                                throw new EmptyTokenException("The first value cannot be null");
                            }
                        }

                        double currentValue = Double.parseDouble(token);

                        final LinkedHashMap<String, Double> rawDataWithTimeString = new LinkedHashMap<>();
                        rawDataWithTimeString.put(dataAndTime, currentValue);

                        final LinkedHashMap<Integer, LinkedHashMap<String, Double>> rawData = new LinkedHashMap<>();
                        rawData.put(rowNumber, rawDataWithTimeString);

                        rowNumberWithRawDataList.add(rawData);
                        if (rowNumber % amountOfRows == 0) {
                            final LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>> temp = new LinkedList<>();
                            temp.addAll(rowNumberWithRawDataList);
                            splitedRowNumberWithRawDataList.put(rowNumber, temp);
                            rowNumberWithRawDataList.clear();
                        }
                    }
                }
                //splitedRowNumberWithRawDataListWithIndex.add(splitedRowNumberWithRawDataList);
                splitedRowNumberWithRawDataListWithIndex.put(index, splitedRowNumberWithRawDataList);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return splitedRowNumberWithRawDataListWithIndex;
    }

    public void testPreparedRawData(final LinkedHashMap orderedRawDataFromFile) {
        // LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>>>>
        for (Object allColumnsAndAllRowsInGivenPortions : orderedRawDataFromFile.values())
            for (Object allRowsForAllColumns : ((LinkedHashMap) allColumnsAndAllRowsInGivenPortions).values())
                for (Object rowNumberAndTimeStampWithRawValue : ((LinkedList) allRowsForAllColumns))
                    for (Object timeStampAndRawValue : ((LinkedHashMap) rowNumberAndTimeStampWithRawValue).values())
                        for (Object rawValue : ((LinkedHashMap) timeStampAndRawValue).values())
                            System.out.println(rawValue); // as Double
    }

    public void prepareRawDataForNeuralNetwork(final LinkedHashMap orderedRawDataFromFile) {
        final Set entrySet = orderedRawDataFromFile.entrySet();
        final Object[] objects = entrySet.toArray();
        for (Object o1 : objects) {
            final Object columnIndex = ((Map.Entry) o1).getKey();
            final Object o2 = ((Map.Entry) o1).getValue();
            final Set entrySet2 = ((LinkedHashMap) o2).entrySet();
            final Object[] objects1 = entrySet2.toArray();
            int firstPortionElement = 1;
            for (Object portion : objects1) {
                int lastPortionElement = (Integer) ((Map.Entry) portion).getKey();
                System.out.println("first portion element = " + firstPortionElement + ", last portion element = " + lastPortionElement);
                firstPortionElement = lastPortionElement + 1;
                final Object portionValue = ((Map.Entry) portion).getValue();
                for (Object o3 : (LinkedList) portionValue) {
                    final Set rowNumberAndTimeStampWithRowValue = ((LinkedHashMap) o3).entrySet();
                    final Object[] asArray = rowNumberAndTimeStampWithRowValue.toArray();
                    for (Object o4 : asArray) {
                        final Object rowNumber = ((Map.Entry) o4).getKey();
                        System.out.println("columnIndex = " + columnIndex + ", rowNumber = " + rowNumber);
                        final Object timeStampAndRawValue = ((Map.Entry) o4).getValue();
                        System.out.println("timeStampAndRawValue = " + timeStampAndRawValue);
                    }
                }
            }
        }
    }
}
