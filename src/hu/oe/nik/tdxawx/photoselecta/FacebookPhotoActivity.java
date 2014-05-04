package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.utility.FacebookManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class FacebookPhotoActivity extends Activity {

	FacebookManager fbm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.postingtofacebook);
		
		TextView txt = (TextView) findViewById(R.id.fbtext);
        Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
        txt.setTypeface(HelveticaNeueCB);
		
		fbm = new FacebookManager(FacebookPhotoActivity.this);
		Intent i = getIntent();
		Bundle b = i.getExtras();
		if (b != null) {
			String path = b.getString("path");
			String message = b.getString("message");
			if (path != null && path != "") {
				fbm.PostPhotoToFacebook(path, message);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fbm.onActivityResult(requestCode, resultCode, data);
	}
}
