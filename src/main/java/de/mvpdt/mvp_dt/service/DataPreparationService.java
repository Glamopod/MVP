package de.mvpdt.mvp_dt.service;

import de.mvpdt.mvp_dt.exception.EmptyTokenException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
     * @param amountOfRows in each portion of the raw data
     * @param columnIndex of one or more columns
     * @return LinkedHashMap with a LinkedHashMap for each single raw data column
     * @throws EmptyTokenException when the first value in a column is null
     */
    public LinkedHashMap prepareRawDataFromFile(final String rawDataFilePath, final int amountOfRows, final int ... columnIndex) throws EmptyTokenException{
        // return
        final LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>>>> splitedRowNumberWithRawDataListWithIndex = new LinkedHashMap<>();
        //final LinkedList<LinkedHashMap<Integer, LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>>>> splitedRowNumberWithRawDataListWithIndex = new LinkedList<>();
        for(int index : columnIndex) {
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
                        if(rowNumber % amountOfRows == 0) {
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
}
