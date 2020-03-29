package de.mvpdt.mvp_dt;

import de.mvpdt.mvp_dt.exception.EmptyTokenException;
import de.mvpdt.mvp_dt.service.DataPreparationService;

import java.io.IOException;
import java.util.LinkedHashMap;

public class StandaloneControlPredictionApp {

    public static void main(String[] args) throws IOException, EmptyTokenException {
        DataPreparationService dataPreparationService = new DataPreparationService();
        LinkedHashMap orderedRawDataFromFile = dataPreparationService
                .prepareRawDataFromFile(StandaloneControlPredictionApp.class
//                        .getResource("/input/testStockPrediction.csv")
                        .getResource("/input/USDJPY_M1_202003190000_202003192016.csv")
//                        .getPath(), 60, 2);
                        .getPath(), 60, 2, 3, 4, 5);

        // dataPreparationService.testPreparedRawData(orderedRawDataFromFile);
        dataPreparationService.prepareRawDataForNeuralNetwork(orderedRawDataFromFile);

        // Dieser Statement gibt nur eine Spalte aus (2 - <HIGH>)
        // System.out.println(orderedRawDataFromFile.values().iterator().next().toString().replace(',','\n'));
    }
}
