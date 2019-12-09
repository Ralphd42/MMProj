/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.projecttools;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacv.FrameRecorder;

/**
 *
 * @author ralph
 */
public class Tools {
    static final boolean UseGui =false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(  args[0].compareTo("all")==0  )
        {
            VideoParser vp = new VideoParser();
            Path p = Paths.get(args[1]);
            p.getParent();
            Path par = p.getParent();
            vp.getAllFilesFromVide(args[1], par.toString());
        }
        if(args[0].compareTo("avg")==0      )
        {
            VideoParser vp =new VideoParser();
            vp.GetAverageImageForVideo(args[1], args[2]);
        
        
        
        
        
        
        }
        
        if(args[0].compareTo("mot")==0      )
        {
            //public int RemoveAvgFromVid(String inputVideo, String outputVideo, String avgFileLocation)
            VideoParser vp =new VideoParser();
            vp.RemoveAvgFromVid( args[1],args[2],args[3]);
            
            
        
        
        
        
        }
        
        else{
        
        
        VideoParser p = new VideoParser();
        int fc =p.SplitVideo(
            "/home/ralph/development/fall2019Classes/mm/MMProj/SY_00004.AVI",
            "/home/ralph/development/fall2019Classes/mm/MMProj/MpTEST.mp4", 
            0, 500);
        /*
        System.out.println("cnt = " +fc );
        if(args.length>0){
        int fc1 = p.FrameCount("/home/ralph/development/fall2019Classes/mm/MMProj/SY_00004.AVI");
        p.GetNFilesFromVideo("/home/ralph/development/fall2019Classes/mm/MMProj/SY_00004.AVI",
                
                "/home/ralph/development/fall2019Classes/mm/MMProj/pics", 10);
        
        */
        try {
            p.SummarizeShot
                ("/home/ralph/development/fall2019Classes/mm/MMProj/pics/img6.jpg",
                        "/home/ralph/development/fall2019Classes/mm/MMProj/Mp400004.mp4",
                        32, "/home/ralph/development/fall2019Classes/mm/MMProj/summary.MP4",
                        "/home/ralph/development/fall2019Classes/mm/MMProj/summary.txt", 1.01, 500, 290);
        } catch (FrameRecorder.Exception ex) {
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
 //       }
        if(UseGui){
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ToolFrame().setVisible(true);
            }
        });}
    }
    
}
