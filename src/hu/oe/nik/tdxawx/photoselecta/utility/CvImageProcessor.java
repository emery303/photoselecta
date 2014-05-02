package hu.oe.nik.tdxawx.photoselecta.utility;

import hu.oe.nik.tdxawx.photoselecta.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private CascadeClassifier      mJavaDetector;
    private File                   mCascadeFile;
	
	public CvImageProcessor(Context ctx) {
		mDetector = new ColorBlobDetector();
		try {
            // load cascade file from application resources
            InputStream is = ctx.getResources().openRawResource(R.raw.face);
            File cascadeDir = ctx.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "face.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e("PS-CASCADE", "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i("PS-CASCADE", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

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
    	MatOfRect smallfaces = new MatOfRect();
    	MatOfRect largefaces = new MatOfRect();
    	mJavaDetector.detectMultiScale(mg, smallfaces, 1.1, 2, 2, new Size(smallfacesize, smallfacesize), new Size());
    	mJavaDetector.detectMultiScale(mg, largefaces, 1.1, 2, 2, new Size(largefacesize, largefacesize), new Size());
    	
    	double snow = 0;
    	double sky = 0;
    	double yellow = 0;
    	double red = 0;
    	double sand = 0;
    	double dark = 0;
    	double sfaces = smallfaces.toArray().length;
    	double lfaces = largefaces.toArray().length;
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
		else if (dark > 0.6)
			cat = "#night";
		else if (sfaces > 0)
			cat = "#people";
        
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
