package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.ViewPhotosAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.Constants;
import hu.oe.nik.tdxawx.photoselecta.utility.Utility;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.GridView;

public class ViewPhotosActivity extends Activity {

	private Utility utils;
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private ViewPhotosAdapter adapter;
	private GridView gridView;
	private int columnWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_photos);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		gridView = (GridView) findViewById(R.id.view_photos_grid);
		utils = new Utility(this);
		InitilizeGridLayout();
		imagePaths = utils.getFilePaths();
		adapter = new ViewPhotosAdapter(ViewPhotosActivity.this, imagePaths, columnWidth);
		gridView.setAdapter(adapter);
	}

	private void InitilizeGridLayout() {
		Resources r = getResources();
		float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constants.GRID_PADDING, r.getDisplayMetrics());
		columnWidth = (int)((utils.getScreenWidth() - ((Constants.NUM_OF_COLUMNS + 1) * padding)) / Constants.NUM_OF_COLUMNS);
		gridView.setNumColumns(Constants.NUM_OF_COLUMNS);
		gridView.setColumnWidth(columnWidth);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setPadding((int)padding, (int)padding, (int)padding, (int)padding);
		gridView.setHorizontalSpacing((int)padding);
		gridView.setVerticalSpacing((int)padding);
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ViewPhotosActivity.this, MainActivity.class);
		setResult(1, i);
		finish();
	}

}
