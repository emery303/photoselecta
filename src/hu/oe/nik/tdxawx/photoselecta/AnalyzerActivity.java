package hu.oe.nik.tdxawx.photoselecta;

import hu.oe.nik.tdxawx.photoselecta.adapters.AnalyzerAdapter;
import hu.oe.nik.tdxawx.photoselecta.imaging.ImageAnalyzer;
import hu.oe.nik.tdxawx.photoselecta.imaging.Photo;
import hu.oe.nik.tdxawx.photoselecta.utility.DatabaseManager;
import hu.oe.nik.tdxawx.photoselecta.utility.DraggableGridView;
import hu.oe.nik.tdxawx.photoselecta.utility.PhotoMenu;

import java.lang.reflect.Array;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
 
public class AnalyzerActivity extends Activity {
 
	private Handler aH = new Handler();
	ProgressBar pb;
	TextView pbtext;
	final ImageAnalyzer ia = new ImageAnalyzer();
	
	private boolean swipeDeleteEnabled = false;
	private boolean onlyKeepBest = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyzer);
        setTitle("Review photos");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sp.getBoolean("swipedelete", false) == true)
        	this.swipeDeleteEnabled = true;
        if (sp.getBoolean("onlykeepbest", false) == true)
        	this.onlyKeepBest = true;
        
        pb = (ProgressBar)findViewById(R.id.analyzerprogress);
        pbtext = (TextView)findViewById(R.id.analyzertext);
        Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
        pbtext.setTypeface(HelveticaNeueCB);
        //Intent i = getIntent();

        new Thread(new Runnable(){
        	public void run() {
        	
        		final DatabaseManager db = new DatabaseManager(AnalyzerActivity.this);
                final ArrayList<String> files = db.getLatestSession();
                final ArrayList<Photo> photos = new ArrayList<Photo>();
                
                Bitmap b;
                Photo photo;
                
                for (int i = 0; i < files.size(); i++) {
                	String path = files.get(i);
                	b = decodeSampledBitmapFromFile(path, 400, 300);
                	//int size = b.getRowBytes()*b.getHeight();
                	ia.setBitmap(b);
                	photo = new Photo(b, path);
                	//photo.Brightness = ia.isBrightOrDark();
                	//photo.MainColor = ia.getMainColor(20);
                	//photo.Edgyness = ia.detectEdges(edgeDetector, 2.5f, 7.5f);
                	photos.add(photo);
                	
                	int best_one = 0;
                	if (files.size() > 0) {
		           		for (int j = 0; j < photos.size(); j++) {
		           			if (compareSharpness(photos.get(best_one), photos.get(j)) == photos.get(j))
		           			best_one = j;
		           		}
                	}
	           		
	           		final int best = best_one;
	           		final int progress = Math.round( ( (float)i / (float)files.size() ) * 200);
                	
                	aH.post(new Runnable(){
        				public void run(){
        					pb.setProgress(progress);
        					
        					if (progress >= 100 || files.size() == 1) {
        						photos.get(best).bestInSession = true;
        						
        						if (onlyKeepBest && files.size() > 1) {
        							for (int i = 0; i < photos.size(); i++) {
        								if (i != best) {
	        								Photo p = photos.get(i);
	        								db.deletePhotoByPath(p.getPath());
        								}
        							}
        							Photo bp = photos.get(best);
        							photos.clear();
        							photos.add(bp);
        						}
        						
        						GridView g = (GridView)findViewById(R.id.analyzergrid);
        						AnalyzerAdapter adapter = new AnalyzerAdapter(AnalyzerActivity.this, photos);
        						if (!swipeDeleteEnabled) {
        							adapter.disableSwipeDelete();
        						}
        		                g.setAdapter(adapter);
        		                registerForContextMenu(g);
        		                pb.setVisibility(8);
        		                pbtext.setVisibility(8);
        		                
        		                if (swipeDeleteEnabled) {
	        		                final AlertDialog info = new AlertDialog.Builder(AnalyzerActivity.this).create();
	        		            	info.setTitle("Review photos");
	        		            	info.setIcon(android.R.drawable.ic_dialog_info);
	        		            	info.setMessage("Swipe the photos you want to delete!");
	        		            	info.setButton("OK", new DialogInterface.OnClickListener() {        		 
	        		                    public void onClick(DialogInterface dialog, int which) {
	        		                    	info.dismiss();
	        		                    }
	        		                });
	        		            	info.show();
        		                }
        					}
        				}
        			});
                }

        	}
        }).start();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
      if (v.getId()==R.id.analyzergrid) {
        menu.setHeaderTitle("Photo actions");
        String[] menuItems = {"View", "Assign tag", "Discard"};
        for (int i = 0; i<menuItems.length; i++) {
          menu.add(Menu.NONE, i, i, menuItems[i]);
        }
      }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	final DatabaseManager db = new DatabaseManager(getApplicationContext());
    	final int photo_id = info.targetView.getId();
    	final String _path = db.getPhotoPathById(photo_id);
    	if (item.getItemId() == 0) {
    		Intent viewphoto = new Intent(AnalyzerActivity.this, FullscreenPhotoActivity.class);
    		viewphoto.putExtra("path", _path);
    		startActivityForResult(viewphoto, 1);
    	}
    	if (item.getItemId() == 1) {
    		
    		//--- tag list ---
            final CharSequence[] alltags = db.getTags();
            final CharSequence[] currentTags = db.getTagsByPhotoId(photo_id);

            final Dialog dialog = new Dialog(AnalyzerActivity.this);
            dialog.setTitle("Assign tags to photo");
            dialog.setContentView(R.layout.assign_tag);
            Typeface HelveticaNeueCB = Typeface.createFromAsset(getAssets(), "HelveticaNeue-CondensedBold.ttf");
            ((TextView)(dialog.findViewById(R.id.addnewtag_text))).setTypeface(HelveticaNeueCB);
            String tags_of_photo = "";
            for (int i = 0; i < currentTags.length; i++) {
            	tags_of_photo += currentTags[i]+", ";
            }
            ArrayAdapter<CharSequence> tagsadapter = new ArrayAdapter<CharSequence>(AnalyzerActivity.this, android.R.layout.simple_dropdown_item_1line, alltags);
            final MultiAutoCompleteTextView tags = (MultiAutoCompleteTextView)dialog.findViewById(R.id.selectedtags);
            tags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            tags.setAdapter(tagsadapter);
            tags.setText(tags_of_photo);
            tags.setTypeface(HelveticaNeueCB);
            Button btn_cancel = (Button)dialog.findViewById(R.id.tag_cancel);
            Button btn_assign = (Button)dialog.findViewById(R.id.tag_ok);
            btn_cancel.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					dialog.cancel();
				}
			});
            btn_assign.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					String t = tags.getText().toString().trim();
					if (t.equals("")) {
						Toast.makeText(AnalyzerActivity.this, "Please give a tag to assign!", Toast.LENGTH_LONG).show();
					} else {
						String[] selectedTags = t.split(",");
						int assigned = 0;
						for (int i = 0; i < selectedTags.length; i++) {
							String tag = selectedTags[i].trim();
							if (!tag.equals("")) {
								int tag_id = db.insertNewTag(tag);
								db.assignTagToPhoto(photo_id, tag_id);
								assigned++;
							}
						}
						Toast.makeText(AnalyzerActivity.this, "Successfully assigned "+assigned+" tag(s).", Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				}
			});
            dialog.show();
    		//--- tag list end ---

    	}
    	if (item.getItemId() == 2) {
    		db.deletePhotoByPath(_path);
    		info.targetView.startAnimation( AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout));
    		info.targetView.setVisibility(View.INVISIBLE);
    		Toast.makeText(AnalyzerActivity.this, "Photo discarded.", Toast.LENGTH_SHORT).show();
    	}
    	return super.onContextItemSelected(item);
    }
    
    @Override
	public void onBackPressed() {
		Intent i = new Intent(AnalyzerActivity.this, MainActivity.class);
		i.putExtra("RETURNED_FROM_ANALYZER", 1);
		setResult(1, i);
		finish();
	}
    
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    if (height > reqHeight || width > reqWidth) {
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    return inSampleSize;
    }
    
    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
    
    public Photo compareSharpness(Photo a, Photo b) {
		float blur_a = 0;
		float blur_b = 0;
		
	    for (int x = 0; x < b.getWidth(); x++) {
	    	for (int y = 0; y < b.getHeight(); y++) {
	    		blur_a += a.getBitmap().getPixel(x, y) & 0xFF;
	    		blur_b += b.getBitmap().getPixel(x, y) & 0xFF;
	        }
	    }
	    
	    float min;
	    if (blur_a < blur_b)
	    	min = blur_a;
	    else
	    	min = blur_b;
	    
	    if ((blur_a / min) > (blur_b / min))
	    	return a; 
    	else
    		return b;
	 }

}