package hu.oe.nik.tdxawx.photoselecta;

import java.util.ArrayList;

import hu.oe.nik.tdxawx.photoselecta.adapters.FullscreenPhotoAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.Utility;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class FullscreenPhotoActivity extends Activity{

	private Utility utils;
	private FullscreenPhotoAdapter adapter;
	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen_photo);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		viewPager = (ViewPager) findViewById(R.id.fullscreen_photo_pager);

		utils = new Utility(getApplicationContext());

		Intent i = getIntent();
		int position = i.getIntExtra("position", 0);

		ArrayList<String> selectedPhoto = new ArrayList<String>(); 
		selectedPhoto.add(utils.getFilePaths().get(position));
		
		adapter = new FullscreenPhotoAdapter(FullscreenPhotoActivity.this, selectedPhoto);

		viewPager.setAdapter(adapter);

		// displaying selected image first
		viewPager.setCurrentItem(position);
	}
}
