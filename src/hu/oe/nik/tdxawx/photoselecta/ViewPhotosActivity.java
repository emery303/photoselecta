package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.GalleryAdapter;
import hu.oe.nik.tdxawx.photoselecta.adapters.ViewPhotosAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.Constants;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;
import hu.oe.nik.tdxawx.photoselecta.utility.DraggableGridView;
import hu.oe.nik.tdxawx.photoselecta.utility.Utility;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class ViewPhotosActivity extends Activity {

	private ArrayList<String> imagePaths = new ArrayList<String>();
	private GalleryAdapter adapter;
	private GridView gridView;
	private ImageView deletebutton;
	private DatabaseManager db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photogallery);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		db = new DatabaseManager(getApplicationContext());
		
		deletebutton = (ImageView)findViewById(R.id.btn_deletephoto);
		gridView = (GridView) findViewById(R.id.photogallery);
		imagePaths = db.getAllPhotos(true);
		adapter = new GalleryAdapter(ViewPhotosActivity.this, imagePaths, 320, 320);
		gridView.setAdapter(adapter);

	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ViewPhotosActivity.this, MainActivity.class);
		setResult(1, i);
		finish();
	}

}
