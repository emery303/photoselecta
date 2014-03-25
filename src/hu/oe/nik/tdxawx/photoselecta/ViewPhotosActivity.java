package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.ViewPhotosAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.Constants;
import hu.oe.nik.tdxawx.photoselecta.utility.Utility;
import com.animoto.android.views.*;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class ViewPhotosActivity extends Activity {

	private Utility utils;
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private ViewPhotosAdapter adapter;
	//private GridView gridView;
	private ImageView deletebutton;
	private DraggableGridView grid;
	private int columnWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_photos);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		//gridView = (GridView) findViewById(R.id.view_photos_grid);
		deletebutton = (ImageView)findViewById(R.id.btn_deletephoto);
		grid = (DraggableGridView) findViewById(R.id.view_photos_grid);
		utils = new Utility(this);
		InitilizeGridLayout();
		imagePaths = utils.getFilePaths();
		adapter = new ViewPhotosAdapter(ViewPhotosActivity.this, imagePaths, columnWidth);
		//grid.setAdapter(adapter);
		for (int i = 0; i < adapter.getCount(); i++) {
			grid.addView(adapter.getView(i, null, null));
		}
		
		deletebutton.setOnHoverListener(new OnHoverListener() {
			
			@Override
			public boolean onHover(View v, MotionEvent event) {
				v.setAlpha(1);
				Toast.makeText(getApplicationContext(), "delete photo?", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}

	private void InitilizeGridLayout() {
		Resources r = getResources();
		float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constants.GRID_PADDING, r.getDisplayMetrics());
		columnWidth = (int)((utils.getScreenWidth() - ((Constants.NUM_OF_COLUMNS + 1) * padding)) / Constants.NUM_OF_COLUMNS);
		//grid.setNumColumns(Constants.NUM_OF_COLUMNS);
		//grid.setColumnWidth(columnWidth);
		//grid.setStretchMode(GridView.NO_STRETCH);
		//grid.setPadding((int)padding, (int)padding, (int)padding, (int)padding);
		//grid.setHorizontalSpacing((int)padding);
		//grid.setVerticalSpacing((int)padding);
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ViewPhotosActivity.this, MainActivity.class);
		setResult(1, i);
		finish();
	}

}
