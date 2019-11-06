/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.projecttools;
import javax.swing.WindowConstants;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MPEG4;
import org.bytedeco.javacpp.indexer.ShortRawIndexer;

import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_COMP_CHISQR;
import static org.bytedeco.opencv.global.opencv_imgproc.compareHist;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

/**
 *
 * @author ralph
 */
public class VideoParser {
    public int MaxDiffSHOT(
            String fname, String outFileName ) throws FrameGrabber.Exception, FrameRecorder.Exception {
        FFmpegFrameRecorder outRec = 
                new FFmpegFrameRecorder
                    (outFileName, 640, 720);
        outRec.setFrameRate(1);
        outRec.setFormat("mp4");
        outRec.setVideoCodec(AV_CODEC_ID_MPEG4);
        
        int cnt = 0;
        Mat mret = new Mat();
        Mat morig = null;
        Mat histOrig = new Mat();
        IplImage image;
        FFmpegFrameGrabber grb = new FFmpegFrameGrabber(fname);
        grb.start();
        Frame fr;
        outRec.start();
        Frame fr1=null, fr2=null;  // fr1 and fr2 are the two frames with the greatest distance using chi squared
        Frame frPrev=null;  // the previous frame
        double maxHistDiff =-100F;
        int maxFrameLoc= 0;
        ToMat mt = new ToMat();
        while (((fr = grb.grab()) != null)) {
            if (cnt == 0) {
                
                morig = mt.convert(fr);
                
                fr1 = fr.clone();
                frPrev = fr.clone();
                
                histOrig = Histograms.getHistMatGrey(morig);
                ++cnt;
                
            } else {
                
                 
                //System.out.printf("\nIS KEY FRAME %b",fr.keyFrame);
                if (fr.image != null) {
                    if( cnt==1)
                    {
                        fr2=fr.clone();
                    }
                    ++cnt;
                    Mat nxt = mt.convert(fr);
                    Mat nxtHist = Histograms.getHistMatGrey(nxt);
                    double histdiff = compareHist(histOrig, nxtHist, CV_COMP_CHISQR);
                    if (histdiff > maxHistDiff) 
                    {
                        fr2 =fr.clone();;//fr);
                        fr1 = frPrev;
                        maxHistDiff=histdiff;
                        maxFrameLoc =cnt;
                        System.out.printf("\nIMGNUM%d diff %f", cnt, histdiff);
                        ///morig = mt.convert(fr);
                        //histOrig = Histograms.getHistMatGrey(morig);
                    }
                    histOrig =nxtHist;
                    frPrev = fr.clone();
                }
            }
        }
        // write out files
        // write to outfile
       outRec.record(fr1);
        outRec.record(fr2);
        outRec.stop();
        return cnt;
    }
    
    
    public void BlocProcMat()
    {
       //ubmat
       /* val image1 = loadAndShowOrExit(new File("data/church01.jpg"), IMREAD_GRAYSCALE)
  val image2 = loadAndShowOrExit(new File("data/church02.jpg"), IMREAD_GRAYSCALE)

  // define a template
  val target = new Mat(image1, new Rect(120, 40, 30, 30))
  show(target, "Template")
        */
        
        Mat img = imread("/home/ralph/development/fall2019Classes/mm/HW2/target.jpg");
        Mat m = new Mat( img, new Rect(0,0,200,200));
        display(m, "piece");
        System.out.println(m.rows()+ " " + m.cols());
        Mat m2 = new Mat( img, new Rect(100,100,200,200));
        display(m2, "piece2");
        System.out.println(m2.rows()+ " " + m2.cols());
        
        /*
        
        
        Mat m = new Mat(100,100,3);
        ShortRawIndexer sI = m.createIndexer();
        for (int y = 0; y < m.rows(); y++) {

            for (int x = 0; x < m.cols(); x++) {

               // sI.put(x,y,255);
            }
        }
        imwrite("/home/ralph/development/fall2019Classes/mm/HW2/white.jpg",m);
        */
    
    
    }
    
    
    public boolean isPicMatch(Mat pic, Mat compTo, double tv)
    {
        final int blocksz =16;
        boolean rv = false;
        int xdim = pic.rows();
        int ydim = pic.cols();
        /*
            get greyscale histogram for compto
            build looping mechanism to get blocks from pic
            calculate distance
            if distance less than thresh we have an item
        */
        
        for (int curRow =0;( (curRow+blocksz)<xdim ) && (!rv)    ; curRow+=blocksz)
        {
            for (int curCol =0; ((curCol+blocksz)<ydim)  && (!rv)   ;curCol +=blocksz)
            {
                Mat chunck =new Mat( pic, new Rect(curRow,curCol,blocksz,blocksz));
                double diff=getHistDist(chunck,compTo);
                if( diff<tv)
                {
                    rv = true;
                }
            
            }
        
        }
    
    
        return rv;
    }
    
    
    
    
    
    
    
    
    
    double getHistDist( Mat m1, Mat m2)
    {
        Mat m1GreyHist = Histograms.getHistMatGrey(m1);
        Mat m2GreyHist = Histograms.getHistMatGrey(m2);
        double hChiSq  = compareHist(m1GreyHist, m2GreyHist, CV_COMP_CHISQR);
        return hChiSq;
    }
    
    
    static void display(Mat image, String caption) {
        // Create image window named "My Image".
        final CanvasFrame canvas = new CanvasFrame(caption, 1.0);

        // Request closing of the application when the image window is closed.
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Convert from OpenCV Mat to Java Buffered image for display
        final OpenCVFrameConverter converter = new OpenCVFrameConverter.ToMat();
        // Show image on window.
        Frame f = converter.convert(image);

        canvas.showImage(converter.convert(image));
    }
    
    
    
    public void readAvi() throws FrameGrabber.Exception
    {
        FrameGrabber grabber = new FFmpegFrameGrabber("");
        grabber.start();
IplImage image= null;
//while((image=grabber.grab())!=null){
   // TODO set the image on the canvas or panel where ever you want. 
//}
grabber.stop();
    
    
    
    
    
    
    }

    



}
