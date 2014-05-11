package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.imaging.ColorBlobDetector;
import hu.oe.nik.tdxawx.photoselecta.imaging.CvImageProcessor;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CvCameraActivity extends Activity implements /*OnTouchListener,*/ CvCameraViewListener2 {

    //private boolean              mIsColorSelected = false;
    private Mat mRgba;
    private Mat mGray;
    private Scalar mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    
    private CvImageProcessor proc;
    
    private MediaPlayer _shuttersound;
    FileOutputStream fos;
    Date date;
    boolean busy;
    private boolean took_photos = false;
	private long SESSION_ID;
	private TextView categorytext;
	private String selectedCategory = "";

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("PS-OPENCV", "OpenCV loaded successfully");
                    
                    //System.loadLibrary("detection_based_tracker");
                    
                    proc = new CvImageProcessor(getApplicationContext());
                    
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CvCameraActivity() {
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cvcameraview);
        
        setTitle("Take photos - category mode");
        SESSION_ID = Calendar.getInstance().getTimeInMillis();
		_shuttersound = MediaPlayer.create(CvCameraActivity.this, R.raw.shutter);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cvcamsurface);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        Button btn_shutter = (Button)findViewById(R.id.btn_shutter);
        Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
        btn_shutter.setTypeface(HelveticaNeueCB);
		btn_shutter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TakePhoto();
			}
		});
		
		categorytext = (TextView)findViewById(R.id.categorytext);
        categorytext.setTypeface(HelveticaNeueCB);
        categorytext.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
    	
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        //mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	mRgba = inputFrame.rgba();
    	mGray = inputFrame.gray();
    	/*if (proc != null) {
	    	CharSequence cat = proc.determineCategory(mRgba, mGray);
	    	this.selectedCategory = cat.toString();
	        updateCategoryText(cat, "");
    	}*/
        return mRgba;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
    
    private Scalar convertScalarRgba2Hsv(Scalar rgbaColor) {
        Mat pointMatHsv = new Mat();
        Mat pointMatRgba = new Mat(1, 1, CvType.CV_8UC4, rgbaColor);
        Imgproc.cvtColor(pointMatRgba, pointMatHsv, Imgproc.COLOR_RGB2HSV_FULL, 3);

        return new Scalar(pointMatHsv.get(0, 0));
    }
    
    @Override
	public void onBackPressed() {
		Intent i = new Intent(CvCameraActivity.this, MainActivity.class);
		if (this.took_photos) {
			i.putExtra("CAMERA_DONE_PHOTOS", "YES");
		} else {
			i.putExtra("CAMERA_DONE_PHOTOS", "NO");
		}
		
		i.putExtra("SESSION_MODE", "CATEGORY");
		
		setResult(1, i);
		finish();
	}
    
    public void TakePhoto() {
    	if (!busy) {
          busy = true;
    	  Mat imageMatrix = new Mat();

    	  Imgproc.cvtColor(mRgba, imageMatrix, Imgproc.COLOR_RGBA2BGR, 3);
    	  
    	  	if (proc != null) {
	  	    	CharSequence cat = proc.determineCategory(mRgba, mGray);
	  	    	this.selectedCategory = cat.toString();
	  	        //updateCategoryText(cat, "");
	  	    	runOnUiThread(new Runnable() {
	  	    	     @Override
	  	    	     public void run() {
	  	    	    	 categorytext.setText(selectedCategory);
	  	    	    	 categorytext.setVisibility(View.VISIBLE);
	  	    	    	 categorytext.startAnimation( AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout) );
	  	    	    	 categorytext.setVisibility(View.INVISIBLE);
	  	    	    }
	  	    	});
	      	}

			try {
				date = new Date();
				String fname = "/photoselecta/"+date.getTime()+".jpg";
				String path = Environment.getExternalStorageDirectory().getAbsolutePath().concat(fname);
				File file = new File(path);
				Highgui.imwrite(file.toString(), imageMatrix);
				
				DatabaseManager db = new DatabaseManager(CvCameraActivity.this);
				int photo_id = db.insertPhoto(path, SESSION_ID);
				Log.d("PS-DB", "PS Saved photo with ID #"+String.valueOf(photo_id));
				int category_id = db.getCategoryIdByName(this.selectedCategory);
				Log.d("PS-DB", "PS Detected category ID #"+String.valueOf(category_id));
				db.assignCategoryToPhoto(photo_id, category_id);
				db.CloseDB();
				
				if (_shuttersound.isPlaying()) {
					_shuttersound.stop();
		    	}
				_shuttersound.start();
				
				took_photos = true;
			
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(CvCameraActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}

    	  
		  busy = false;
		  Toast.makeText(CvCameraActivity.this, "Picture saved in '"+this.selectedCategory+"'!", Toast.LENGTH_SHORT).show();
		  
	  }	
    }
	
    private void updateCategoryText(CharSequence text, CharSequence area) {
    	CharSequence r = text;
    	if (area != null && area != "")
    		r = r+" ("+area+")";
    	
    	final CharSequence result = r;
    	
    	runOnUiThread(new Runnable() {
    	     @Override
    	     public void run() {
    	    	 categorytext.setText(result);
    	    }
    	});
    }
}
