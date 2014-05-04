package hu.oe.nik.tdxawx.photoselecta.adapters;

import hu.oe.nik.tdxawx.photoselecta.FullscreenPhotoActivity;
import hu.oe.nik.tdxawx.photoselecta.R;
import hu.oe.nik.tdxawx.photoselecta.ViewPhotosByCategoryActivity;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;
import hu.oe.nik.tdxawx.photoselecta.utility.PhotoMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class CategorizedPhotoAdapter extends BaseAdapter {

	private Activity _activity;
	private ArrayList<String> _filePaths = new ArrayList<String>();
	private int imageWidth;
	private int imageHeight;
	private DatabaseManager db;

	public CategorizedPhotoAdapter(Activity activity, int imageWidth, int imageHeight, CharSequence catname) {
		db = new DatabaseManager(activity.getApplicationContext());

		this._activity = activity;
		this._filePaths = db.getPhotosByCategory(catname);
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	@Override
	public int getCount() {
		return this._filePaths.size();
	}

	@Override
	public Object getItem(int position) {
		return this._filePaths.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(_activity);
		} else {
			imageView = (ImageView) convertView;
		}
		File f = new File(_filePaths.get(position));
		if (f.exists()) {
			Bitmap image = decodeFile(_filePaths.get(position), imageWidth,	imageHeight);
	
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,	imageHeight));
			
			Bitmap thumbnail;
			if (image.getWidth() >= image.getHeight()){
				thumbnail = Bitmap.createBitmap(
					image, 
					image.getWidth()/2 - image.getHeight()/2,
				    0,
				    image.getHeight(), 
				    image.getHeight()
				    );
			} else {
					thumbnail = Bitmap.createBitmap(
					image,
				    0, 
				    image.getHeight()/2 - image.getWidth()/2,
				    image.getWidth(),
				    image.getWidth() 
				    );
			}
			
			imageView.setImageBitmap(Bitmap.createScaledBitmap(thumbnail, imageWidth, imageHeight, true));
			imageView.setOnClickListener(new OnImageClickListener(_filePaths.get(position)));
			imageView.setOnLongClickListener(new OnImageLongClickListener(_filePaths.get(position)));
		
		} else {
			db.deletePhotoByPath(_filePaths.get(position));
		}
		return imageView;
	}

	class OnImageClickListener implements OnClickListener {
		String _path;
		public OnImageClickListener(String path) {
			this._path = path;
		}
		@Override
		public void onClick(View v) {
			v.startAnimation( AnimationUtils.loadAnimation(_activity, R.anim.bounce) );
			v.postDelayed(new Runnable() {
			    @Override
			    public void run() {
			    	Intent i = new Intent(_activity, FullscreenPhotoActivity.class);
					i.putExtra("path", _path);
					_activity.startActivity(i);
			    }
			}, 250);
		}
	}
	
	class OnImageLongClickListener implements OnLongClickListener {
		String _path;
		public OnImageLongClickListener(String path) {
			this._path = path;
		}
		@Override
		public boolean onLongClick(View v) {
			v.startAnimation( AnimationUtils.loadAnimation(_activity, R.anim.bounce) );
			v.postDelayed(new Runnable() {
				@Override
				public void run() {
					new PhotoMenu(_activity, _path).openPhotoMenu();
				}
			}, 200);			
			return true;
		}
	}

	/*
	 * Resizing image size
	 */
	public static Bitmap decodeFile(String filePath, int WIDTH, int HEIGHT) {
		try {

			File f = new File(filePath);

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			o.inDither = true;
			o.inPreferredConfig = Bitmap.Config.RGB_565;
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
