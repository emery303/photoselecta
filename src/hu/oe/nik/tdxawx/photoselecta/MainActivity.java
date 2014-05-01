package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	public static List<String> _photos = new ArrayList<String>();
	public static final String PHOTOS_ORDER = "desc";
	private DatabaseManager db;
	
	public SharedPreferences _preferences;
	
	private ImageView splashpic;
	private TextView splashloader;
	
	// OpenCV init
	private BaseLoaderCallback ocvLoadCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i("PS-OPENCV", "OpenCV loaded successfully");
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, ocvLoadCallback);
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
        
        splashpic = (ImageView)findViewById(R.id.splashpic);
        splashloader = (TextView)findViewById(R.id.splashloaderbar);
        splashloader.setScaleX(0.0f);
		
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this._preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		new SplashScreen().execute();

	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (data != null) {
			Bundle ex = data.getExtras();
			if (ex != null) {
				String cam_result = ex.getString("CAMERA_DONE_PHOTOS");
				String last_session_mode = ex.getString("SESSION_MODE");
				
				if (cam_result != null)
				if (cam_result.equals("YES") && last_session_mode.equals("SHARPNESS")) { 					
					Intent analyzer = new Intent(MainActivity.this, AnalyzerActivity.class);
					analyzer.putExtra("SESSION_MODE", last_session_mode);
					startActivityForResult(analyzer, 1003);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.add(0, 1, 0, "Settings").setIcon(R.drawable.icon_opts_settings);
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(settings, 1003);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static List<String> readPhotosFromDevice(final String direction) {
		List<String> photo_file_names = new ArrayList<String>();
		File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat("/photoselecta"));
		File[] fs = root.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".jpg");
		    }
		});
		
		
		Arrays.sort(fs, new Comparator<File>() {
			public int compare (File a, File b) {
				if (direction.toLowerCase() == "desc") {
					return (int)(b.lastModified() - a.lastModified());
				} else {
					return (int)(a.lastModified() - b.lastModified());
				}
			}
		});
		
		for (File file : fs) {
			photo_file_names.add( file.getPath() );
		}
		
		return photo_file_names;
	}
	
	public static void refreshPhotos() {
		_photos.clear();
		_photos = readPhotosFromDevice(PHOTOS_ORDER);
	}
	
	private class SplashScreen extends AsyncTask<Void, Integer, Void>  
    {      
        @Override  
        protected Void doInBackground(Void... params)  
        {   
        	try  
            {    
                synchronized (this)  
                {    
                    int counter = 0;    
                    while(counter <= 10)  
                    {    
                        this.wait(50);
                        counter++;
                    }  
                }  
            }  
            catch (InterruptedException e)  
            {  
                e.printStackTrace();  
            }
        	publishProgress(0); 
        	// --- create image container folder if not exists
    		File cdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat("/photoselecta")); 
    		if (!cdir.exists()) {
    			cdir.mkdirs();
    		}
    		File tdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat("/photoselecta/thumbs")); 
    		if (!tdir.exists()) {
    			tdir.mkdirs();
    		}
    		publishProgress(25);
    		// open database and check for DB <-> FS inconsistency
    		db = new DatabaseManager(getApplicationContext());   		
    		db.checkFiles();
    		// ---
    		publishProgress(50);
    		_photos = readPhotosFromDevice(PHOTOS_ORDER);
        	
            publishProgress(100);
            return null;  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... values)  
        {  
        	splashloader.setScaleX(((float)values[0])/100);
        }  
    
        @Override  
        protected void onPostExecute(Void result)  
        {    
            InitStartScreen();
        }  
    }
	
	public void InitStartScreen() {
		
		setContentView(R.layout.start);
		
		ImageButton btn_exit = (ImageButton)findViewById(R.id.start_exit);
		ImageButton btn_takenewphotos = (ImageButton)findViewById(R.id.start_takenewphotos);
		ImageButton btn_viewphotos = (ImageButton)findViewById(R.id.start_viewphotos);
		ImageButton btn_settings = (ImageButton)findViewById(R.id.start_settings);
		
		btn_exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				v.startAnimation( (Animation)AnimationUtils.loadAnimation(v.getContext(), R.anim.bounce) );
				new AlertDialog.Builder(MainActivity.this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Exit application")
		        .setMessage("Are you sure you want to exit?")
		        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
		        {
	        	@Override
		        public void onClick(DialogInterface dialog, int which) {
		            finish();    
		        }

		    	})
		    .setNegativeButton("No", null)
		    .show();
			}
		});
		
		btn_takenewphotos.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.startAnimation( (Animation)AnimationUtils.loadAnimation(v.getContext(), R.anim.bounce) );
				final Intent cam = new Intent(MainActivity.this, CameraActivity.class);
				final Intent cvcam = new Intent(MainActivity.this, CvCameraActivity.class);
				new AlertDialog.Builder(MainActivity.this) 
				.setTitle("Choose session mode")
				.setItems(new CharSequence[] {"Sharpness mode", "Category mode"}, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which) {
				        case 0:
				        	cam.putExtra("session_mode", "sharpness");
				        	startActivityForResult(cam, 1001);
				        	break;
				        case 1:
				        	cvcam.putExtra("session_mode", "category");
				        	startActivityForResult(cvcam, 1001);
				        	break;
			        	default:
			        		break;
				        }
				    }
				}).show();
			}
		});
	
		btn_viewphotos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.startAnimation( (Animation)AnimationUtils.loadAnimation(v.getContext(), R.anim.bounce) );
				Intent viewphotos = new Intent(MainActivity.this, ViewPhotosByCategoryActivity.class);
				startActivityForResult(viewphotos, 1002);
			}
		});
		
		btn_settings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.startAnimation( (Animation)AnimationUtils.loadAnimation(v.getContext(), R.anim.bounce) );
				Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(settings, 1003);
			}
		});
	}
	
}