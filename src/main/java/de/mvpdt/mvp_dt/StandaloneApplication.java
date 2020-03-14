package de.mvpdt.mvp_dt;

import de.mvpdt.mvp_dt.prediction.NeuralNetworkStockPredictor;

import java.io.IOException;

public class StandaloneApplication {
    public static void main(String[] args) throws IOException {

        NeuralNetworkStockPredictor predictor = new NeuralNetworkStockPredictor(
                5, StandaloneApplication.class
                .getResource("/input/USDJPY_202003120000_202003132142.csv")
                .getPath());
        // 2 for BID
        // 3 for ASK
        // 4 for LAST
        // 5 for VOLUME
        predictor.prepareData(2);

        System.out.println("Training starting");
        predictor.trainNetwork();

        System.out.println("Testing network");
        predictor.testNetwork();
    }
}
