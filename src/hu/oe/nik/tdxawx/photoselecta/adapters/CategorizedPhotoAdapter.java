package hu.oe.nik.tdxawx.photoselecta.adapters;

import hu.oe.nik.tdxawx.photoselecta.FullscreenPhotoActivity;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class CategorizedPhotoAdapter extends BaseAdapter {

	private Activity _activity;
	private ArrayList<String> _filePaths = new ArrayList<String>();
	private int imageWidth;

	public CategorizedPhotoAdapter(Activity activity, int imageWidth, CharSequence catname) {
		DatabaseManager db = new DatabaseManager(activity.getApplicationContext());

		this._activity = activity;
		this._filePaths = db.getPhotosByCategory(catname);
		this.imageWidth = imageWidth;
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

		Bitmap image = decodeFile(_filePaths.get(position), imageWidth,	imageWidth);

		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,	imageWidth));
		imageView.setImageBitmap(image);

		imageView.setOnClickListener(new OnImageClickListener(_filePaths.get(position)));

		return imageView;
	}

	class OnImageClickListener implements OnClickListener {
		String _path;
		public OnImageClickListener(String path) {
			this._path = path;
		}
		@Override
		public void onClick(View v) {
			Intent i = new Intent(_activity, FullscreenPhotoActivity.class);
			i.putExtra("path", _path);
			_activity.startActivity(i);
		}
	}

	/*
	 * Resizing image size
	 */
	public static Bitmap decodeFile(String filePath, int WIDTH, int HIGHT) {
		try {

			File f = new File(filePath);

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			final int REQUIRED_WIDTH = WIDTH;
			final int REQUIRED_HIGHT = HIGHT;
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
					&& o.outHeight / scale / 2 >= REQUIRED_HIGHT)
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
