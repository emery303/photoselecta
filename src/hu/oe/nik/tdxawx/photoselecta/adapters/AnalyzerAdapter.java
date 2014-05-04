package hu.oe.nik.tdxawx.photoselecta.adapters;

import hu.oe.nik.tdxawx.photoselecta.Photo;
import hu.oe.nik.tdxawx.photoselecta.R;
import hu.oe.nik.tdxawx.photoselecta.R.drawable;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;
import hu.oe.nik.tdxawx.photoselecta.SwipeListener;

import java.io.File;
import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.gesture.GestureStroke;
import android.gesture.GestureUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path.FillType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AnalyzerAdapter extends BaseAdapter {  
	 private Context context;  
	 private ArrayList<Photo> photos;
	 
	 private static final int THUMBNAIL_WIDTH = 720;
	 private static final int THUMBNAIL_HEIGHT = 540;
	 
	 private GestureDetector gd;
	 private DatabaseManager db;
	 
	 private boolean swipeDeleteEnabled = true;
	  
	 public AnalyzerAdapter (Context c, ArrayList<Photo> photos) {  
		 this.context = c;  
		 this.photos = photos;
		 this.gd = new GestureDetector(new SwipeListener(c));
	 }
	 
	 public void disableSwipeDelete() {
		 swipeDeleteEnabled = false;
	 }
	 
	 public int getCount() {  
	  return this.photos.size();
	 }  
	  
	 public Object getItem(int position) {  
		 return photos.get(position);  
	 }  
	  
	 public long getItemId(int position) {  
		 return position;  
	 }  
	  
	 public View getView(int position, View convertView, ViewGroup parent) {
	     Photo p = photos.get(position);
	     ImageView iv;  
	     if (convertView == null) {    
	    	 iv = new ImageView(context);
	    	 iv.setLayoutParams(new GridView.LayoutParams(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));  
	     } else {  
	    	 iv = (ImageView)convertView;  
	     }
	     
	     Bitmap bitmap;
	     if (p.bestInSession) {
	    	 //Bitmap b = p.makeThumbnailWithMeta(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
	    	 Bitmap b = Bitmap.createScaledBitmap(p.getBitmap(), THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, false);
	    	 Bitmap.Config bitmapConfig = b.getConfig();
	 		 if(bitmapConfig == null) {
	 		    bitmapConfig = Bitmap.Config.RGB_565;
	 		 }
	    	 Bitmap bcopy = b.copy(bitmapConfig, true);
	    	 Canvas c = new Canvas(bcopy);
	    	 Bitmap tick = BitmapFactory.decodeResource(parent.getResources(), R.drawable.tick);
	    	 Paint pnt = new Paint();
	    	 pnt.setStyle(Style.FILL);
	    	 c.drawBitmap(tick, 10, 10, pnt);
	    	 bitmap = bcopy;
	     } else {
	    	 bitmap = Bitmap.createScaledBitmap(p.getBitmap(), THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, false);
	     }
	     iv.setImageBitmap(bitmap);
		 
		 db = new DatabaseManager(context);
		 int photo_dbid = db.getPhotoIdByPath(p.getPath());
		 
		 iv.setId(photo_dbid);
		 final int itempos = position; 
		 
		 iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.performLongClick();
			}
		 });
		 
		 if (swipeDeleteEnabled) {
			 iv.setOnTouchListener(new View.OnTouchListener() {			 
				@Override
				public boolean onTouch(final View v, final MotionEvent event) {
					if (gd.onTouchEvent(event)) {
						if (photos.size() > 0) {
							v.startAnimation( AnimationUtils.loadAnimation(context, R.anim.fadeout) );
							v.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									String path = photos.get(itempos).getPath();
									photos.remove(itempos);
									db = new DatabaseManager(context);
									db.deletePhotoByPath(path);
									db.CloseDB();
									File photo_file = new File(path);
									photo_file.delete();
									AnalyzerAdapter.this.notifyDataSetChanged();
									Toast.makeText(context, "Photo discarded.", Toast.LENGTH_SHORT).show();
								}
							}, 250);
						}
					} else {
						if (event.getAction() == MotionEvent.ACTION_UP) {
							v.performClick();
						}
					}
					return true;
				}
			  });
		 }
		  
		 return iv;  
	 }  
}  