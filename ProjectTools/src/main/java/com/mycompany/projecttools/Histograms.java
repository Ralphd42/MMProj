/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.projecttools;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.opencv.imgproc.Imgproc;



/**
 *
 * @author ralph
 */
public class Histograms {
    public static Mat getHistMatGrey(Mat image){
        if(image.channels()==3){
            cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        }
        IntPointer sz=new IntPointer(new int[]{256});
        IntPointer chn=new IntPointer(new int[]{0});
        final float[] ranges = {0f,256f}; 
        PointerPointer<FloatPointer> range_pointer=new PointerPointer<>(ranges); 
        Mat hist=new Mat(); 
        hist.rows(image.rows()).cols(image.cols()); 
        Mat mask=new Mat(); 
        mask.rows(image.rows()).cols(image.cols()); 
        opencv_imgproc.calcHist(image, 1, chn, mask, hist, 1, sz, range_pointer,true,false); 
        opencv_core.normalize(hist, hist, 0, 1, opencv_core.NORM_MINMAX, -1, new Mat()); 
        return hist; 
    }
}
