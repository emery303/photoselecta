package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.CategorizedPhotoAdapter;
import hu.oe.nik.tdxawx.photoselecta.adapters.TaggedPhotoAdapter;
import hu.oe.nik.tdxawx.photoselecta.adapters.ViewPhotosAdapter;
import hu.oe.nik.tdxawx.photoselecta.utility.Constants;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;
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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPhotosByTagActivity extends Activity {

	private Utility utils;
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private ImageView deletebutton;
	private int columnWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_photos_by_tag);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		final ProgressBar loading = (ProgressBar)findViewById(R.id.tagphotosprogress);
		final GridView container = (GridView)findViewById(R.id.photolistbytag);
		
		loading.setVisibility(View.INVISIBLE);
		container.setVisibility(View.INVISIBLE);
		
		TextView tagtext = (TextView) findViewById(R.id.tagtext);
		final MultiAutoCompleteTextView tagname = (MultiAutoCompleteTextView) findViewById(R.id.tagname);
        Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
        tagtext.setTypeface(HelveticaNeueCB);
        tagname.setTypeface(HelveticaNeueCB);
        tagname.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        
        final DatabaseManager db = new DatabaseManager(getApplicationContext());
        final CharSequence[] taglist = db.getTags();
        ArrayAdapter<CharSequence> tagadapter = new ArrayAdapter<CharSequence>(ViewPhotosByTagActivity.this, R.layout.list_item_1, taglist);
        tagname.setAdapter(tagadapter);
        
        tagname.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
        			long arg3) {
        		tagname.dismissDropDown();
        		tagname.clearFocus();
        		loading.setVisibility(View.VISIBLE);
        		final String[] selectedTags = tagname.getText().toString().trim().split(",");
        		new Thread(new Runnable() {
					@Override
					public void run() {
						String[] photos = db.getPhotoPathsByMultipleTags(selectedTags);
						final TaggedPhotoAdapter adapter = new TaggedPhotoAdapter(ViewPhotosByTagActivity.this, 320, 320, photos);
						
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								container.setAdapter(adapter);		
								loading.setVisibility(View.INVISIBLE);
								container.setVisibility(View.VISIBLE);
								container.startAnimation( AnimationUtils.loadAnimation(ViewPhotosByTagActivity.this, R.anim.fadein));
							}
						});
						
					}
				}).start();
        	}
		});
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ViewPhotosByTagActivity.this, MainActivity.class);
		setResult(1, i);
		finish();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Bundle ex = data.getExtras();
			if (ex != null) {
				boolean must_revalidate = ex.getBoolean("MUST_REVALIDATE");
				if (must_revalidate) {
					finish();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
