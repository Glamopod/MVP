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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

@Service
public class NetworkService {
    private String neuralNetworkModelFilePath = "stockPredictor.nnet";
    private int slidingWindowSize = 3;
    private double max = 0;
    private double min = Double.MAX_VALUE;

    public void trainNetwork(final LinkedList<Double> rawValuesList, int slidingWindowSize, final boolean createNewNN) throws IOException {
        this.slidingWindowSize = slidingWindowSize;
        NeuralNetwork<BackPropagation> neuralNetwork = createOrLoadNN(createNewNN);

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

    private NeuralNetwork<BackPropagation> createOrLoadNN(boolean createNewNN) {
        NeuralNetwork<BackPropagation> neuralNetwork = null;
        if (createNewNN) {
            try {
                Files.deleteIfExists(Paths.get(neuralNetworkModelFilePath));
            } catch (IOException e) {
                System.err.println("Error while removing existing NN");
                e.printStackTrace();
            }
            //The number of hidden neurons should be between the size of the input layer and the size of the output layer.
            // The number of hidden neurons should be 2/3 the size of the input layer, plus the size of the output layer.
            // The number of hidden neurons should be less than twice the size of the input layer.Jun 1, 2017
            neuralNetwork = new MultiLayerPerceptron( // new NN
                    slidingWindowSize,
                    2*(slidingWindowSize*(2/3)+1),
                    2*(slidingWindowSize*(3/3)+1),
                    1); // new NN

            int maxIterations = 10000;
            double learningRate =0.5;
            double maxError = 0.000000000001;
            SupervisedLearning learningRule = neuralNetwork.getLearningRule(); // new NN
            learningRule.setMaxError(maxError); // new NN
            learningRule.setLearningRate(learningRate); // new NN
            learningRule.setMaxIterations(maxIterations); // new NN
            learningRule.addListener(new LearningEventListener() { // new NN
                public void handleLearningEvent(LearningEvent learningEvent) { // new NN
                    SupervisedLearning rule = (SupervisedLearning) learningEvent // new NN
                            .getSource(); // new NN
// DO NOT USE                System.out.println("Network error for interation " // new NN
// DO NOT USE                        + rule.getCurrentIteration() + ": " // new NN
// DO NOT USE                        + rule.getTotalNetworkError()); // new NN
                } // new NN
            }); // new NN
        } else {
            neuralNetwork = NeuralNetwork // load existing NN
                    .createFromFile(neuralNetworkModelFilePath); // load existing NN
        }
        return neuralNetwork;
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
