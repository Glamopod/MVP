package de.mvpdt.mvp_dt;

import de.mvpdt.mvp_dt.exception.EmptyTokenException;
import de.mvpdt.mvp_dt.service.DataPreparationService;

import java.io.IOException;
import java.util.LinkedHashMap;

public class StandaloneControlPredictionApp {

    private static final String TEST_FILE1 = "/input/USDJPY_M1_202003190000_202003192016.csv";
    private static final String TEST_FILE2 = "/input/testForwards.csv";
    private static final String TEST_FILE3 = "/input/testBackwards.csv";
    private static final int slidingWindowSize = 5;


    public static void main(String[] args) throws IOException, EmptyTokenException {

        DataPreparationService dataPreparationService = new DataPreparationService();
        LinkedHashMap orderedRawDataFromFile2 = dataPreparationService
                .prepareRawDataFromFile(StandaloneControlPredictionApp.class
                        .getResource(TEST_FILE2)
                        .getPath(), 60, 2);


        LinkedHashMap orderedRawDataFromFile3 = dataPreparationService
                .prepareRawDataFromFile(StandaloneControlPredictionApp.class
                        .getResource(TEST_FILE3)
                        .getPath(), 60, 2);

        // dataPreparationService.testPreparedRawData(orderedRawDataFromFile);
        dataPreparationService.prepareRawDataForNeuralNetwork(orderedRawDataFromFile2, slidingWindowSize, true);
        dataPreparationService.prepareRawDataForNeuralNetwork(orderedRawDataFromFile3, slidingWindowSize, false);

        // Dieser Statement gibt nur eine Spalte aus (2 - <HIGH>)
        // System.out.println(orderedRawDataFromFile.values().iterator().next().toString().replace(',','\n'));
    }
}
