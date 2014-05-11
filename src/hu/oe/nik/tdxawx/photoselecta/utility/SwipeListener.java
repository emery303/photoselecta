package hu.oe.nik.tdxawx.photoselecta.utility;

import android.app.Application;
import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.Toast;

public class SwipeListener extends SimpleOnGestureListener {
	
   private static final int SWIPE_MIN_DISTANCE = 50;
   private static final int SWIPE_THRESHOLD_VELOCITY = 100;
   
   private Context context;
   
   public SwipeListener(Context ctx) {
	   this.context = ctx;
   }
	
   @Override
   public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		    return false; // Right to left
		}  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		    return true; // Left to right
		}
		
		if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		    return false; // Bottom to top
		}  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		    return false; // Top to bottom
		}
		return false;
   }
}