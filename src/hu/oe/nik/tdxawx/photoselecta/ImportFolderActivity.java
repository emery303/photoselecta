package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.AnalyzerAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.CvImageProcessor;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;
import hu.oe.nik.tdxawx.photoselecta.utility.DraggableGridView;
import hu.oe.nik.tdxawx.photoselecta.utility.FolderPicker;
import hu.oe.nik.tdxawx.photoselecta.utility.FolderPicker.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class ImportFolderActivity extends Activity {
	
	private Handler iH = new Handler();
	private TextView progressBar;
	private TextView progressText;

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.importfolder);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        progressText = (TextView)findViewById(R.id.importprogresstext);
        Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
        progressText.setTypeface(HelveticaNeueCB);
        progressBar = (TextView)findViewById(R.id.importprogressbar);
        progressBar.setScaleX(0.0f);
        
        new FolderPicker(ImportFolderActivity.this, new Result() {
			@Override
			public void onChooseDirectory(String dir) {
				importFolder(dir);
			}
			
			public void onCancel(boolean cancelled) {
				if (cancelled) {
					finish();
				}	
			}
		}, null);
        
    }
	
	@Override
	public void onBackPressed() {
		finish();
	};
	
	private void importFolder(String path) {
		final String folderPath = path;
		new Thread(new Runnable(){
        	public void run() {
        		ArrayList<String> files = new ArrayList<String>();
        		File folder = new File(folderPath);
        		DatabaseManager db = new DatabaseManager(ImportFolderActivity.this);
        		File[] filenames = folder.listFiles();
        		long session_id = Calendar.getInstance().getTimeInMillis();
        		for (File file : filenames) {
        	        if (!file.isDirectory() && (file.getName().toLowerCase().endsWith(".bmp") 
        	        		                    || file.getName().toLowerCase().endsWith(".jpg")
        	        		                    || file.getName().toLowerCase().endsWith(".png")
        	        							)) {
    	                files.add(file.getAbsolutePath());
        	        }
        	    }
        		
        		CvImageProcessor proc = new CvImageProcessor(getApplicationContext());
        		int imported = 0;
        		
                for (int i = 0; i < files.size(); i++) {
                	String md5hash;
                	String path = files.get(i);
                	String filenameArray[] = path.split("\\.");
                    String extension = filenameArray[filenameArray.length-1];
                    String filename = String.valueOf(session_id)+"_"+String.valueOf(i)+"."+extension;
                	String newpath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/photoselecta/").concat(filename);
                	try {
	                	FileInputStream in = new FileInputStream(path);
	                	MessageDigest md = MessageDigest.getInstance("MD5");
	                    DigestInputStream dis = new DigestInputStream(in, md);
	                    md5hash = new BigInteger(md.digest()).toString(16);
	                    if (db.checksumExists(md5hash)) {
	                    	Log.d("","PS IMPORT checksum already exists, skipping "+path);
	                    	continue;
	                    }
	                    FileOutputStream out = new FileOutputStream(newpath);
	                    byte[] buf = new byte[1024];
	                    int len;
	                    while ((len = in.read(buf)) > 0) {
	                        out.write(buf, 0, len);
	                    }
	                    in.close();
	                    out.close();
                	} catch (Exception e) {
                		Log.d("", "PS EXCEPTION "+e.getMessage());
                		break;
                	}

                	try {
                		Bitmap b = BitmapFactory.decodeFile(path);
                		Mat image_gray = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC1);
                		Mat image_rgba = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC4);
                		Utils.bitmapToMat(b, image_gray);
                		Utils.bitmapToMat(b, image_rgba);
                		Imgproc.cvtColor(image_gray, image_gray, Imgproc.COLOR_RGB2GRAY);
                		
                		String catname = proc.determineCategory(image_rgba, image_gray).toString();
                		int category_id = db.getCategoryIdByName(catname);
                		int photo_id = db.insertPhoto(newpath, session_id);
                		db.assignCategoryToPhoto(photo_id, category_id);
                		db.insertImportChecksum(md5hash);
                		Log.d("","PS IMPORT imported photo #"+photo_id+", assigned category #"+category_id);
                	} catch (Exception e){
                		Log.d("", e.getMessage());
                	}
                	
	           		final float progress = ((float)i / (float)files.size());
                	
	           		runOnUiThread(new Runnable() {
		           	     @Override
		           	     public void run() {
		           	    	 progressBar.setScaleX(progress);
		           	    	 Log.d("", "Import progress "+progress);
		           	    }
		           	});
	           		
	           		imported++;
                }
                
                final int total_imported = imported;

                runOnUiThread(new Runnable() {
	           	     @Override
	           	     public void run() {
	           	    	 progressText.setText("");
	           	    	 progressBar.setScaleX(0.0f);
	           	    	 AlertDialog a = new AlertDialog.Builder(ImportFolderActivity.this).create();
	           	    	 a.setTitle("Import finished");
	           	    	 if (total_imported > 0) {
	           	    		 a.setMessage("Successfully imported " + total_imported + " photos!");
	           	    	 } else {
	           	    		a.setMessage("No photos to import.");
	           	    	 }
	           	    	 a.setButton(AlertDialog.BUTTON_POSITIVE, "Back to photos", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface d, int which) {
								d.dismiss();
								Intent i = new Intent(ImportFolderActivity.this, MainActivity.class);
								if (total_imported > 0) {
									i.putExtra("IMPORTED_PHOTOS", "YES");
								} else {
									i.putExtra("IMPORTED_PHOTOS", "NO");
								}
								setResult(1, i);
								finish();
							}
	           	    	 });
	           	    	 a.show();
	           	    }
	           	});
                
        	}
        }).start();
	}
	
}
