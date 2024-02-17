package com.mcakir.scanner;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class Util {

    public static String SOURCE_FOLDER = System.getProperty("user.dir") + "/sources/";
    public static String TARGET_FOLDER = System.getProperty("user.dir") + "/target/";
    public static String[][] q30_series =
            new String[][]{
                    {"1", "11", "21"},
                    {"2", "12", "22"},
                    {"3", "13", "23"},
                    {"4", "14", "24"},
                    {"5", "15", "25"},
                    {"6", "16", "26"},
                    {"7", "17", "27"},
                    {"8", "18", "28"},
                    {"9", "19", "29"},
                    {"10", "20", "30"}
            };
    public static String[][] q24_series =
            new String[][]{
                    {"1", "9", "17"},
                    {"2", "10", "18"},
                    {"3", "11", "19"},
                    {"4", "12", "20"},
                    {"5", "13", "21"},
                    {"6", "14", "22"},
                    {"7", "15", "23"},
                    {"8", "16", "24"}
            };
    public static String getSource(String name) {
        return SOURCE_FOLDER + name;
    }

    public static String getOutput(String name) {
        return TARGET_FOLDER + name;
    }

    public static void write2File(Mat source, String name) {
        imwrite(getOutput(name), source);
    }


    public static Mat drawCounter(List<MatOfPoint> drafts) {
        Mat outputImage = new Mat(50, 50, CvType.CV_8UC3); // RGB image
        Imgproc.drawContours(outputImage, drafts, -1, new Scalar(255, 255, 255), 2);
        return outputImage;
    }


    public static void sout(String str) {
        System.out.println(str);
    }

    public static void sortTopLeft2BottomRight(List<MatOfPoint> points) {
        // top-left to right-bottom sort
        Collections.sort(points, (e1, e2) -> {

            Point o1 = new Point(e1.get(0, 0));
            Point o2 = new Point(e2.get(0, 0));

            return o1.y > o2.y ? 1 : -1;
        });
    }

    public static void sortLeft2Right(List<MatOfPoint> points) {
        // left to right sort
        Collections.sort(points, (e1, e2) -> {

            Point o1 = new Point(e1.get(0, 0));
            Point o2 = new Point(e2.get(0, 0));

            return o1.x > o2.x ? 1 : -1;
        });
    }
}
