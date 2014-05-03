package hu.oe.nik.tdxawx.photoselecta.utility;

import hu.oe.nik.tdxawx.photoselecta.MainActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class FacebookManager {

	final String[] _scope = new String[]{"publish_actions,publish_stream,offline_access"};
	Session session;
	Facebook fb;
	Activity _activity;
	
	public FacebookManager(Activity a, String app_id) {
		this.fb = new Facebook(app_id);
		this._activity = a;
	}
	
	public void PostPhotoToFacebook(String path, String message) {
		
		final String photo_path = path;
		final String post_message = message;
		
		fb.authorize(_activity, _scope, new Facebook.DialogListener() {
    		@Override
    		public void onFacebookError(FacebookError e) {
    			Log.d("PS FBERROR", "EXCEPT "+e.getMessage());
    		}
    		@Override
    		public void onComplete(Bundle values) {
    			Log.d("PS FB", "EXCEPT auth OK.");
    			
    			try {

    				  Session.openActiveSession(_activity, true, new Session.StatusCallback() {
    				    @Override
    				    public void call(Session session, SessionState state, Exception exception) {
    				    	final Session s = session;
    				    	Log.d("PS FBCOMP", "EXCEPT Session call. State: "+state.toString());
    						if (exception != null)
    							Log.d("PS FBCOMP", "EXCEPT Session exception: "+exception.getMessage());
    						if (state == SessionState.OPENED) {
    							Log.d("PS FBCOMP", "EXCEPT Session opened, posting.");
    							Bitmap post_image = BitmapFactory.decodeFile(photo_path);
    							Request rq = Request.newUploadPhotoRequest(s, post_image, uploadPhotoRequestCallback);
    							Bundle parameters = rq.getParameters();
    					        parameters.putString("message", post_message);
    					        rq.setParameters(parameters);
    					        rq.executeAsync();
    						}
    				    }
    				  });
    			} catch (Exception e) {
    				Log.d("PS FBCOMP", "EXCEPT "+e.getMessage());
    			}
    		}
    		@Override
    		public void onError(DialogError e) {
    			Log.d("PS FBDIALOGERROR", "EXCEPT "+e.getMessage());
    		}
    		@Override
    		public void onCancel() {
    			Log.d("PS FBCANCEL", "EXCEPT CANCELLED");
    		}
    	});
	}
	
	Request.Callback uploadPhotoRequestCallback = new Request.Callback() {
	    @Override
	    public void onCompleted(Response response) {
	        if (response.getError() != null) { 
	            Log.d("PS FB", "EXCEPT Post error:" + response.getError().getErrorMessage() + " " + response.getError().getException().getMessage());
	        } else{
	             String idRploadResponse = (String) response.getGraphObject().getProperty("id");
	             if (idRploadResponse!= null) { 
	                String fbPhotoAddress = "https://www.facebook.com/photo.php?fbid=" +idRploadResponse;
	                Toast.makeText(_activity.getApplicationContext(), "Photo has been successfully posted to Facebook!", Toast.LENGTH_SHORT).show();
	                Log.d("PS FB", "EXCEPT posted: "+fbPhotoAddress);
	             } else { 
	            	Log.d("PS FB", "EXCEPT response ID error");
	             } 
	        }
	    }
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		fb.authorizeCallback(requestCode, resultCode, data);
		Session session = Session.getActiveSession();
		if (session != null && !session.isOpened()) {
			session.onActivityResult(_activity, requestCode, resultCode, data);
		}
	}
}