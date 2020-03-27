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
import java.util.LinkedList;

@Service
public class NetworkService {
    private String neuralNetworkModelFilePath = "stockPredictor.nnet";
    private int slidingWindowSize = 5;

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
                System.out.println("Network error for interation "
                        + rule.getCurrentIteration() + ": "
                        + rule.getTotalNetworkError());
            }
        });

        final DataSet trainingSet = createTrainingData(rawValuesList);
//        DataSet trainingSet = loadTraininigData(rawValuesList);
//        neuralNetwork.learn(trainingSet);
//        neuralNetwork.save(neuralNetworkModelFilePath);
        System.out.println(trainingSet.getRows().size());
        System.out.println(trainingSet.getRows().toString());
//      System.out.println(trainingSet.getRows().toString().replace(",", "\n"));
    }

    private DataSet loadTraininigData(final LinkedList<Double> rawValuesList) throws IOException {
        DataSet trainingSet = new DataSet(slidingWindowSize, 1);
        int counter = 0;
        for (Double rawData : rawValuesList) {
            double trainValues[] = new double[slidingWindowSize];
            if (counter < slidingWindowSize) {
                trainValues[counter] = rawData;
            } else {
                double expectedValue[] = new double[]{rawData}; // expected rawValue
                trainingSet.addRow(new DataSetRow(trainValues, expectedValue));
                counter = 0;
            }
            counter++;
        }
        return trainingSet;
    }

    private DataSet createTrainingData(final LinkedList<Double> rawValuesList) {
        DataSet trainingSet = new DataSet(slidingWindowSize, 1);
        while (!rawValuesList.isEmpty()) {
            double trainValues[] = new double[slidingWindowSize];
            for (int counter = 0; counter < slidingWindowSize; counter++) {
                if (rawValuesList.size() > counter)
                    trainValues[counter] = rawValuesList.get(counter);
            }
            if (rawValuesList.size() > slidingWindowSize) {
                double expectedValue[] = new double[]{rawValuesList.get(slidingWindowSize)};
                trainingSet.addRow(new DataSetRow(trainValues, expectedValue));
            }
            rawValuesList.removeFirst();
        }
        return trainingSet;
    }
}
