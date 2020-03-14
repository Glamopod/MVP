package de.mvpdt.mvp_dt.prediction;

import java.io.*;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;

/**
 * https://technobium.com/stock-market-prediction-using-neuroph-neural-networks/
 */
public class NeuralNetworkStockPredictor {

    private int slidingWindowSize;
    private double max = 0;
    private double min = Double.MAX_VALUE;
    private String rawDataFilePath;

    private String learningDataFilePath = NeuralNetworkStockPredictor.class.getResource("/input/").getPath() + "learningData.csv";
    private String neuralNetworkModelFilePath = "stockPredictor.nnet";

    public NeuralNetworkStockPredictor(int slidingWindowSize,
                                       String rawDataFilePath) {
        this.rawDataFilePath = rawDataFilePath;
        this.slidingWindowSize = slidingWindowSize;
    }

    public void prepareData(final int elementIndex) {
        getMaxAndMin(elementIndex);
        writePreparedDataIntoFile(elementIndex);
    }

    private void getMaxAndMin(final int elementIndex) {
        // Find the minimum and maximum values - needed for normalization
        try (final BufferedReader reader = new BufferedReader(new FileReader(rawDataFilePath));){
            String line;
            String lastBid = "";
            while ((line = reader.readLine()) != null) {
                if(!line.startsWith("<")) { // die Überschriftszeile soll nicht genommen werden
                    String[] tokens = line.split("\t");
                    String bid = tokens[elementIndex];
                    if (StringUtils.isEmpty(bid)) {
                        bid = lastBid;
                    } else {
                        lastBid = bid;
                    }
                    double crtValue = Double.parseDouble(bid);
                    if (crtValue > max) {
                        max = crtValue;
                    }
                    if (crtValue < min) {
                        min = crtValue;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method gets data from the file with currency rates (e.g. USDJPY_202003120000_202003132142.csv)
     * Each line of this file contains several tab separated elements, e.g.
     * 2020.03.12	00:00:00.094	104.429	104.469
     * This method get the element with an as a parameter passed index on each line,
     * normalizes it to a value between 0 and 1 and puts the number of such normalized values,
     * which is equals to the value of the slidingWindowSize variable as a new line into the file,
     * specified in the variable learningDataFilePath (e.g. learningData.csv).
     * An example for such a line:
     * 0.3010125632852059, 0.3028126757922377, 0.3029626851678222, 0.3010125632852059, 0.3023626476654797, 0.30191261953872184
     * According to the slide window principle
     * the first line contains elements from line 1 till 'slidingWindowsSize' + 1 of the currency rates file,
     * the second - lines from 2 till 'slidingWindowsSize' + 2
     * the third - lines from 3 till 'slidingWindowsSize' + 3
     * and so on.
     *
     * @param elementIndex row index for BID, ASK, LAST or VOLUME
     */
    private void writePreparedDataIntoFile(final int elementIndex) {
        // Keep a queue with slidingWindowSize + 1 values
        LinkedList<Double> valuesQueue = new LinkedList<Double>();
        try (BufferedReader reader = new BufferedReader(new FileReader(rawDataFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(learningDataFilePath))){
            String line;
            String lastBid = "";
            while ((line = reader.readLine()) != null) {
                if(!line.startsWith("<")) { // die Überschriftszeile soll nicht genommen werden
                    String[] tokens = line.split("\t");
                    String bid = tokens[elementIndex];
                    if (StringUtils.isEmpty(bid)) {
                        bid = lastBid;
                    } else {
                        lastBid = bid;
                    }
                    double crtValue = Double.parseDouble(bid);
                    // Normalize values and add it to the queue
                    double normalizedValue = normalizeValue(crtValue);
                    valuesQueue.add(normalizedValue);

                    if (valuesQueue.size() == slidingWindowSize + 1) {
                        String valueLine = valuesQueue.toString().replaceAll(
                                "\\[|\\]", "");
                        writer.write(valueLine);
                        writer.newLine();
                        // Remove the first element in queue to make place for a new one
                        valuesQueue.removeFirst();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    double normalizeValue(double input) {
        return (input - min) / (max - min) * 0.8 + 0.1;
    }

    double deNormalizeValue(double input) {
        return min + (input - 0.1) * (max - min) / 0.8;
    }

    public void trainNetwork() throws IOException {
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

        DataSet trainingSet = loadTraininigData(learningDataFilePath);
        neuralNetwork.learn(trainingSet);
        neuralNetwork.save(neuralNetworkModelFilePath);
    }

    DataSet loadTraininigData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        DataSet trainingSet = new DataSet(slidingWindowSize, 1);

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");

                double trainValues[] = new double[slidingWindowSize];
                for (int i = 0; i < slidingWindowSize; i++) {
                    trainValues[i] = Double.valueOf(tokens[i]);
                }
                double expectedValue[] = new double[] { Double
                        .valueOf(tokens[slidingWindowSize]) };
                trainingSet.addRow(new DataSetRow(trainValues, expectedValue));
                System.out.println("Load Taining Expected Value: " + expectedValue[0]+ "  trainValue:" + trainValues[0]);
            }
        } finally {
            reader.close();
        }
        return trainingSet;
    }

    public void testNetwork() {
        NeuralNetwork neuralNetwork = NeuralNetwork
                .createFromFile(neuralNetworkModelFilePath);
        neuralNetwork.setInput(normalizeValue(2056.15),
                normalizeValue(2061.02), normalizeValue(2086.24),
                normalizeValue(2067.89), normalizeValue(2059.69));

        neuralNetwork.calculate();
        double[] networkOutput = neuralNetwork.getOutput();
        System.out.println("Expected value  : 2066.96");
        System.out.println("Predicted value : "
                + deNormalizeValue(networkOutput[0]));
    }
}