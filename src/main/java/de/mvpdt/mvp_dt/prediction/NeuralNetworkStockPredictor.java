package de.mvpdt.mvp_dt.prediction;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import de.mvpdt.mvp_dt.exception.EmptyTokenException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
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

    private String learningDataFilePath = NeuralNetworkStockPredictor.class.getResource("/input/").getPath() + "learningData";
    private String predictedFilePath = NeuralNetworkStockPredictor.class.getResource("/input/").getPath() + "predicted.csv";
    private String neuralNetworkModelFilePath = "stockPredictor.nnet";

    private Date dateAndTimeForPrediction;
    private List<Double> normalizedValuesForPrediction = new LinkedList<>();
    //private Map<Integer, Double> predictedValuePerRow = new HashMap<>();
    private Map<Integer, LinkedList<Double>> lastSlidingWindowPerRowForPrediction = new HashMap<>();

    public NeuralNetworkStockPredictor(int slidingWindowSize,
                                       String rawDataFilePath) {
        this.rawDataFilePath = rawDataFilePath;
        this.slidingWindowSize = slidingWindowSize;
    }

    public void prepareData(final int ... elementIndex) {
        for(int index : elementIndex) {
            try {
                getMaxAndMinForNormalization(index);
                prepareDataAndWriteItIntoFile(index);
            } catch (EmptyTokenException e) {
                // ToDo: add log4j LOGGING
                e.printStackTrace();
            }
        }
    }

    private void getMaxAndMinForNormalization(final int elementIndex) throws EmptyTokenException {
        // Find the minimum and maximum values - needed for normalization
        try (final BufferedReader reader = new BufferedReader(new FileReader(rawDataFilePath));){
            String line;
            String lastToken = "";
            while ((line = reader.readLine()) != null) {
                if(!line.startsWith("<")) { // die Überschriftszeile soll nicht genommen werden
                    String[] tokens = line.split("\t");
                    String token = tokens[elementIndex];

                    // if the current token is null take the previous one (from the last day)
                    if (StringUtils.isEmpty(token)) {
                        token = lastToken;
                    } else {
                        lastToken = token;
                        if(StringUtils.isBlank(lastToken)) {
                            throw new EmptyTokenException("The first value cannot be null");
                        }
                    }

                    double currentValue = Double.parseDouble(token);
                    setMaxAndMin(currentValue);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMaxAndMin(double currentValue) {
        if (currentValue > max) {
            max = currentValue;
        }
        if (currentValue < min) {
            min = currentValue;
        }
    }

    private void getValuesForPrediction(final List<Double> allTokens) {
        final int size = allTokens.size();
        for(int i = 0; i < slidingWindowSize; i++) {
            final int index = size - (slidingWindowSize - i);
            final Double token = allTokens.get(index);
            System.out.println("token for prediction = " + token);
            normalizedValuesForPrediction.add(normalizeValue(token));
        }
    }

    private void calculateDateAndTimeForPrediction(final List<Date> allDates) {
        final Date lastDate = allDates.get(allDates.size() - 1);
        dateAndTimeForPrediction = DateUtils.addMinutes(lastDate, 1);
    }

    private void saveAllDates(final String[] tokens, final List<Date> allDates) {
        try {
            String date = tokens[0];
            String time = tokens[1];
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Locale.GERMAN);
            formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            Date dateAndTime = formatter.parse(date + ' ' + time);
            String formattedDateString = formatter.format(dateAndTime);
            allDates.add(dateAndTime);
        } catch(ParseException e) {
            e.printStackTrace();
        }
    }

    private void saveAllValues(final double currentValue, final List<Double> allTokens) {
        allTokens.add(currentValue);
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
    private void prepareDataAndWriteItIntoFile(final int ... elementIndex) throws EmptyTokenException {
        for(int index : elementIndex) {
            // Keep a queue with slidingWindowSize + 1 values
            LinkedList<Double> valuesQueue = new LinkedList<Double>();
            try (BufferedReader reader = new BufferedReader(new FileReader(rawDataFilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(learningDataFilePath + index + ".csv"))) {
                String line;
                String lastBid = "";
                List<Date> allDates = new LinkedList<>();
                List<Double> allTokens = new LinkedList<>();
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("<")) { // die Überschriftszeile soll nicht genommen werden
                        String[] tokens = line.split("\t");

                        // Datum und Zeit auslesen und in einer Instanzveriablen abspeichern
                        saveAllDates(tokens, allDates);

                        String bid = tokens[index];

//                    // if the current token is null take the previous one (from the last day)
                        if (StringUtils.isEmpty(bid)) {
                            bid = lastBid;
                        } else {
                            lastBid = bid;
                            if (StringUtils.isBlank(lastBid)) {
                                throw new EmptyTokenException("The first value cannot be null");
                            }
                        }

                        double crtValue = Double.parseDouble(bid);
                        // Normalize values and add it to the queue
                        double normalizedValue = normalizeValue(crtValue);
                        valuesQueue.add(normalizedValue);

                        createAndWriteSlidingWindowOfValues(valuesQueue, writer, index);

                        // saveAllValues(crtValue, allTokens);
                    }
                }
                calculateDateAndTimeForPrediction(allDates);
                // getValuesForPrediction(allTokens);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * For training
     * @param valuesQueue
     * @param writer
     * @param elementIndex
     * @throws IOException
     */
    private void createAndWriteSlidingWindowOfValues(LinkedList<Double> valuesQueue, BufferedWriter writer, int elementIndex) throws IOException {
        // ToDo: For loop for all indexes
        if (valuesQueue.size() == slidingWindowSize + 1) {
            String valueLine = valuesQueue.toString().replaceAll(
                    "\\[|\\]", "");
            writer.write(valueLine);
            writer.newLine();
            // put last sliding window of a file (last 5 values) with row index into this HashMap
            // to use it for prediction in the method #testNetwork()
            lastSlidingWindowPerRowForPrediction.put(elementIndex, valuesQueue);
            // Remove the first element in queue to make place for a new one
            valuesQueue.removeFirst();
        }
    }

    double normalizeValue(double input) {
        return (input - min) / (max - min) * 0.8 + 0.1;
    }

    double deNormalizeValue(double input) {
        return min + (input - 0.1) * (max - min) / 0.8;
    }

    public void trainNetwork(final int ... elementIndex) throws IOException {
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

        DataSet trainingSet = loadTraininigData(elementIndex);
        neuralNetwork.learn(trainingSet);
        neuralNetwork.save(neuralNetworkModelFilePath);
    }

    DataSet loadTraininigData(int ... elementIndex) throws IOException {
        DataSet trainingSet = new DataSet(slidingWindowSize, 1);
        for(int index : elementIndex) {
            BufferedReader reader = new BufferedReader(new FileReader(learningDataFilePath + index + ".csv"));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");

                    double trainValues[] = new double[slidingWindowSize];
                    for (int i = 0; i < slidingWindowSize; i++) {
                        trainValues[i] = Double.valueOf(tokens[i]);
                    }
                    double expectedValue[] = new double[]{Double
                            .valueOf(tokens[slidingWindowSize])};
                    trainingSet.addRow(new DataSetRow(trainValues, expectedValue));
                    System.out.println("Index: " + index + " Load Taining Expected Value: " + expectedValue[0] + "  trainValue:" + trainValues[0]);
                }
            } finally {
                reader.close();
            }
        }
        return trainingSet;
    }

    public void testNetwork(final int ... elementIndex) {
        NeuralNetwork neuralNetwork = NeuralNetwork
                .createFromFile(neuralNetworkModelFilePath);

        // max 4 values: <HIGH>	<LOW> <CLOSE> <TICKVOL>
        final Map<Integer, Double> result = new HashMap<>(4);

        for(int index : elementIndex) {
            LinkedList<Double> normalizedValuesForPrediction = lastSlidingWindowPerRowForPrediction.get(index);
            Double[] doubles = normalizedValuesForPrediction.toArray(new Double[this.normalizedValuesForPrediction.size()]);

            neuralNetwork.setInput(Stream.of(doubles).mapToDouble(value -> value.doubleValue()).toArray());

            neuralNetwork.calculate();
            double[] networkOutput = neuralNetwork.getOutput();
            // System.out.println("Expected value  : 2066.96");
            double predictedValue = deNormalizeValue(networkOutput[0]);
            System.out.println("Index: " + index + ", Predicted value : "
                    + predictedValue);

            result.put(index, predictedValue);
        }

        savePredictionResults(result);
    }

    private void savePredictionResults(Map<Integer, Double> result) {
        String pattern = "yyyy.MM.dd\tHH:mm:ss";
        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date according to the chosen pattern
        DateFormat df = new SimpleDateFormat(pattern);
        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        String dateAsString = df.format(dateAndTimeForPrediction);
        final StringBuilder predicted = new StringBuilder(dateAsString);
        result.forEach((key, value) -> predicted.append("\t" + value));
        System.out.println("predicted = " + predicted);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(predictedFilePath));
            writer.write(predicted.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}