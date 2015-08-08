package com.nkfust.im.housesearchapp;

import android.util.Log;

import com.nkfust.im.housesearchapp.util.Tool;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ORB {
	
	private static final String TAG="ORB_Matching::ORB.java";
	private Tool tool;
	private byte data[];
	private Mat image;
	private Mat descriptors;
	private FeatureDetector detector;
	private MatOfKeyPoint keypoints;
	private DescriptorExtractor descriptor;
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	private File file;

	public void getTool(Tool tool) {
		this.tool = tool;
	}

//	public void createDB(String filepath, String filename) {
	//給圖片檔案路徑
	public void createDB(String filepath) {
		Log.i(TAG,"createDB");
		//取得該圖片的des路徑
		String desfilepath = tool.editExtension(filepath, "des");
		Log.i(TAG,"desfilepath:"+desfilepath);
		
		detector = FeatureDetector.create(FeatureDetector.ORB);
		descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		keypoints = new MatOfKeyPoint();
		descriptors = new Mat();
//		image = Highgui.imread(filepath + "/" + filename + ".jpg", 0);
		image = Highgui.imread(filepath, 0);
		detector.detect(image, keypoints);
		descriptor.compute(image, keypoints, descriptors);
		data = new byte[(int) (descriptors.total() * descriptors.channels())];
		descriptors.get(0, 0, data);
		try {
			bos = new BufferedOutputStream(new FileOutputStream(desfilepath));
			bos.write(data);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		image.release();
		descriptors.release();
		keypoints.release();
		Log.i(TAG,"finish createDB");
	}
	
//	public Mat resdDB(String filepath, String filename) {
	//給圖片檔案路徑
	public Mat resdDB(String filepath) {
		Log.i(TAG,"filepath:"+filepath);
		//取得該圖片的des路徑
		String desfilepath = tool.editExtension(filepath, "des");
		
		descriptors = new Mat();
		try {
//			file = new File(filepath + "/" + filename + ".des");
			file = new File(desfilepath);
			data = new byte[(int) file.length()];
			bis = new BufferedInputStream(new FileInputStream(file));
			bis.read(data);
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		descriptors = new Mat(data.length / 32, 32, CvType.CV_8UC1);
		descriptors.put(0, 0, data);
		return descriptors;
	}


}
