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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class AnalyzerAdapter extends BaseAdapter {  
	 private Context context;  
	 private ArrayList<Photo> photos;
	 
	 private static final int THUMBNAIL_WIDTH = 400;
	 private static final int THUMBNAIL_HEIGHT = 300;
	 
	 private GestureDetector gd;
	 private DatabaseManager db;
	  
	 public AnalyzerAdapter (Context c, ArrayList<Photo> photos) {  
		 this.context = c;  
		 this.photos = photos;
		 this.gd = new GestureDetector(new SwipeListener(c));
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
	    	 iv.setPadding(8,8,8,8);
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
	    	 //bitmap = p.makeThumbnailWithMeta(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
	    	 bitmap = Bitmap.createScaledBitmap(p.getBitmap(), THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, false);
	     }
	     iv.setImageBitmap(bitmap);
	  
		 if (p.Brightness == 1) {
			 iv.setBackgroundColor(Color.WHITE);
		 } else {
			 iv.setBackgroundColor(Color.BLACK);
		 }
		 
		 db = new DatabaseManager(context);
		 int photo_dbid = db.getPhotoIdByPath(p.getPath());
		 
		 iv.setId(photo_dbid);
		 final int itempos = position; 
		 
		 iv.setOnTouchListener(new View.OnTouchListener() {			 
			@Override
			public boolean onTouch(final View view1, final MotionEvent event) {
				if (gd.onTouchEvent(event)) {
					if (photos.size() > 1) {
						if (photos.get(itempos).bestInSession)
							Toast.makeText(context, "You can't delete the best photo!", Toast.LENGTH_SHORT).show();
						else {
							view1.startAnimation( AnimationUtils.loadAnimation(context, R.anim.fadeout) );
							view1.postDelayed(new Runnable() {
								
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
								}
							}, 250);
						}
					}
				} else {
					
				}
				return false;
			}
		  });
		 
		 /*
		 iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(v.getContext(), "click!", Toast.LENGTH_SHORT).show();
			}
		 });
		 */
		  
		 return iv;  
	 }  
}  