package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.GaugeOnePointerLearningDataset;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.TrainingSet;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;


public class AutoEncoderGauge extends Gauge {

    private final Size AUTOENCODER_INPUT_SIZE = Gauge.DEFAULT_SIZE; //Eingabeschicht Autoencoder


    protected final HashMap<RotatedRect, Double> labelScale = new HashMap<>();

    private double[] pointerAngel = null;
    protected Mat idealisierteDarstellung = null;
    private int hiddenLayer; //Genauigkeit des verwendeten Autoencoders

    private TrainingSet trainingSet = null;

    AutoEncoderGauge(Gauge gauge) throws NotGaugeWithPointerException {
        this(gauge, GaugeOnePointerLearningDataset.get(), 10);
    }

    AutoEncoderGauge(Gauge gauge, TrainingSet trainingSet, int hiddenLayer) throws NotGaugeWithPointerException {
        super(gauge.source, gauge.canny, gauge.otsu);
        this.hiddenLayer = hiddenLayer;
        this.trainingSet = trainingSet;

        setHeatMap(gauge.getHeatMap());

        // Idealisierte Darstellung ist OTSU mit Weißem Skalenfeld
        Mat ideal = otsu.clone();

        //Wenn Wenn es mehr Schwarz als weiß gibt müssen farben getauscht werden
        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);

        List<Pixel> pixels = Helper.getAllPixel(ideal, mask);
        if (pixels.stream().filter(pixel -> pixel.color == 0).count() > pixels.size() / 2) {
            Core.bitwise_not(ideal, ideal);
        }
        setIdealisierteDarstellung(ideal);
    }

    protected void setIdealisierteDarstellung(Mat idealisierteDarstellung) {
        this.idealisierteDarstellung = idealisierteDarstellung;
    }


    /**
     * Unsupervised learning mit Boolean Autoencoder
     *
     * @return
     */
    public double[] getPointerAngel() {
        if (pointerAngel == null || pointerAngel.length == 0) {
            Pair<double[], Integer> min = null;
            Mat minTV = null;

            Mat eingangsVektor = new Mat();

            Imgproc.resize(getIdealisierteDarstellung(), eingangsVektor, AUTOENCODER_INPUT_SIZE);

            List<Pair<Mat, double[]>> ausgangsVektoren = trainingSet.getTrainingset(AUTOENCODER_INPUT_SIZE, hiddenLayer);

            Imgcodecs.imwrite("data/out/aePointer.png", eingangsVektor);

            for (int i = 0; i < ausgangsVektoren.size(); i++) {
                Mat konjunktion = new Mat();
                Core.bitwise_or(eingangsVektor, ausgangsVektoren.get(i).p1, konjunktion);
//                int p = Core.countNonZero(konjunktion);

                int p = (int)(Core.sumElems(konjunktion).val[0] / 255);
                if (min == null || min.p2 > p) {
                    min = new Pair<>(ausgangsVektoren.get(i).p2, p);
                    minTV = ausgangsVektoren.get(i).p1;
//                    System.out.println(konjunktion.dump());
//                    System.out.println(p);
//                    HighGui.imshow("min", konjunktion);
//                    HighGui.waitKey(100);
                }


//                System.out.println(min.p2 +" " + p);
            }

//            Imgcodecs.imwrite("data/ae/kurskreisel/"+ min.p1[0] +"_"+ System.currentTimeMillis() + ".png", eingangsVektor);

            pointerAngel = min.p1;
        }
        return pointerAngel;

    }


    /**
     * Malt die erkannten Skalenmarkierungen und den Zeiger auf das Eingabe Mat
     *
     * @param drawing
     * @return
     */
    public Mat getDrawing(Mat drawing) {
        if (drawing == null) {
            drawing = Mat.zeros(Gauge.DEFAULT_SIZE, Gauge.TYPE);
        }
        Mat finalDrawing = drawing;
        Imgproc.cvtColor(drawing, drawing, Imgproc.COLOR_GRAY2RGB);


        for (int i = 0; i < getPointerAngel().length; i++) {
            Imgproc.arrowedLine(finalDrawing, getCenter(), poolarZuBildkoordinaten(getPointerAngel()[i], (getRadius() - 10) / (i + 1)), new Scalar(0, 69, 255), 5);
        }

        return finalDrawing;
    }

    public Mat getIdealisierteDarstellung() {
        return idealisierteDarstellung;
    }

}
