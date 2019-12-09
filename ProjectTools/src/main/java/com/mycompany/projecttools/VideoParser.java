/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.projecttools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.WindowConstants;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MPEG4;
import static org.bytedeco.ffmpeg.global.avutil.AVERROR_STREAM_NOT_FOUND;
import static org.bytedeco.ffmpeg.global.avutil.av_strerror;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import static org.bytedeco.opencv.global.opencv_core.subtract;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_COMP_CHISQR;
import static org.bytedeco.opencv.global.opencv_imgproc.compareHist;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author ralph
 */
public class VideoParser {

    public int MaxDiffSHOT(
            String fname, String outFileName) throws FrameGrabber.Exception, FrameRecorder.Exception {
        FFmpegFrameRecorder outRec
                = new FFmpegFrameRecorder(outFileName, 640, 720);
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
        Frame fr1 = null, fr2 = null;  // fr1 and fr2 are the two frames with the greatest distance using chi squared
        Frame frPrev = null;  // the previous frame
        double maxHistDiff = -100F;
        int maxFrameLoc = 0;
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
                    if (cnt == 1) {
                        fr2 = fr.clone();
                    }
                    ++cnt;
                    Mat nxt = mt.convert(fr);
                    Mat nxtHist = Histograms.getHistMatGrey(nxt);
                    double histdiff = compareHist(histOrig, nxtHist, CV_COMP_CHISQR);
                    if (histdiff > maxHistDiff) {
                        fr2 = fr.clone();;//fr);
                        fr1 = frPrev;
                        maxHistDiff = histdiff;
                        maxFrameLoc = cnt;
                        System.out.printf("\nIMGNUM%d diff %f", cnt, histdiff);
                        ///morig = mt.convert(fr);
                        //histOrig = Histograms.getHistMatGrey(morig);
                    }
                    histOrig = nxtHist;
                    frPrev = fr.clone();
                }
            }
        }
        outRec.record(fr1);
        outRec.record(fr2);
        outRec.stop();
        return cnt;
    }

    public void BlocProcMat() {
        //ubmat
        /* val image1 = loadAndShowOrExit(new File("data/church01.jpg"), IMREAD_GRAYSCALE)
  val image2 = loadAndShowOrExit(new File("data/church02.jpg"), IMREAD_GRAYSCALE)

  // define a template
  val target = new Mat(image1, new Rect(120, 40, 30, 30))
  show(target, "Template")
         */

        Mat img = imread("/home/ralph/development/fall2019Classes/mm/HW2/target.jpg");
        Mat m = new Mat(img, new Rect(0, 0, 200, 200));
        display(m, "piece");
        System.out.println(m.rows() + " " + m.cols());
        Mat m2 = new Mat(img, new Rect(100, 100, 200, 200));
        display(m2, "piece2");
        System.out.println(m2.rows() + " " + m2.cols());

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

    public boolean isPicMatch(Mat pic, Mat compTo, double tv) {
        final int blocksz = 16;
        boolean rv = false;
        int xdim = pic.rows();
        int ydim = pic.cols();
        double minDiff = 9999999.999;
        /*
            get greyscale histogram for compto
            build looping mechanism to get blocks from pic
            calculate distance
            if distance less than thresh we have an item
         */

        for (int curRow = 0; ((curRow + blocksz) < xdim) && (!rv); curRow += blocksz) {
            for (int curCol = 0; ((curCol + blocksz) < ydim) && (!rv); curCol += blocksz) {

                Mat chunck = new Mat(pic, new Rect(curCol, curRow, blocksz, blocksz));
                double diff = getHistDist(chunck, compTo);
                if (diff < tv) {
                    rv = true;
                }
                if (diff < minDiff) {
                    minDiff = diff;

                }

            }

        }
        System.out.println("MinDIff is " + minDiff);

        return rv;
    }

    double getHistDist(Mat m1, Mat m2) {
        Mat m1GreyHist = Histograms.getHistMatGrey(m1);

        Mat m2GreyHist = Histograms.getHistMatGrey(m2);
        double hChiSq = compareHist(m1GreyHist, m2GreyHist, CV_COMP_CHISQR);
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

    public void readAvi() throws FrameGrabber.Exception {
        FrameGrabber grabber = new FFmpegFrameGrabber("");
        grabber.start();
        IplImage image = null;
        //while((image=grabber.grab())!=null){
        // TODO set the image on the canvas or panel where ever you want. 
        //}
        grabber.stop();

    }

    /*
    needs a ref pic 
    movie file
    output file
    blocksize
    output info file
    loop file 
    test if has image
    if has save.
    output frame number
    repeat for file
    
    
    enhancements.
        key frames only
    
    other tools  
        get main key frame
    Show distance between 
        main Key and all items
    
        
    
    
    
    
     */
    public int SummarizeShot(String refImage, String inputVideo, int blocksize, String OutPutFile, String outputinfoFile,
            double maxDist) throws FrameGrabber.Exception, FrameRecorder.Exception, IOException {
        return SummarizeShot(refImage, inputVideo, blocksize, OutPutFile, outputinfoFile,
                maxDist, -1, -1);
    }

    public int SummarizeShot(String refImage, String inputVideo, int blocksize, String OutPutFile, String outputinfoFile,
            double maxDist, int x, int y) throws FrameGrabber.Exception, FrameRecorder.Exception, IOException {
        Mat compTo = imread(refImage);
        if (x > -1 && y > -1) {
            Rect r = new Rect(x, y, blocksize, blocksize);

            compTo = new Mat(compTo, r);

        }

        int retval = 0;
        FFmpegFrameRecorder outRec
                = new FFmpegFrameRecorder(OutPutFile, 640, 720);
        outRec.setFrameRate(5);
        outRec.setFormat("mp4");
        outRec.setVideoCodec(AV_CODEC_ID_MPEG4);
        FrameGrabber grb = new FFmpegFrameGrabber(inputVideo);
        grb.start();
        Frame fr;
        outRec.start();
        int cntr = 0;
        ToMat mt = new ToMat();
        PrintWriter pw = new PrintWriter(new FileWriter(outputinfoFile));
        while (((fr = grb.grab()) != null)) {
            if (fr.image != null) {
                ++cntr;
                boolean keep = false;
                Mat pic = mt.convert(fr);
                keep = isPicMatch(pic, compTo, maxDist);
                if (keep) {
                    outRec.record(fr);
                    pw.println("Frame :" + cntr + " " + fr.imageChannels);
                }
            }
        }
        pw.close();
        outRec.stop();
        return retval;
    }

    public int GetNFilesFromVideo(String InputVideo, String outputPath, int numImages) {
        int grbcnt = 0;
        try {
            FFmpegFrameGrabber grb = new FFmpegFrameGrabber(InputVideo);
            //grb.setFormat("avi");
            grb.start();
            Frame fr;
            ToMat mt = new ToMat();
            while (((fr = grb.grab()) != null) && (grbcnt < numImages)) {
                if (fr.image != null) {
                    Mat m = mt.convert(fr);
                    String fName = outputPath + "/img" + grbcnt + ".jpg";
                    imwrite(fName, m);
                    grbcnt++;
                }
            }
        } catch (FrameGrabber.Exception eg) {
            //_strerror()
            //String err   = av_strerror(,,-2);
            BytePointer buffer = new BytePointer(256);
            av_strerror(-2, buffer, buffer.capacity());
            System.out.println(buffer.getString());
            System.out.println(eg.getMessage());
            System.out.println(eg);
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        } catch (Exception exp) {
            System.out.print(exp);
            // must start handling

        }
        return grbcnt;

    }

    public int getAllFilesFromVide(String InputVideo, String outputPath) {

        return GetNFilesFromVideo(InputVideo, outputPath, Integer.MAX_VALUE);

    }

    public int KeyFrame(String fname, int threshehold, int FrameRate) throws FrameGrabber.Exception, FrameRecorder.Exception {
        FFmpegFrameRecorder outRec = new FFmpegFrameRecorder("/home/ralph/development/fall2019Classes/mm/HW2/HW2P3.mp4", 640, 720);
        outRec.setFrameRate(FrameRate);
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
        while (((fr = grb.grab()) != null)) {
            if (cnt == 0) {
                ToMat mt = new ToMat();
                morig = mt.convert(fr);
                Histograms hs = new Histograms();
                histOrig = Histograms.getHistMatGrey(morig);
                ++cnt;
                outRec.record(fr);
            } else {
                ToMat mt = new ToMat();
                //System.out.printf("\nIS KEY FRAME %b",fr.keyFrame);
                if (fr.image != null) {
                    Mat nxt = mt.convert(fr);
                    Mat nxtHist = Histograms.getHistMatGrey(nxt);
                    double histdiff = compareHist(histOrig, nxtHist, CV_COMP_CHISQR);
                    if (histdiff > threshehold) {
                        System.out.printf("\nIMGNUM%d diff %f", cnt, histdiff);
                        outRec.record(fr);
                        morig = mt.convert(fr);
                        histOrig = Histograms.getHistMatGrey(morig);
                        ++cnt;
                    }
                } else {
                    //av frame do nothing now
                }
            }
        }
        outRec.stop();
        return cnt;
    }

    public int FrameCount(String str) {
        int cnt = 0;
        try {
            FFmpegFrameGrabber grb = new FFmpegFrameGrabber(str);
            grb.start();
            Frame fr;

            while (((fr = grb.grab()) != null)) {
                if (fr.image != null) {
                    ++cnt;
                }
            }
        } catch (Exception exp) {

        }

        return cnt;

    }

    public int SplitVideo(String VideoFile, String outFile, int startFrame, int cnt) {
        int added = 0;
        try {
            FFmpegFrameRecorder outRec
                    = new FFmpegFrameRecorder(outFile, 640, 720);
            FFmpegFrameGrabber grb = new FFmpegFrameGrabber(VideoFile);
            grb.start();
            grb.getVideoFrameRate();

            outRec.setFormat("mp4");
            outRec.setVideoCodec(AV_CODEC_ID_MPEG4);
            outRec.setFrameRate(5);
            Frame fr;
            outRec.start();

            int currFrame = 0;
            while ((currFrame < (startFrame + cnt))
                    && ((fr = grb.grab()) != null)) {
                if (fr.image != null) {
                    ++currFrame;
                    if (currFrame >= startFrame) {
                        outRec.record(fr);
                        ++added;
                    }
                }
            }
            outRec.stop();
        } catch (Exception exp) {
            System.err.print(exp);

        }
        return added;

    }

    public int GetAverageImageForVideo(String inputVideo, String outputAvgLocation) {
        int cntr = 0;
        int xdim;
        int ydim;
        long[][] sums = null;
        Mat pic = null;
        try {
            FrameGrabber grb = new FFmpegFrameGrabber(inputVideo);
            grb.start();
            Frame fr;
            ToMat mt = new ToMat();
            while (((fr = grb.grab()) != null)) {
                if (fr.image != null) {
                    pic = mt.convert(fr);
                    cvtColor(pic, pic, Imgproc.COLOR_RGB2GRAY);
                    if (cntr == 0) {
                        xdim = pic.rows();
                        ydim = pic.cols();
                        System.out.println("xdim =" + xdim);
                        System.out.println("ydim =" + ydim);
                        sums = new long[xdim][ydim];
                    }

                    UByteRawIndexer sI = pic.createIndexer();

                    for (int y = 0; y < pic.cols(); y++) {
                        for (int x = 0; x < pic.rows(); x++) {
                            //System.out.println(x +" - " +  y + " - "+  sI.get(x, y) );
                            sums[x][y] += sI.get(x, y);
                        }

                    }
                    cntr++;
                    // System.out.println("IMAGE NUM ="+ cntr);

                }
            }
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(VideoParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        UByteRawIndexer sI = pic.createIndexer();
        for (int y = 0; y < pic.cols(); y++) {
            for (int x = 0; x < pic.rows(); x++) {
                //System.out.println( sI.get(x, y) );
                int val = (int) sums[x][y] / cntr;
                sI.put(x, y, val);
            }

        }

        imwrite(outputAvgLocation, pic);

        return cntr;
    }

    /**
     * this function will take a file remove background and resave image
     *
     * @param inputVideo the input video
     * @param outputVideo the outputvidep
     * @param avgFileLocation the avg/background file
     * @return
     */
    public int RemoveAvgFromVid(String inputVideo, String outputVideo, String avgFileLocation) {
        int cntr = 0;
        int numpicsused = 0;
        GetAverageImageForVideo(inputVideo, avgFileLocation);
        Mat avg = imread(avgFileLocation);
        FFmpegFrameRecorder outRec
                = new FFmpegFrameRecorder(outputVideo, 640, 720);
        outRec.setFrameRate(30);
        outRec.setFormat("mp4");
        outRec.setVideoCodec(AV_CODEC_ID_MPEG4);
        FrameGrabber grb = new FFmpegFrameGrabber(inputVideo);
        try {
            /*
            get average
            2 load avg
            3 create both reader and writer
             */

            grb.start();
            Frame fr;
            outRec.start();

            ToMat mt = new ToMat();

            while (((fr = grb.grab()) != null)) {
                if (fr.image != null) {
                    //greyscal then subtract then write to FrameGrabber
                    Mat currImg = mt.convert(fr);
                    Mat imgResult = subtract(currImg, avg).asMat();
                    Java2DFrameConverter convToFrame = new Java2DFrameConverter();
                    Frame frnew = mt.convert(imgResult);
                    outRec.record(frnew);
                }
            }
            outRec.stop();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(VideoParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FrameRecorder.Exception exR) {
            Logger.getLogger(VideoParser.class.getName()).log(Level.SEVERE, null, exR);
        }
        return cntr;
    }
}
