package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.TaggedPhotoAdapter;
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings.TextSize;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPhotosByTagActivity extends Activity {

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
		setContentView(R.layout.view_photos_by_tag);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		buildPhotoList();
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ViewPhotosByTagActivity.this, MainActivity.class);
		setResult(1, i);
		finish();
	}
	
	public void buildPhotoList() {
		Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
		LinearLayout layout = (LinearLayout)findViewById(R.id.tagphotolist);
		DatabaseManager db = new DatabaseManager(getApplicationContext());
		db.assignRandomTags();
		db.getTag2Photos();
		CharSequence[] tags = db.getTags();
		for (int i = 0; i < tags.length; i++) {
			TaggedPhotoAdapter adapter = new TaggedPhotoAdapter(ViewPhotosByTagActivity.this, 320, tags[i]);
			if (adapter.getCount() > 0) {
				TextView tv = new TextView(getApplicationContext());
				tv.setText(tags[i]);
				tv.setTypeface(HelveticaNeueCB);
				tv.setBackgroundColor(Color.DKGRAY);
				tv.setTextColor(Color.WHITE);
				tv.setPadding(8, 8, 8, 8);
				tv.setTextSize(18.0f);
				tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				layout.addView(tv);
				GridView gv = new GridView(getApplicationContext());
				gv.setNumColumns(3);
				gv.setPadding(8, 8, 8, 8);
				gv.setAdapter(adapter);
				layout.addView(gv);
			}
		}
	}

}
