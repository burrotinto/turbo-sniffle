package de.burrotinto.turboSniffle.meters.gauge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

@Setter
@Getter
public class Gauge {
    public final static int TYPE = CvType.CV_8U;
    private Mat source;

    public Gauge(Mat source) {
        //Convertiere in Grau
        if(source.type() == TYPE) {
            this.source = source;
        } else {
            this.source = Mat.zeros(source.size(),TYPE);
            Imgproc.cvtColor(source, this.source, Imgproc.COLOR_BGR2GRAY );
        }
    }

    public Mat toSize(Size size) {
        Mat out = new Mat();
        Imgproc.resize(source, out, size);
        return out;
    }


}
