package hu.oe.nik.tdxawx.photoselecta.imaging;

import hu.oe.nik.tdxawx.photoselecta.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.util.Log;

public class CvImageProcessor {

	private ColorBlobDetector    mDetector;
	private CascadeClassifier      mJavaFaceDetector;
    private File mCascadeFile_face;
    private CascadeClassifier mJavaCarDetector1;
    private CascadeClassifier mJavaCarDetector2;
    private CascadeClassifier mJavaCarDetector3;
    private CascadeClassifier mJavaCarDetector4;
    private File mCascadeFile_car1;
    private File mCascadeFile_car2;
    private File mCascadeFile_car3;
    private File mCascadeFile_car4;
	
	public CvImageProcessor(Context ctx) {
		mDetector = new ColorBlobDetector();
		try {
            // load cascade files from application resources
            
			InputStream is1 = ctx.getResources().openRawResource(R.raw.face);
			InputStream isc1 = ctx.getResources().openRawResource(R.raw.car1);
			InputStream isc2 = ctx.getResources().openRawResource(R.raw.car2);
			InputStream isc3 = ctx.getResources().openRawResource(R.raw.car3);
			InputStream isc4 = ctx.getResources().openRawResource(R.raw.car4);
            File cascadeDir = ctx.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile_face = new File(cascadeDir, "face.xml");
            mCascadeFile_car1 = new File(cascadeDir, "car1.xml");
            mCascadeFile_car2 = new File(cascadeDir, "car2.xml");
            mCascadeFile_car3 = new File(cascadeDir, "car3.xml");
            mCascadeFile_car4 = new File(cascadeDir, "car4.xml");
            FileOutputStream os1 = new FileOutputStream(mCascadeFile_face);
            FileOutputStream osc1 = new FileOutputStream(mCascadeFile_car1);
            FileOutputStream osc2 = new FileOutputStream(mCascadeFile_car2);
            FileOutputStream osc3 = new FileOutputStream(mCascadeFile_car3);
            FileOutputStream osc4 = new FileOutputStream(mCascadeFile_car4);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is1.read(buffer)) != -1) {
                os1.write(buffer, 0, bytesRead);
            }
            is1.close();
            os1.close();
            
            while ((bytesRead = isc1.read(buffer)) != -1) { osc1.write(buffer, 0, bytesRead); }
            isc1.close(); osc1.close();
            while ((bytesRead = isc2.read(buffer)) != -1) { osc2.write(buffer, 0, bytesRead); }
            isc2.close(); osc2.close();
            while ((bytesRead = isc3.read(buffer)) != -1) { osc3.write(buffer, 0, bytesRead); }
            isc3.close(); osc3.close();
            while ((bytesRead = isc4.read(buffer)) != -1) { osc4.write(buffer, 0, bytesRead); }
            isc4.close(); osc4.close();

            mJavaFaceDetector = new CascadeClassifier(mCascadeFile_face.getAbsolutePath());
            if (mJavaFaceDetector.empty()) {
                Log.e("PS-CASCADE", "Failed to load cascade classifier");
                mJavaFaceDetector = null;
            }
            
            mJavaCarDetector1 = new CascadeClassifier(mCascadeFile_car1.getAbsolutePath());
            if (mJavaCarDetector1.empty())
                mJavaCarDetector1 = null;
            mJavaCarDetector2 = new CascadeClassifier(mCascadeFile_car2.getAbsolutePath());
            if (mJavaCarDetector2.empty())
                mJavaCarDetector2 = null;
            mJavaCarDetector3 = new CascadeClassifier(mCascadeFile_car3.getAbsolutePath());
            if (mJavaCarDetector3.empty())
                mJavaCarDetector3 = null;
            mJavaCarDetector4 = new CascadeClassifier(mCascadeFile_car4.getAbsolutePath());
            if (mJavaCarDetector4.empty())
                mJavaCarDetector4 = null;
            
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("PS-CASCADE", "Failed to load cascade. Exception thrown: " + e);
        }
	}
	
	public CharSequence determineCategory(Mat m, Mat mg) {
    	CharSequence cat = "";
    	int smallfacesize = (int)(m.size().height * 0.2);
    	int largefacesize = (int)(m.size().height * 0.5);
    	int carsize = (int)(m.size().height * 0.35);
    	MatOfRect smallfaces = new MatOfRect();
    	MatOfRect largefaces = new MatOfRect();
    	MatOfRect cars1 = new MatOfRect();
    	MatOfRect cars2 = new MatOfRect();
    	MatOfRect cars3 = new MatOfRect();
    	MatOfRect cars4 = new MatOfRect();
    	// face detection
    	mJavaFaceDetector.detectMultiScale(mg, smallfaces, 1.1, 2, 2, new Size(smallfacesize, smallfacesize), new Size());
    	mJavaFaceDetector.detectMultiScale(mg, largefaces, 1.1, 2, 2, new Size(largefacesize, largefacesize), new Size());
    	// car detection
    	mJavaCarDetector1.detectMultiScale(mg, cars1, 1.1, 2, 2, new Size(carsize, carsize), new Size());
    	mJavaCarDetector2.detectMultiScale(mg, cars2, 1.1, 2, 2, new Size(carsize, carsize), new Size());
    	mJavaCarDetector3.detectMultiScale(mg, cars3, 1.1, 2, 2, new Size(carsize, carsize), new Size());
    	mJavaCarDetector4.detectMultiScale(mg, cars4, 1.1, 2, 2, new Size(carsize, carsize), new Size());
    	
    	double snow = 0;
    	double sky = 0;
    	double yellow = 0;
    	double red = 0;
    	double sand = 0;
    	double dark = 0;
    	double sfaces = smallfaces.toArray().length;
    	double lfaces = largefaces.toArray().length;
    	double tcars = cars1.toArray().length + cars2.toArray().length + cars3.toArray().length + cars4.toArray().length;
    	double earth = 0;
    	double green = 0;
    	String debugtext = "";
    	List<MatOfPoint> cntr;
    		
		// snow component
    	mDetector.setColorRadius(new Scalar(127,50,55));
    	mDetector.setHsvColor(new Scalar(127,0,200));
        mDetector.process(m);
        snow = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        //cntr = mDetector.getContours();
        //Imgproc.drawContours(m, cntr, -1, new Scalar(120,120,120,255), 2);
       
        // sand component
    	mDetector.setColorRadius(new Scalar(30,120,110));
    	mDetector.setHsvColor(new Scalar(30,120,230));
        mDetector.process(m);
        sand = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        //cntr = mDetector.getContours();
        //Imgproc.drawContours(m, cntr, -1, new Scalar(255,255,0,255), 2);
        
        // sky component
    	mDetector.setColorRadius(new Scalar(25,150,90));
    	mDetector.setHsvColor(new Scalar(155,255,180));
        mDetector.process(m);
        sky = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        //cntr = mDetector.getContours();
        //Imgproc.drawContours(m, cntr, -1, new Scalar(0,0,255,255), 2);
        
        // yellow component
    	mDetector.setColorRadius(new Scalar(25,150,70));
    	mDetector.setHsvColor(new Scalar(40,255,180));
        mDetector.process(m);
        yellow = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        //cntr = mDetector.getContours();
        //Imgproc.drawContours(m, cntr, -1, new Scalar(255,255,0,255), 2);
        
        // red component
    	mDetector.setColorRadius(new Scalar(15,200,150));
    	mDetector.setHsvColor(new Scalar(0,255,100));
        mDetector.process(m);
        red = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        //cntr = mDetector.getContours();
        //Imgproc.drawContours(m, cntr, -1, new Scalar(255,0,0,255), 2);
        
        // earth component
    	mDetector.setColorRadius(new Scalar(20,80,100));
    	mDetector.setHsvColor(new Scalar(44,78,60));
        mDetector.process(m);
        earth = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        //cntr = mDetector.getContours();
        //Imgproc.drawContours(m, cntr, -1, new Scalar(127,140,0,255), 2);
        
     	// green component
    	mDetector.setColorRadius(new Scalar(20,110,110));
    	mDetector.setHsvColor(new Scalar(70,190,150));
        mDetector.process(m);
        green = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        //cntr = mDetector.getContours();
        //Imgproc.drawContours(m, cntr, -1, new Scalar(0,255,0,255), 2);
        
        // dark component
    	mDetector.setColorRadius(new Scalar(255,40,80));
    	mDetector.setHsvColor(new Scalar(0,0,0));
        mDetector.process(m);
        dark = ((mDetector.getContourArea() / (m.size().width*m.size().height)) * 100) / 5;
        
        cat = "Uncategorized";
        
        if (lfaces > 0)
			cat = "#selfie";
        else if (green > 0.5 && earth > 0.25)
        	cat = "#forest";
        else if (sand > 0.3 && sky > 0.4 && green > 0.01)
			cat = "#beach";
        else if (snow > 0.4 && sky > 0.3)
        	cat = "#snow";
		else if (yellow > 0.2 && red > 0.4)
			cat = "#sunset";
		else if (sfaces > 0)
			cat = "#people";
		else if (tcars > 1)
			cat = "#cars";
		else if (dark > 0.5 && (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 21 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 4) )
			cat = "#night";
        
        return cat;
    }
	
	public Mat determineShape(Mat m, Mat mg) {
    	mDetector.setColorRadius(new Scalar(255,20,100));
    	mDetector.setHsvColor(new Scalar(0,0,40));
    	mDetector.process(m);
    	List<MatOfPoint> cntr = mDetector.getContours();
    	List<MatOfPoint> newcntr = mDetector.getContours();
    	//newcntr.clear();
    	Log.d("","PS SHAPE contour matrix size: "+cntr.size());
    	for (int i = 0; i < cntr.size(); i++) {
    		MatOfPoint2f m2f = new MatOfPoint2f( cntr.get(i).toArray() );
    		MatOfPoint2f m2fr = new MatOfPoint2f();
    		Imgproc.approxPolyDP(m2f, m2fr, 0.02*Imgproc.arcLength(m2f, true), true);
    		Log.d("","PS SHAPE contour approx length: "+m2fr.toArray().length);
    		if (m2fr.toArray().length > 2 && m2fr.toArray().length < 5) {
    			newcntr.clear();
    			newcntr.add(new MatOfPoint(m2fr.toArray()));
    			Imgproc.drawContours(m, newcntr, -1, new Scalar(0,255,0,255), 3);
    		}
    	}
    	return m;
    }
    
}
