package hu.oe.nik.tdxawx.photoselecta.adapters;

import java.io.File;
import java.util.ArrayList;

import com.squareup.picasso.Picasso;

import hu.oe.nik.tdxawx.photoselecta.R;
import hu.oe.nik.tdxawx.photoselecta.utility.PhotoMenu;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter {
    
	private Activity _activity;
	private ArrayList<String> _items;
	
	private int thumbnailWidth;
	private int thumbnailHeight;
	
	public GalleryAdapter(Activity activity, ArrayList<String> items, int thWidth, int thHeight) {
        this._activity = activity;
        this._items = items;
        this.thumbnailWidth = thWidth;
        this.thumbnailHeight = thHeight;
    }
	
	@Override
	public int getCount() {
		return this._items.size();
	}
	
	@Override
	public Object getItem(int position) {
		return this._items.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ImageView imageView;
    	
    	if (convertView == null) {
            convertView = _activity.getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
        }
    	
    	imageView = (ImageView)convertView.findViewById(R.id.placeholder);

        final String _path = _items.get(position);

        imageView.setImageResource(R.drawable.photo_placeholder);
        Picasso.with(_activity)
            .load(new File(_path))
            .placeholder(R.drawable.photo_placeholder)
            .centerCrop()
            .resize(thumbnailWidth, thumbnailHeight)
            .into(imageView);
        
        imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(_activity, R.anim.bounce));
				v.postDelayed(new Runnable() {
					@Override
					public void run() {
						new PhotoMenu(_activity, _path).openPhotoMenu();
					}
				}, 200);
			}
		});

        return convertView;
    }
}