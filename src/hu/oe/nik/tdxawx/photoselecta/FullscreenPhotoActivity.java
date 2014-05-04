package hu.oe.nik.tdxawx.photoselecta;

import java.util.ArrayList;

import hu.oe.nik.tdxawx.photoselecta.adapters.FullscreenPhotoAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.BluetoothManager;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;
import hu.oe.nik.tdxawx.photoselecta.utility.FacebookManager;
import hu.oe.nik.tdxawx.photoselecta.utility.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FullscreenPhotoActivity extends Activity{

	private FullscreenPhotoAdapter adapter;
	private ViewPager viewPager;
	String current_photo_path;
	Intent intent;
	FacebookManager fbm;
	BluetoothManager bm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen_photo);
		
		fbm = new FacebookManager(FullscreenPhotoActivity.this);
		bm = new BluetoothManager(FullscreenPhotoActivity.this);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		viewPager = (ViewPager) findViewById(R.id.fullscreen_photo_pager);

		intent = getIntent();
		current_photo_path = intent.getStringExtra("path");

		ArrayList<String> selectedPhoto = new ArrayList<String>(); 
		selectedPhoto.add(current_photo_path);
		
		adapter = new FullscreenPhotoAdapter(FullscreenPhotoActivity.this, selectedPhoto);

		viewPager.setAdapter(adapter);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.photo_item_menu, menu);
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.photo_item_menu_delete:
				new AlertDialog.Builder(FullscreenPhotoActivity.this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Delete photo")
		        .setMessage("Are you sure you want to delete this photo?")
		        .setPositiveButton("Yes, delete", new DialogInterface.OnClickListener()
		        {
		        	@Override
			        public void onClick(DialogInterface dialog, int which) {
		        		DatabaseManager db = new DatabaseManager(getApplicationContext());
		        		db.deletePhotoByPath(current_photo_path);
		        		Toast.makeText(getApplicationContext(), "Photo deleted.", Toast.LENGTH_SHORT).show();
		        		intent.putExtra("MUST_REVALIDATE", true);
		        		setResult(1, intent);
		        		finish();
			        }
		    	})
			    .setNegativeButton("No", null)
			    .show();
				break;
			
			case R.id.photo_item_menu_facebook:
				fbm.PostPhotoToFacebook(current_photo_path, "Posted from FullscreenPhotoActivity");
				break;
				
			case R.id.photo_item_menu_bluetooth:
				bm.SendFile(current_photo_path);
				break;
				
			default:
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fbm.onActivityResult(requestCode, resultCode, data);
		bm.onActivityResult(requestCode, resultCode, data);
	}
}
