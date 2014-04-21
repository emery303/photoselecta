package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.ViewPhotosAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.Constants;
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
		
		deletebutton = (ImageView)findViewById(R.id.btn_deletephoto);
		grid = (DraggableGridView) findViewById(R.id.view_photos_grid);
		utils = new Utility(this);
		InitilizeGridLayout();
		imagePaths = utils.getFilePaths();
		adapter = new ViewPhotosAdapter(ViewPhotosActivity.this, imagePaths, columnWidth);
		for (int i = 0; i < adapter.getCount(); i++) {
			grid.addView(adapter.getView(i, null, null));
			grid.addPath(String.valueOf(adapter.getItem(i)));
		}
		
		grid.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (v == null) {
					Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
				}
				//if (v.toString() != "")
					//deletebutton.setAlpha(0.5f);
				return false;
			}
		});
		grid.setTouchEndListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				deletebutton.setAlpha(0.0f);
				final int x = (int)event.getX();
				final int y = (int)event.getY();
				if (event != null && (int)event.getY() < 192) {
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					boolean fastdelete = sp.getBoolean("fastdelete", false);
					
					if (!fastdelete){
						new AlertDialog.Builder(ViewPhotosActivity.this)
				        .setIcon(android.R.drawable.ic_dialog_alert)
				        .setTitle("")
				        .setMessage("Delete this photo?")
				        .setPositiveButton("Yes, delete", new DialogInterface.OnClickListener()
				        {
				        	@Override
					        public void onClick(DialogInterface dialog, int which) {
				        		grid.removeViewAt(grid.getIndexFromCoor(x, y));
								Toast.makeText(getApplicationContext(), "photo deleted", Toast.LENGTH_SHORT).show();    
					        }
				    	})
					    .setNegativeButton("Cancel", null)
					    .show();
					} else {
						grid.removeViewAt(grid.getIndexFromCoor(x, y));
						Toast.makeText(getApplicationContext(), "photo deleted", Toast.LENGTH_SHORT).show();
					}
				}
				return false;
			}
		});
		grid.setDeleteHoverListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (v.toString() != "") {
					if (event != null && (int)event.getY() < 192)
						deletebutton.setAlpha(0.9f);
					else
						deletebutton.setAlpha(0.25f);
				}
				return false;
			}
		});
		
		deletebutton.setOnHoverListener(new OnHoverListener() {
			
			@Override
			public boolean onHover(View v, MotionEvent event) {
				v.setAlpha(1);
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
