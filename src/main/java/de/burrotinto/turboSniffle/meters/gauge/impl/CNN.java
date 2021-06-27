//package de.burrotinto.turboSniffle.meters.gauge.impl;
//
//import lombok.val;
//import org.bytedeco.javacpp.opencv_core;
//import org.datavec.image.loader.NativeImageLoader;
//import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
//import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
//import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
//import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
//import org.deeplearning4j.nn.conf.layers.OutputLayer;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.deeplearning4j.nn.weights.WeightInit;
//import org.nd4j.linalg.activations.Activation;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.dataset.api.DataSet;
//import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
//import org.nd4j.linalg.lossfunctions.LossFunctions;
//import org.opencv.core.Mat;
//
//import java.io.IOException;
//
//public class CNN {
//
//    public static void main(String[] args) throws IOException {
//        MultiLayerConfiguration configuration
//                = new NeuralNetConfiguration.Builder()
//                .iterations(1000)
//                .activation(Activation.RELU)
//                .weightInit(WeightInit.XAVIER)
//                .learningRate(0.1)
//                .regularization(true).l2(0.0001)
//                .list()
//                .layer(0, new ConvolutionLayer.Builder().nIn(512 * 512).nOut(64).build())
//                .layer(1, new ConvolutionLayer.Builder().nIn(64).nOut(16).build())
////                .layer(2, new ConvolutionLayer.Builder().nIn(3).nOut(3).build())
////                .layer(3, new ConvolutionLayer.Builder().nIn(3).nOut(3).build())
////                .layer(4, new ConvolutionLayer.Builder().nIn(3).nOut(3).build())
////                .layer(5, new ConvolutionLayer.Builder().nIn(3).nOut(3).build())
////                .layer(6, new ConvolutionLayer.Builder().nIn(3).nOut(3).build())
////                .layer(7, new ConvolutionLayer.Builder().nIn(3).nOut(3).build())
//                .layer(2, new OutputLayer.Builder(
//                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//                        .activation(Activation.SOFTMAX)
//                        .nIn(16).nOut(360).build())
//                .backprop(true).pretrain(false)
//                .build();
//
//        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
//        model.init();
//
//
//        val x = new NativeImageLoader();
//        x.asMatrix(new Mat());
//
//        DataSetIterator iterator = new (
//                x, 150, 512*512, 360);
//
//        iterator.
//        INDArray trainingData;
//        trainingData.
//        model.fit(trainingData);
//    }
//}
