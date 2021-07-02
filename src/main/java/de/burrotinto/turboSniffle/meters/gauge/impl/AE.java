//package de.burrotinto.turboSniffle.meters.gauge.impl;
//
//import de.burrotinto.turboSniffle.meters.gauge.Gauge;
//import de.burrotinto.turboSniffle.meters.gauge.test.Pointer;
//import lombok.SneakyThrows;
//import lombok.val;
//import org.apache.commons.lang3.tuple.ImmutablePair;
//import org.apache.commons.lang3.tuple.Pair;
//import org.datavec.image.loader.NativeImageLoader;
//import org.nd4j.linalg.activations.Activation;
//import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator;
//import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
//import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
//import org.deeplearning4j.nn.api.OptimizationAlgorithm;
//import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
//import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher;
//import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher;
//import org.nd4j.linalg.indexing.NDArrayIndex;
//import org.nd4j.linalg.learning.config.AdaGrad;
//import org.deeplearning4j.nn.conf.layers.DenseLayer;
//import org.deeplearning4j.nn.conf.layers.OutputLayer;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.deeplearning4j.nn.weights.WeightInit;
//import org.deeplearning4j.optimize.api.IterationListener;
//import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.dataset.DataSet;
//import org.nd4j.linalg.factory.Nd4j;
//import org.nd4j.linalg.lossfunctions.LossFunctions;
//
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Random;
//
//public class AE {
//
//    @SneakyThrows
//    public static void main(String[] args) {
//        val conf = new NeuralNetConfiguration.Builder()
//                .seed(12345)
//                .weightInit(WeightInit.XAVIER)
//                .updater(new AdaGrad())
//                .activation(Activation.RELU)
//                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//                .l2(0.0001)
//                .list()
//                .layer(0, new DenseLayer.Builder().nIn((int) Gauge.DEFAULT_SIZE.height * (int) Gauge.DEFAULT_SIZE.width).nOut(250)
//                        .build())
//                .layer(1, new DenseLayer.Builder().nIn(720).nOut(360)
//                        .build())
//                .layer(2, new DenseLayer.Builder().nIn(360).nOut(720)
//                        .build())
//                .layer(3, new OutputLayer.Builder().nIn(720).nOut((int) Gauge.DEFAULT_SIZE.height * (int) Gauge.DEFAULT_SIZE.width)
//                        .lossFunction(LossFunctions.LossFunction.MSE)
//                        .build())
//                .build();
//
//        val net = new MultiLayerNetwork(conf);
//        net.setListeners(new ScoreIterationListener(1));
//
//        val iter = new MnistDataSetIterator(100, 50000, false);
//
//        NativeImageLoader nil = new NativeImageLoader();
//        INDArray image = nil.asMatrix(cvImage).div;
//
//        DataSetFetcher dsf = new BaseDataFetcher() {
//            @Override
//            public void fetch(int i) {
//
//            }
//        };
//        DataSetIterator dsi = new BaseDatasetIterator();
//        val featuresTrain = new ArrayList<INDArray>();
//        val featuresTest = new ArrayList<INDArray>();
//        val labelsTest = new ArrayList<INDArray>();
//
//        val rand = new Random(12345);
//
//        while (iter.hasNext()) {
//            val next = iter.next();
//            val split = next.splitTestAndTrain(80, rand);  //80/20 split (from miniBatch = 100)
//            featuresTrain.add(split.getTrain().getFeatures());
//            val dsTest = split.getTest();
//            featuresTest.add(dsTest.getFeatures());
//            val indexes = Nd4j.argMax(dsTest.getLabels(), 1); //Convert from one-hot representation -> index
//            labelsTest.add(indexes);
//        }
//
//        val nEpochs = 30;
//        for (int i = 0; i < nEpochs; i++) {
//            featuresTrain.forEach(indArray -> net.fit(indArray, indArray));
//            System.out.println("Epoch " + i + " complete");
//        }
////Evaluate the model on the test data
////Score each example in the test set separately
////Compose a map that relates each digit to a list of (score, example) pairs
////Then find N best and N worst scores per digit
//        val listsByDigit = new HashMap<Integer, ArrayList<Pair<Double, INDArray>>>();
////
////        (0 to 9).foreach{ i => listsByDigit.put(i, new util.ArrayList[Pair[Double, INDArray]]) }
////
////        (0 to featuresTest.size-1).foreach{ i =>
////            val testData = featuresTest.get(i)
////            val labels = labelsTest.get(i)
////
////            (0 to testData.rows-1).foreach{ j =>
////                val example = testData.getRow(j, true)
////                val digit = labels.getDouble(j).toInt
////                val score = net.score(new DataSet(example, example))
////                // Add (score, example) pair to the appropriate list
////                val digitAllPairs = listsByDigit.get(digit)
////                digitAllPairs.add(new ImmutablePair[Double, INDArray](score, example))
////            }
////        }
////
//////Sort each list in the map by score
////        val c = new Comparator[Pair[Double, INDArray]]() {
////            override def compare(o1: Pair[Double, INDArray],
////            o2: Pair[Double, INDArray]): Int =
////                    java.lang.Double.compare(o1.getLeft, o2.getLeft)
////        }
////
////        listsByDigit.values().forEach(digitAllPairs => Collections.sort(digitAllPairs, c))
////
//////After sorting, select N best and N worst scores (by reconstruction error) for each digit, where N=5
////        val best = new util.ArrayList[INDArray](50)
////        val worst = new util.ArrayList[INDArray](50)
////
////        (0 to 9).foreach{ i =>
////            val list = listsByDigit.get(i)
////
////            (0 to 4).foreach{ j=>
////                best.add(list.get(j).getRight)
////                worst.add(list.get(list.size - j - 1).getRight)
////            }
////        }
//    }
//
//    public class MyCustomIterator implements DataSetIterator {
//        private INDArray inputs, desiredOutputs;
//        private int itPosition = 0; // the iterator position in the set.
//
//        public MyCustomIterator(float[] inputsArray,
//                                float[] desiredOutputsArray,
//                                int numSamples,
//                                int inputDim,
//                                int outputDim) {
//            inputs = Nd4j.create(inputsArray, new int[]{numSamples, inputDim});
//            desiredOutputs = Nd4j.create(desiredOutputsArray, new int[]{numSamples, outputDim});
//        }
//
//        public DataSet next(int num) {
//            // get a view containing the next num samples and desired outs.
//            INDArray dsInput = inputs.get(
//                    NDArrayIndex.interval(itPosition, itPosition + num),
//                    NDArrayIndex.all());
//            INDArray dsDesired = desiredOutputs.get(
//                    NDArrayIndex.interval(itPosition, itPosition + num),
//                    NDArrayIndex.all());
//
//            itPosition += num;
//
//            return new DataSet(dsInput, dsDesired);
//        }
//
//        // implement the remaining virtual methods...
//
//    }
//}
