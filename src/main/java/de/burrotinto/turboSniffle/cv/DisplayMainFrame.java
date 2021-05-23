package de.burrotinto.turboSniffle.cv;

import de.burrotinto.turboSniffle.meters.gauge.CirceGaugeOnePointer;
import de.burrotinto.turboSniffle.meters.gauge.CirceGaugeVerticalSpeed;
import de.burrotinto.turboSniffle.meters.Measuring;
import de.burrotinto.turboSniffle.meters.MeasuringType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;


public class DisplayMainFrame {
     final TextDedection textDedection = new TextDedection();


    private final HashMap<String, DisplayFrame> measurings = new HashMap<String, DisplayFrame>();
    private Mat mainframe;
    private Mat greyMainframe;

    public void setMainframe(Mat mainframe) {
        this.mainframe = mainframe.clone();
        this.greyMainframe = Mat.zeros(mainframe.size(), mainframe.type());
        Imgproc.cvtColor(this.mainframe, this.greyMainframe, Imgproc.COLOR_BGR2GRAY);
    }

    public Mat getMainframe() {
        return mainframe;
    }

    public Mat getGreyMainframe() {
        return greyMainframe;
    }

    /**
     * Hinzufügen eine Messgerätes
     *
     * @param point  links oben
     * @param length Quadratischer Ausschnitt
     * @param name   Zum auffinden des Messgerätes
     * @return
     */
    public DisplayFrame addMeasuring(Point point, int length, String name, MeasuringType type, String unit) {
        val rect = new Rect((int) point.x, (int) point.y, length, length);
        Measuring measuring;


        switch (type) {
            case Vertical_Speed:
                measuring = new CirceGaugeVerticalSpeed(greyMainframe.submat(rect),textDedection);
                break;
            default:
                measuring = new CirceGaugeOnePointer(greyMainframe.submat(rect),textDedection);
                break;
        }

        val displayFrame = new DisplayFrame(name, rect, measuring, unit);
        measurings.put(name, displayFrame);

        return displayFrame;
    }

    public Measuring getMeasuring(String name) {
        return measurings.get(name).getMeasuring();
    }

    public String getUnit(String name) {
        return measurings.get(name).getUnit();
    }

    public double getValue(String name) {
        return measurings.get(name).getMeasuring().getValue();
    }

    public Mat getRasterdDisplayMainFrame(int raster) {
        val drawing = mainframe.clone();

        val color = new Scalar(255, 255, 255);

        for (int i = raster; i < mainframe.size().width; i = i + raster) {
            Imgproc.line(drawing, new Point(i, 0), new Point(i, mainframe.size().height), color, 2);
        }
        for (int i = raster; i < mainframe.size().height; i = i + raster) {
            Imgproc.line(drawing, new Point(0, i), new Point(mainframe.size().width, i), color, 2);
        }
        return drawing;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class DisplayFrame {
        private String name;
        private Rect frame;
        private Measuring measuring;
        private String unit;
    }
}
