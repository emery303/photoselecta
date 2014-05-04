package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

	private Camera _cam;
	private MediaPlayer _shuttersound;
	SurfaceView sv;
	SurfaceHolder sh;
	LayoutInflater inflater;
	boolean inUse = false;
	boolean busy = false;
	Bitmap camera_image;
	Bitmap preview_image;
	Date date;
	FileOutputStream fos;
	boolean previewProcessing = false;
	Handler previewHandler = new Handler(Looper.getMainLooper());
	int previewWidth = 640;
	int previewHeight = 480;
	int FrameCounter = 0; 
	
	private boolean took_photos = false;
	private long SESSION_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cameraview);
		
		setTitle("Take photos - quality mode");
		SESSION_ID = Calendar.getInstance().getTimeInMillis();
		_shuttersound = MediaPlayer.create(CameraActivity.this, R.raw.shutter);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		getWindow().setFormat(PixelFormat.UNKNOWN);
		sv = (SurfaceView) findViewById(R.id.camsurface);
		
		if (sh == null)
			sh = sv.getHolder();
		sh.addCallback(this);
		sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		Button btn_shutter = (Button)findViewById(R.id.btn_shutter);
        Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
        btn_shutter.setTypeface(HelveticaNeueCB);
		btn_shutter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!busy)
					_cam.takePicture(null, onRawPic, onJpgPic);
			}
		});
	}	
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(CameraActivity.this, MainActivity.class);
		if (this.took_photos) {
			i.putExtra("CAMERA_DONE_PHOTOS", "YES");
		} else {
			i.putExtra("CAMERA_DONE_PHOTOS", "NO");
		}
		
		i.putExtra("SESSION_MODE", "SHARPNESS");
		
		setResult(1, i);
		finish();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		_cam.startPreview();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (_cam == null) {
			try {
				_cam = Camera.open();
				_cam.setPreviewDisplay(holder);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (_cam != null) {
			_cam.setPreviewCallback(null);
			_cam.stopPreview();
			_cam.release();
			_cam = null;
			inUse = false;
		}
	}
	
	/*
	 * JPG image data callback
	 */
	PictureCallback onJpgPic = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			busy = true;
			try {
			date = new Date();
			String fname = "/photoselecta/"+date.getTime()+".jpg";
			String path = Environment.getExternalStorageDirectory().getAbsolutePath().concat(fname);
			fos = new FileOutputStream(path);
			fos.write(data);
			fos.flush();
			fos.close();
			//Toast.makeText(CameraActivity.this, "Image saved: "+fname, Toast.LENGTH_SHORT).show();
			
			DatabaseManager db = new DatabaseManager(CameraActivity.this);
			int photo_id = db.insertPhoto(path, SESSION_ID);
			db.assignCategoryToPhoto(photo_id, db.getCategoryIdByName("Uncategorized"));
			db.CloseDB();
			
			took_photos = true;
			
			if (_shuttersound.isPlaying())
				_shuttersound.stop();
			_shuttersound.start();
			
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			camera.startPreview();
			busy = false;
			Toast.makeText(CameraActivity.this, "Picture saved!", Toast.LENGTH_SHORT).show();
		}
	};
	
	/*
	 * RAW image data callback
	 */
	PictureCallback onRawPic = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			//Toast.makeText(CameraActivity.this, "RAW image taken", Toast.LENGTH_SHORT).show();
		}
	};
	
}
