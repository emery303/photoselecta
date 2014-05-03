package hu.oe.nik.tdxawx.photoselecta.adapters;

import hu.oe.nik.tdxawx.photoselecta.R;
import hu.oe.nik.tdxawx.photoselecta.utility.TouchImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FullscreenPhotoAdapter extends PagerAdapter {

	private Activity _activity;
	private ArrayList<String> _imagePaths;
	private LayoutInflater inflater;

	// constructor
	public FullscreenPhotoAdapter(Activity activity, ArrayList<String> imagePaths) {
		this._activity = activity;
		this._imagePaths = imagePaths;
	}

	@Override
	public int getCount() {
		return this._imagePaths.size();
	}

	@Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }
	
	@Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        Button btnClose;
 
        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.fullscreen_photo_image, container, false);
 
        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.photo_image);
        btnClose = (Button) viewLayout.findViewById(R.id.btn_close_photo);
        Typeface HelveticaNeueCB = Typeface.createFromAsset(_activity.getAssets(), "HelveticaNeue-CondensedBold.ttf");
        btnClose.setTypeface(HelveticaNeueCB);
        
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.RGB_565;
        //options.inJustDecodeBounds = false;
        //options.inDither = true;
        //Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
        //int scWidth = bitmap.getWidth() / 2;
        //float aspect = (float)bitmap.getHeight() / (float)bitmap.getWidth();
        //int scHeight = Math.round(scWidth * aspect);
        //imgDisplay.setImageBitmap(Bitmap.createScaledBitmap(bitmap, scWidth, scHeight, false));
        Bitmap b = decodeFile(_imagePaths.get(position), 640, 480);
        imgDisplay.setImageBitmap(b);
        
        btnClose.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				_activity.finish();
			}
		}); 

        ((ViewPager) container).addView(viewLayout);
 
        return viewLayout;
	}
	
	@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
 
    }
	
	/*
	 * Resizing image size
	 */
	public static Bitmap decodeFile(String filePath, int WIDTH, int HEIGHT) {
		try {

			File f = new File(filePath);

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			final int REQUIRED_WIDTH = WIDTH;
			final int REQUIRED_HEIGHT = HEIGHT;
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
					&& o.outHeight / scale / 2 >= REQUIRED_HEIGHT)
				scale *= 2;

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
