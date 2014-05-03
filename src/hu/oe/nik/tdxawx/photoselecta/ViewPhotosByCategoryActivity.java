package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.CategorizedPhotoAdapter;
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
import android.graphics.drawable.GradientDrawable.Orientation;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings.TextSize;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPhotosByCategoryActivity extends Activity {

	private ScrollView container;
	private ProgressBar progress;
	private TextView progresstext;
	Typeface HelveticaNeueCB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_photos_by_category);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
		container = (ScrollView)findViewById(R.id.categoryphotocontainer);
		progress = (ProgressBar)findViewById(R.id.catphotos_loading);
		progresstext = (TextView)findViewById(R.id.catphotos_loadingtext);
		progresstext.setTypeface(HelveticaNeueCB);
		buildPhotoList();
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ViewPhotosByCategoryActivity.this, MainActivity.class);
		setResult(1, i);
		finish();
	}
	
	public void buildPhotoList() {		
		final DatabaseManager db = new DatabaseManager(getApplicationContext());
		
		new Thread(new Runnable(){
        	public void run() {
        		final LinearLayout layout = new LinearLayout(ViewPhotosByCategoryActivity.this);
        		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        		layout.setOrientation(LinearLayout.VERTICAL);
        		CharSequence[] categories = db.getCategories();
        		for (int i = 0; i < categories.length; i++) {
        			CategorizedPhotoAdapter adapter = new CategorizedPhotoAdapter(ViewPhotosByCategoryActivity.this, 320, 320, categories[i]);
        			if (adapter.getCount() > 0) {
        				TextView tv = new TextView(getApplicationContext());
        				tv.setText(categories[i]);
        				tv.setTypeface(HelveticaNeueCB);
        				tv.setBackgroundColor(Color.DKGRAY);
        				tv.setTextColor(Color.WHITE);
        				tv.setPadding(8, 8, 8, 8);
        				tv.setTextSize(18.0f);
        				tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        				layout.addView(tv);
        				HorizontalScrollView hscv = new HorizontalScrollView(getApplicationContext());
        				LinearLayout ll = new LinearLayout(getApplicationContext());
        				ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        				for (int n = adapter.getCount()-1; n >= 0; n--) {
        					ImageView iv = (ImageView)adapter.getView(n, null, null);
        					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        					lp.setMargins(8, 8, 8, 8);
        					iv.setLayoutParams(lp);
        					ll.addView(iv);
        				}
        				hscv.addView(ll);
        				layout.addView(hscv);
        			}
        		}
        		
        		runOnUiThread(new Runnable(){ 
        			@Override
        			public void run() {
        				Animation fadein = AnimationUtils.loadAnimation(ViewPhotosByCategoryActivity.this, R.anim.fadein);
        				progress.setVisibility(View.INVISIBLE);
        				progresstext.setVisibility(View.INVISIBLE);
        				container.addView(layout);
        				container.setVisibility(View.VISIBLE);
        				container.startAnimation(fadein);
        			}
        		});
        	
        	}
		}).start();

	}

}
