//package de.burrotinto.turboSniffle.meters.gauge;
//
//import de.burrotinto.popeye.transformation.PointPair;
//import org.opencv.core.Mat;
//import org.opencv.core.Point;
//
//import java.util.Optional;
//
//public class GaugeWithOnePointer extends Gauge {
//
//    private Optional<Double> pointerAngel = Optional.empty();
//    public GaugeWithOnePointer(Mat source) {
//        super(source,null);
//    }
//
//    public GaugeWithOnePointer(Mat source, Double pointerAngel) {
//        super(source,null);
//        this.pointerAngel = Optional.ofNullable(pointerAngel);
//    }
//
//    public double getPointerAngel() {
//        return pointerAngel.orElse(-1.0);
//    }
//
//    public void setPointerAngel(double angel){
//        pointerAngel = Optional.ofNullable(angel);
//    }
//
////    public PointPair getPointer(){
////        double angle = getPointerAngel() -90;
////        double length = getSource().size().width/2;
////        Point p1 = new Point (length,length);
////        Point p2 = new Point();
////
////        double x =  Math.round(p1.x + length * Math.cos(angle * Math.PI / 180.0));
////        double y =  Math.round(p1.y + length * Math.sin(angle * Math.PI / 180.0));
////
////        return new PointPair(p1,new Point(x,y));
////    }
//
//}
