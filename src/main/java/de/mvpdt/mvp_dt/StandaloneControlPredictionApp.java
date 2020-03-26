package de.mvpdt.mvp_dt;

import de.mvpdt.mvp_dt.exception.EmptyTokenException;
import de.mvpdt.mvp_dt.service.DataPreparationService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class StandaloneControlPredictionApp {

    public static void main(String[] args) throws IOException, EmptyTokenException {
        DataPreparationService dataPreparationService = new DataPreparationService();
        LinkedHashMap orderedRawDataFromFile = dataPreparationService
                .prepareRawDataFromFile(StandaloneControlPredictionApp.class
                        .getResource("/input/USDJPY_M1_202003190000_202003192016.csv")
                        .getPath(), 60, 2, 3, 4, 5);

        // LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedList<LinkedHashMap<Integer, LinkedHashMap<String, Double>>>>>
        for (Object value : orderedRawDataFromFile.values())
            for (Object o : ((LinkedHashMap) value).values())
                for (Object o1 : ((LinkedList) o))
                    for (Object o2 : ((LinkedHashMap) o1).values())
                        System.out.println(o2);
                     // for(Object o3 : ((LinkedHashMap)o2).values())
                     //    System.out.println(o3);

        // Dieser Statement gibt nur eine Spalte aus (2 - <HIGH>)
        // System.out.println(orderedRawDataFromFile.values().iterator().next().toString().replace(',','\n'));


    }
}
