package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.meters.Measuring;
import lombok.val;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public abstract class CircleGauge implements Measuring {
    private static final Size SIZE = new Size(512, 512);
    private final int thresholdCanny = 255/3;
    private Mat fittedSrc;
    protected int threshold;

    protected List<MatOfPoint> contours = new ArrayList<>();

    private RotatedRect gaugeElipse;
    private Mat gaugeMat;


    public CircleGauge(int threshold) {
        this.threshold = threshold;
    }

    protected Mat getRefittetSrcMat(){
        return fittedSrc;
    }

    @Override
    public void update(Mat mat) {
        fittedSrc = mat.clone();
        val edgeDedected = getSobel(mat);
        val lines = getLineDedection(edgeDedected);



        //BA

        val input = fittedSrc.clone();
        val ca = new Mat(input.size(), input.type());
        val sobel = getSobel(input);

        Imgproc.Canny(mat, ca, threshold, threshold * 2);

        Imgcodecs.imwrite("eingabe.jpg",input);
        Imgcodecs.imwrite("Canny.jpg",ca);
        Imgcodecs.imwrite("Sobel.jpg",sobel);

        Mat out = new Mat(edgeDedected.size(),edgeDedected.type());
        for (int x = 0; x < lines.rows(); x++) {
            double[] l = lines.get(x, 0);
            Imgproc.line(out, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255, 255, 255), 3, Imgproc.LINE_8, 0);
        }





        val removedLines = removeLines(edgeDedected, lines);
        HighGui.imshow("asd",out);
        HighGui.imshow("asdvcbvb",removedLines);
        HighGui.waitKey();

        val elipse = getGreatestElipse(removedLines);

        gaugeMat = Mat.zeros(mat.size(), mat.type());
        Imgproc.ellipse(gaugeMat, elipse, new Scalar(255, 255, 0), 10);

        //initialisation
        int rad = (int) Math.max(elipse.size.width, elipse.size.height) / 2;
        val rect = new Rect((int) elipse.center.x - rad, (int) elipse.center.y - rad, rad * 2, rad * 2);


        val drawing = edgeDedected.clone();
        Imgproc.ellipse(drawing,elipse,new Scalar(255,255,255),10);
        Imgproc.rectangle(drawing,elipse.boundingRect(),new Scalar(255,255,255));
        Imgproc.resize(drawing, drawing, new Size(512, 512));


//        HighGui.imshow("boundingRect",drawing);
//        HighGui.waitKey();


        Imgproc.resize(gaugeMat.submat(rect), gaugeMat, getSize());
        Imgproc.resize(mat.submat(rect), fittedSrc,getSize());

//        Imgproc.drawMarker(fittedSrc,new Point(drawing.width()/2,drawing.height()/2), getColor());
//        Imgcodecs.imwrite(System.currentTimeMillis()+".jpg",fittedSrc);

        gaugeElipse = getGreatestElipse(gaugeMat);
    }

    public RotatedRect getGauge() {
        return gaugeElipse;
    }

    public Mat getGaugeMat() {
        val drawing = Mat.zeros(gaugeMat.size(), gaugeMat.type());

        Imgproc.ellipse(drawing, gaugeElipse, new Scalar(125, 125, 0), 10);
        return drawing;
    }

    public Point getCenter() {
        return gaugeElipse.center;
    }

    public double getDurchmesser() {
        return Math.max(gaugeElipse.size.width, gaugeElipse.size.height);
    }

    public static Mat getEdgeDedectionCanny(Mat mat, int threshold){
        //Canny and Contours finding
        Mat cannyOutput = new Mat();
        Imgproc.Canny(mat, cannyOutput, threshold, threshold * 2);
        return cannyOutput;
    }

    protected Mat getEdgeDedectionCanny(Mat mat) {
        return getEdgeDedectionCanny(mat,threshold);
    }

    private Mat getEdgeDedectionCannyGauge(Mat mat){
        return getEdgeDedectionCanny(mat,thresholdCanny );
    }


    protected Mat getLineDedection(Mat mat) {
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(mat, linesP, 1, Math.PI / 180, 255 / 4, mat.size().height/5.0, mat.size().height/25.5); // runs the actual detection

        return linesP;
    }

    protected RotatedRect getGreatestElipse(Mat canny) {
        val contours = new ArrayList<MatOfPoint>();
        var hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Check und initialisierung der AnzeigeDabei wer
        RotatedRect[] minEllipse = new RotatedRect[contours.size()];

        int indexDisplay = 0;



        val maxQuadrat = new Rect(0,0,canny.width(),canny.height());

        for (int i = 0; i < contours.size(); i++) {
            minEllipse[i] = new RotatedRect();
            if (contours.get(i).rows() > 5) {
                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));
            }


            val radius = (int) Math.max(minEllipse[i].size.width,minEllipse[i].size.height)/2;

            if (minEllipse[i].size.area() > minEllipse[indexDisplay].size.area()
                    && minEllipse[i].center.x - radius >= 0
                    && minEllipse[i].center.y - radius >= 0
                    && minEllipse[i].center.x + radius < canny.width()
                    && minEllipse[i].center.y + radius < canny.height()
            ) {
                indexDisplay = i;
            }
        }

        return minEllipse[indexDisplay];

    }

    private Mat removeLines(Mat canny, Mat lines) {
        // Draw the lines
        Mat out = canny.clone();
        for (int x = 0; x < lines.rows(); x++) {
            double[] l = lines.get(x, 0);
            Imgproc.line(out, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_8, 0);
        }
        return out;
    }

    protected boolean isRectInsideRect(Rect a, Rect b){
        return true;
    }
    protected MatOfPoint2f getContourOfRect(Rect rect) {
        return new MatOfPoint2f(new Point(rect.x, rect.y),
                new Point(rect.x + rect.width, rect.y),
                new Point(rect.x, rect.y + rect.height),
                new Point(rect.x + rect.width, rect.y + rect.height));
    }

    abstract protected   Pointer[] getPointer();

    protected Scalar getColor(){
        return new Scalar(255,255,255);
    }


    @Override
    public List<MatOfPoint> getContoures() {
        return contours;
    }

    Mat getSobel(Mat input){
        // First we declare the variables we are going to use
        Mat grad = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;

        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();

        Imgproc.Sobel( input, grad_x, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT );
        Imgproc.Sobel( input, grad_y, ddepth, 0, 1, 3, scale, delta, Core.BORDER_DEFAULT );

        // converting back to CV_8U
        Core.convertScaleAbs( grad_x, abs_grad_x );
        Core.convertScaleAbs( grad_y, abs_grad_y );
        Core.addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad );

        val out = new Mat(grad.size(),grad.type());
        for (int i = 0; i < grad.rows(); i++) {
            for (int j = 0; j < grad.cols(); j++) {
                if(grad.get(i,j)[0]> 85){
                    out.put(i,j,255.0,255.0,255.0);
                } else {
                    out.put(i,j,0,0,0);
                }

            }
        }
        return out;

    }

    @Override
    public Size getSize() {
        return SIZE;
    }
}

