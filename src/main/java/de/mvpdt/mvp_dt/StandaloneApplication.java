package de.mvpdt.mvp_dt;

import de.mvpdt.mvp_dt.prediction.NeuralNetworkStockPredictor;

import java.io.IOException;

public class StandaloneApplication {
    public static void main(String[] args) throws IOException {

        NeuralNetworkStockPredictor predictor = new NeuralNetworkStockPredictor(
                5, StandaloneApplication.class
                .getResource("/input/rawTrainingData.csv")
                .getPath());
        predictor.prepareData();

        System.out.println("Training starting");
        predictor.trainNetwork();

        System.out.println("Testing network");
        predictor.testNetwork();
    }
}
