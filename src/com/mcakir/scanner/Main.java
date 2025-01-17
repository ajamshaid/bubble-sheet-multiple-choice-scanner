package com.mcakir.scanner;

import com.aj.bubblesheet.BBScanner;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mcakir.scanner.Util.*;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.*;

public class Main {




    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {

        sout("...started");
        //Mat source = Imgcodecs.imread(getSource("new-sheet.png"));
        BBScanner scanner = new BBScanner(false);
//        scanner.scanImage(Imgcodecs.imread(getSource("Image (1).jpg")),"Image (1).jpg");
//        scanner.scanImage(Imgcodecs.imread(getSource("Image (2).jpg")),"Image (2).jpg");
//        scanner.scanImage(Imgcodecs.imread(getSource("Image (3).jpg")),"Image (3).jpg");
//        scanner.scanImage(Imgcodecs.imread(getSource("Image (4).jpg")),"Image (4).jpg");
//        scanner.scanImage(Imgcodecs.imread(getSource("Image (5).jpg")),"Image (5).jpg");
        scanner.scanImage(Imgcodecs.imread(getSource("Image (6).jpg")),"Image (6).jpg");
//        scanner.scanImage(Imgcodecs.imread(getSource("Image (7).jpg")),"Image (7).jpg");

//       Scanner scanner = new Scanner(source, 18);
//        scanner.setLogging(true);
//        scanner.scan();

  //      sout("...finished");
    }



}
