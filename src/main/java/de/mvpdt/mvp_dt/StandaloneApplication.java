package de.mvpdt.mvp_dt;

import de.mvpdt.mvp_dt.prediction.NeuralNetworkStockPredictor;
import de.mvpdt.mvp_dt.service.TimeStampService;

import javax.annotation.Resource;
import java.io.IOException;

public class StandaloneApplication {

    @Resource
    private TimeStampService timeStampService;

    public static void main(String[] args) throws IOException {

        NeuralNetworkStockPredictor predictor = new NeuralNetworkStockPredictor(
                5, StandaloneApplication.class
                .getResource("/input/USDJPY_M1_202003190000_202003192016.csv")
                .getPath());
        // 3 for HIGH
        // 4 for LOW
        // 5 for CLOSE
        // 6 for TICKVOL
        final int[] elements = new int[]{2, 3, 4, 5};

        predictor.prepareData(elements);

        System.out.println("Training starting");
        predictor.trainNetwork(elements);

        System.out.println("Testing network");
        predictor.testNetwork(elements);
    }
}
