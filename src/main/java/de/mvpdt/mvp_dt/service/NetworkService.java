package de.mvpdt.mvp_dt.service;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Service
public class NetworkService {
    private String neuralNetworkModelFilePath = "stockPredictor.nnet";
    private int slidingWindowSize = 5;
    private double max = 0;
    private double min = Double.MAX_VALUE;

    public void trainNetwork(final LinkedList<Double> rawValuesList) throws IOException {
        // ToDo: For loop for all indexes
        NeuralNetwork<BackPropagation> neuralNetwork = new MultiLayerPerceptron(
                slidingWindowSize, 2 * slidingWindowSize + 1, 1);

        int maxIterations = 1000;
        double learningRate = 0.5;
        double maxError = 0.00001;
        SupervisedLearning learningRule = neuralNetwork.getLearningRule();
        learningRule.setMaxError(maxError);
        learningRule.setLearningRate(learningRate);
        learningRule.setMaxIterations(maxIterations);
        learningRule.addListener(new LearningEventListener() {
            public void handleLearningEvent(LearningEvent learningEvent) {
                SupervisedLearning rule = (SupervisedLearning) learningEvent
                        .getSource();
                // System.out.println("Network error for interation "
                //         + rule.getCurrentIteration() + ": "
                //         + rule.getTotalNetworkError());
            }
        });

        final DataSet trainingSet = createTrainingData(rawValuesList);
        System.out.println("Rows in the training set = " + trainingSet.getRows().size());
        System.out.println("Rows = " + trainingSet.getRows().toString());
        final int lastRowIndex = trainingSet.size() - 1;
        final DataSetRow lastRow = trainingSet.getRowAt(lastRowIndex);
        trainingSet.removeRowAt(lastRowIndex);
        neuralNetwork.learn(trainingSet);
        neuralNetwork.save(neuralNetworkModelFilePath);
        testNetwork(lastRow);

//      System.out.println(trainingSet.getRows().toString().replace(",", "\n"));
        System.out.println();
    }

    private void setMaxAndMin(final LinkedList<Double> rawValuesList) {
        max = rawValuesList.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
        min = rawValuesList.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
    }

    private DataSet createTrainingData(final LinkedList<Double> rawValuesList) {
        setMaxAndMin(rawValuesList);
        DataSet trainingSet = new DataSet(slidingWindowSize, 1);
        while (!rawValuesList.isEmpty()) {
            double trainValues[] = new double[slidingWindowSize];
            for (int counter = 0; counter < slidingWindowSize; counter++) {
                if (rawValuesList.size() > counter)
                    trainValues[counter] = normalizeValue(rawValuesList.get(counter));
            }
            if (rawValuesList.size() > slidingWindowSize) {
                double expectedValue[] = new double[]{normalizeValue(rawValuesList.get(slidingWindowSize))};
                trainingSet.addRow(new DataSetRow(trainValues, expectedValue));
            }
            rawValuesList.removeFirst();
        }
        return trainingSet;
    }

    public void testNetwork(final DataSetRow lastRow) {
        NeuralNetwork neuralNetwork = NeuralNetwork
                .createFromFile(neuralNetworkModelFilePath);

        neuralNetwork.setInput(lastRow.getInput());
        neuralNetwork.calculate();
        double[] networkOutput = neuralNetwork.getOutput();
        final double expectedValue = deNormalizeValue(lastRow.getDesiredOutput()[0]);
        System.out.println("Expected value = " + expectedValue);
        double predictedValue = deNormalizeValue(networkOutput[0]);
        System.out.println("Predicted value = " + predictedValue);
        System.out.println("Difference = " + (expectedValue - predictedValue));
        // result.put(index, predictedValue);


        //savePredictionResults(result);
    }

    double normalizeValue(double input) {
        return (input - min) / (max - min) * 0.8 + 0.1;
    }

    double deNormalizeValue(double input) {
        return min + (input - 0.1) * (max - min) / 0.8;
    }
}
