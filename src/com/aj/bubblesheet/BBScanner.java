package com.aj.bubblesheet;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static com.mcakir.scanner.Util.*;
import static com.mcakir.scanner.Util.sortLeft2Right;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.*;

public class BBScanner {
    boolean logging = false;
    Mat canny, grayImage ,source, hierarchy, thresh;
    private Rect roi;
    private List<MatOfPoint> contours, bubbles;
    private List<Integer> answers;
    private final double[] ratio = new double[]{ 2, 10};
    private final String[] options = new String[]{"A", "B", "C", "D","E"};

    private final int questionCount=0;

    public BBScanner(boolean logging) {
        this.logging = logging;

        hierarchy = new Mat();
        contours = new ArrayList<>();
        bubbles = new ArrayList<>();
        answers = new ArrayList<>();
    }
    public void scanImage(Mat image, String name) {
        sout("========================");
        sout("Scanning: "+name);
        sout("========================");

        source = image;
        // Preprocess the image
        grayImage = new Mat();
        Imgproc.cvtColor(source, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayImage, grayImage, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        if(logging) write2File(grayImage, "step_1_gray.png");


        thresh = new Mat(grayImage.rows(), grayImage.cols(), grayImage.type());
        threshold(grayImage, thresh, 150, 255, THRESH_BINARY);
        if(logging) write2File(thresh, "step_2_thresh.png");

        int threshold = 100;
        canny = new Mat(grayImage.size(), CV_8UC1);
//        Canny(blur, canny, 160, 20);
        Imgproc.Canny(grayImage, canny, threshold, threshold*3);
        if(logging) write2File(canny, "step_3_canny.png");

        findROI(logging);

        sout("-----------ROI Extracted---------");

        try {
            findBubbles();
        } catch (Exception e) {

            sout("bubble size............."+bubbles.size());

            throw new RuntimeException(e);
        }

        recognizeAnswers();

        sout("*************************************");
      /*  sout("*************************************");
        sout("answer is ....");
        sout("*************************************");
        sout("*************************************");

        for(int index = 0; index < answers.size(); index++){
            Integer optionIndex = answers.get(index);
      //      sout((index + 1) + ". " + (optionIndex == null ? "EMPTY/INVALID" : options[optionIndex]));
        }

        write2File(source, "result.png");

       */
    }

 private void findBubbles() throws Exception {

   //     logging  = true;
        contours.clear();

        findContours(canny.submat(roi), contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        double threshold = 0;
        double _w = roi.width / 28;//this.ratio[0];
        double _h = roi.height / 10 ;//this.ratio[1];
        double minThreshold = Math.floor(Math.min(_w, _h)) - threshold;
        double maxThreshold = Math.ceil(Math.max(_w, _h)) + threshold;

//        minThreshold = 3;
//        maxThreshold = 6;

        if(logging) sout("findBubbles > ideal circle size > minThreshold: " + minThreshold + ", maxThreshold: " + maxThreshold);

        List<MatOfPoint> drafts = new ArrayList<>();
        for(MatOfPoint contour : contours){
            Rect _rect = boundingRect(contour);
            int w = _rect.width;
            int h = _rect.height;
            double ratio = Math.max(w, h) / Math.min(w, h);

            if(logging) System.out.print("\nfindBubbles > founded circle > w: " + w + ", h: " + h);

            if(ratio >= 0.9 && ratio <= 1.1)
                if(Math.max(w, h) < maxThreshold && Math.min(w, h) >= minThreshold){

                    if(logging) System.out.print("--------adding circle");
                    drafts.add(contour);
                }else{
           //         System.out.println("++++Misssing contour"+contour);
                }
        }

       sout("findBubbles > bubbles.size: " + drafts.size());

/*
        if(drafts.size() != questionCount * options.length){
            throw new Exception("Couldn't capture all bubbles.");
        }
        // order bubbles on coordinate system
*/
        sortTopLeft2BottomRight(drafts);

        bubbles = new ArrayList<>();

        for(int j = 0; j < drafts.size(); j+=options.length*3){

            List<MatOfPoint> row = drafts.subList(j, j + options.length*3);

            sortLeft2Right(row);

//            if(logging) write2File(drawCounter(row), "drafts_"+j+".png");
            bubbles.addAll(row);
        }
    }

    public void findROI(boolean logging){

        hierarchy = new Mat() ;
        // Find contours
        contours = new ArrayList<>();

        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

      //  if(logging) sout("getParentRectangle > hiearchy data:\n" + hierarchy.dump());
        //if(logging) sout("Contours Found:\n" +contours.size());

        HashMap<Double, MatOfPoint> rectangles = new HashMap<>();
        for(int i = 0; i < contours.size(); i++){
            MatOfPoint2f approxCurve = new MatOfPoint2f( contours.get(i).toArray() );
            approxPolyDP(approxCurve, approxCurve, 0.02 * arcLength(approxCurve, true), true);

            if(approxCurve.toArray().length == 4){
                rectangles.put((double) i, contours.get(i));
            }
        }

        if(logging) sout("getParentRectangle > contours.size: " + contours.size());
        if(logging) sout("getParentRectangle > rectangles.size: " + rectangles.size());


        if(logging) sout("getParentRectangle > rectangles.keySEt: " + rectangles.keySet());
        int parentIndex = -1;

        // choose hierarchical rectangle which is our main wrapper rect
        for (Map.Entry<Double, MatOfPoint> rectangle : rectangles.entrySet()) {
            double index = rectangle.getKey();

            double[] ids = hierarchy.get(0, (int) index);
            double nextId = ids[0];
            double previousId = ids[1];
//            double childId = ids[2];

            if(nextId != -1 && previousId != -1) continue;

            int k = (int) index;
            int c = 0;

            while(hierarchy.get(0, k)[2] != -1){
                k = (int) hierarchy.get(0, k)[2];
                c++;
            }

            if(hierarchy.get(0, k)[2] != -1) c++;

            if (c >= 2 && index < 100){
                parentIndex = (int) index;
                sout("Parent Index getParentRectangle > index: " + index + ", c: " + c);
            }

            if(logging) sout("getParentRectangle > index: " + index + ", c: " + c);
        }

        if(logging) sout("getParentRectangle > parentIndex: " + parentIndex);


        roi = boundingRect(contours.get(parentIndex));

        if(logging) sout("getParentRectangle > original roi.x: " + roi.x + ", roi.y: " + roi.y);
        if(logging) sout("getParentRectangle > original roi.width: " + roi.width + ", roi.height: " + roi.height);

      //  int padding = 2;
        int padding = 20;
        roi.x += padding;
        roi.y += padding;
        roi.width -= 2 * padding;
        roi.height -= 2 * padding;

        if(logging) sout("getParentRectangle > modified roi.x: " + roi.x + ", roi.y: " + roi.y);
        if(logging) sout("getParentRectangle > modified roi.width: " + roi.width + ", roi.height: " + roi.height);

        if(logging)
            write2File(source.submat(roi), "step_3_roi.png");
    }

    private void recognizeAnswers(){

        int rowNO = 0;
        int colNo = 0;
        int temp = 0;
        String[][] q_series = bubbles.size()==150 ? q30_series : q24_series;
        for(int i = 0; i< bubbles.size(); i+=options.length) {
            List<MatOfPoint> rows = bubbles.subList(i, i+options.length);

            //int[][] filled = new int[rows.size()][4];
            int[][] filled = new int[rows.size()][5];
            boolean isFilled = false;
            for (int j = 0; j < rows.size(); j++) {
                MatOfPoint col = rows.get(j);

                List<MatOfPoint> list = Arrays.asList(col);

                Mat mask = new Mat(thresh.size(), CvType.CV_8UC1);
                drawContours(mask.submat(roi), list, -1, new Scalar(255, 0, 0), -1);

                Mat conjuction = new Mat(thresh.size(), CvType.CV_8UC1);
                Core.bitwise_and(thresh, mask, conjuction);

                // Test code for debugging particular bubble.
                /* if("2".equals(q_series[rowNO][colNo] ) ) {
                     write2File(mask, "mask_" + i + "_" + j + ".png");
                     write2File(conjuction, "conjuction_" + i + "_" + j + ".png");
                }*/

                Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


                int countNonZero = Core.countNonZero(conjuction);
                double pixel =countNonZero/contourArea(rows.get(j))*100;

//                if(logging) sout("recognizeAnswers > " + i + ":" + j + " > countNonZero: " + countNonZero + " > pixel: " + pixel);
                if(pixel>=45 && pixel<=700){
                    //counting filled circles
                    System.out.print("Q"+q_series[rowNO][colNo] +":"+options[j]+ "     ");
                    isFilled = true;
                }



                filled[j] = new int[]{ countNonZero, i, j};
                if(j==4 && !isFilled){
                    System.out.print("Q"+q_series[rowNO][colNo] +":NAN  ");
                }
            }

            colNo++;
            colNo = colNo <= 2 ? colNo:0;

            if(((i+5)%15) == 0){
                 rowNO++;
                 sout("\n--------------------------");
            }


//            int[] selection = chooseFilledCircle(filled);
//
//       //     if(logging) sout("recognizeAnswers > "+i+" > selection is " + (selection == null ? "empty/invalid" : selection[2]));
//
//            if(selection != null){
//
////                putText(source.submat(roi), "(" + i + "_" + selection[2] + ")", new Point(rows.get(selection[2]).get(0, 0)), Core.FONT_HERSHEY_SIMPLEX, 0.3, new Scalar(0, 255, 0));
//                drawContours(source.submat(roi), Arrays.asList(rows.get(selection[2])), -1, new Scalar(0, 255, 0), 3);
//            }
//
//            answers.add(selection == null ? null : selection[2]);
        }

        List<Integer> odds = new ArrayList<>();
        List<Integer> evens = new ArrayList<>();
        for(int i = 0; i < answers.size(); i++){
            if(i % 2 == 0) odds.add(answers.get(i));
            if(i % 2 == 1) evens.add(answers.get(i));
        }

        answers.clear();
        answers.addAll(odds);
        answers.addAll(evens);
    }

    private int[] chooseFilledCircle(int[][] rows){

        double mean = 0;
        for(int i = 0; i < rows.length; i++){
            mean += rows[i][0];
        }
        mean = 1.0d * mean / options.length;

        int anomalouses = 0;
        for(int i = 0; i < rows.length; i++){
            if(rows[i][0] > mean) anomalouses++;
        }

        if(anomalouses == options.length - 1){

            int[] lower = null;
            for(int i = 0; i < rows.length; i++){
                if(lower == null || lower[0] > rows[i][0]){
                    lower = rows[i];
                }
            }

            return lower;

        } else {
            return null;
        }
    }
}
