package hu.oe.nik.tdxawx.photoselecta.utility;

import hu.oe.nik.tdxawx.photoselecta.FacebookPhotoActivity;
import hu.oe.nik.tdxawx.photoselecta.FullscreenPhotoActivity;
import hu.oe.nik.tdxawx.photoselecta.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoMenu extends Activity {

	private Activity _activity;
	private String _path;
	private int _id;
	String newcategoryname = "";
	Typeface HelveticaNeueCB;
	
	private DatabaseManager db;
	private FacebookManager fbm;
	private BluetoothManager bm;
	
	public PhotoMenu(Activity activity, String path) {
		this._activity = activity;
		this._path = path;
		this.db = new DatabaseManager(_activity.getApplicationContext());
		this._id = db.getPhotoIdByPath(_path);
		this.fbm = new FacebookManager(_activity);
		this.bm = new BluetoothManager(_activity);
		this.HelveticaNeueCB = Typeface.createFromAsset(_activity.getAssets(), "HelveticaNeue-CondensedBold.ttf");
	}
	
	public void openPhotoMenu() {
		new AlertDialog.Builder(_activity) 
		.setTitle("Photo menu")
		.setItems(new CharSequence[] {	"View photo",
										"Assign new category", 
										"Edit tags",
										"Post to Facebook",
										"Send via Bluetooth",
										"Delete"}, 
										new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which) {
		        case 0:
		        	Intent i = new Intent(_activity, FullscreenPhotoActivity.class);
		        	i.putExtra("path", _path);
		        	_activity.startActivityForResult(i, 1);
		        	break;
		        case 1: //new category
		        	final int photo_id = db.getPhotoIdByPath(_path);
		        	final String[] cats = db.getCategories();
		        	String currentcatname = db.getCategoryByPhotoId(photo_id).toString();
		        	int selected = 0;
		        	for (int n = 0; n < cats.length; n++) {
		        		if (cats[n].equals(currentcatname)) {
		        			selected = n;
		        			break;
		        		}
		        	}
		        	
		        	new AlertDialog.Builder(_activity)
			        .setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle("Select a category")
			        .setSingleChoiceItems(cats, selected, new OnClickListener() {
			        	@Override
						public void onClick(DialogInterface dialog, int which) {
							newcategoryname = cats[which];
						}
					})
			        .setPositiveButton("Assign", new DialogInterface.OnClickListener()
			        {
			        	@Override
				        public void onClick(DialogInterface dialog, int which) {
			        		if (!newcategoryname.equals("")) {
				        		int category_id = db.getCategoryIdByName(newcategoryname);
				        		db.assignCategoryToPhoto(photo_id, category_id);
				        		dialog.dismiss();
				        		Toast.makeText(_activity.getApplicationContext(), "New category assigned successfully.", Toast.LENGTH_SHORT).show();
			        		}
				        }
			    	})
				    .setNegativeButton("Cancel", null)
				    .show();
		        	break;
		        case 2: //tags
		        	//--- tag list ---
		            final CharSequence[] alltags = db.getTags();
		            final CharSequence[] currentTags = db.getTagsByPhotoId(_id);

		            final Dialog tagdialog = new Dialog(_activity);
		            tagdialog.setTitle("Assign tags to photo");
		            tagdialog.setContentView(R.layout.assign_tag);
		            ((TextView)(tagdialog.findViewById(R.id.addnewtag_text))).setTypeface(HelveticaNeueCB);
		            String tags_of_photo = "";
		            for (int n = 0; n < currentTags.length; n++) {
		            	tags_of_photo += currentTags[n]+", ";
		            }
		            ArrayAdapter<CharSequence> tagsadapter = new ArrayAdapter<CharSequence>(_activity, android.R.layout.simple_dropdown_item_1line, alltags);
		            final MultiAutoCompleteTextView tags = (MultiAutoCompleteTextView)tagdialog.findViewById(R.id.selectedtags);
		            tags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		            tags.setAdapter(tagsadapter);
		            tags.setText(tags_of_photo);
		            tags.setTypeface(HelveticaNeueCB);
		            tags.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (hasFocus) {
					            tagdialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					        }
						}
					});
		            Button btn_cancel = (Button)tagdialog.findViewById(R.id.tag_cancel);
		            Button btn_assign = (Button)tagdialog.findViewById(R.id.tag_ok);
		            btn_cancel.setOnClickListener(new View.OnClickListener() {
						public void onClick(View arg0) {
							tagdialog.cancel();
						}
					});
		            btn_assign.setOnClickListener(new View.OnClickListener() {
		            	@Override
						public void onClick(View arg0) {
							String t = tags.getText().toString().trim();
							if (t.equals("")) {
								db.removeTagsFromPhoto(_id);
								Toast.makeText(_activity, "Successfully removed all tags from photo.", Toast.LENGTH_LONG).show();
								tagdialog.dismiss();
							} else {
								String[] selectedTags = t.split(",");
								int assigned = 0;
								for (int i = 0; i < selectedTags.length; i++) {
									String tag = selectedTags[i].trim();
									if (!tag.equals("")) {
										int tag_id = db.insertNewTag(tag);
										db.assignTagToPhoto(_id, tag_id);
										assigned++;
									}
								}
								Toast.makeText(_activity, "Successfully assigned "+assigned+" tag(s).", Toast.LENGTH_LONG).show();
								tagdialog.dismiss();
							}
						}
					});
		            tagdialog.show();
		        	break;
		        	
		        case 3: //facebook
		        	final EditText commentbox = new EditText(_activity);
		        	commentbox.setWidth(400);
		        	commentbox.setHeight(200);
	        		commentbox.setTypeface(HelveticaNeueCB);
		        	commentbox.setTextSize(18);
		        	commentbox.setTextColor(Color.argb(255, 51, 51, 51));
		        	InputMethodManager imm = (InputMethodManager)_activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		        	imm.showSoftInput(commentbox, InputMethodManager.SHOW_FORCED);
		        	new AlertDialog.Builder(_activity)
			        .setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle("Post to Facebook")
			        .setMessage("Photo description:")
			        .setView(commentbox)
			        .setPositiveButton("Post", new DialogInterface.OnClickListener()
			        {
			        	@Override
				        public void onClick(DialogInterface dialog, int which) {
			        		String path = _path;
			        		String message = commentbox.getText().toString();
			        		Intent i = new Intent(_activity, FacebookPhotoActivity.class);
			        		i.putExtra("path", path);
			        		i.putExtra("message", message);
			        		_activity.startActivity(i);
				        }
			    	})
				    .setNegativeButton("Cancel", null)
				    .show();
		        	break;
		        	
		        case 4: //bluetooth
		        	bm.SendFile(_path);
		        	break;
		        	
		        case 5: //delete
		        	new AlertDialog.Builder(_activity)
			        .setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle("Delete photo")
			        .setMessage("Are you sure you want to delete this photo?")
			        .setPositiveButton("Yes, delete", new DialogInterface.OnClickListener()
			        {
			        	@Override
				        public void onClick(DialogInterface dialog, int which) {
			        		db = new DatabaseManager(_activity.getApplicationContext());
			        		db.deletePhotoByPath(_path);
			        		Toast.makeText(_activity.getApplicationContext(), "Photo deleted.", Toast.LENGTH_SHORT).show();
			        	}
		        	
			        })
				    .setNegativeButton("No", null)
				    .show();
		        	break;
		        	
	        	default:
	        		break;
		        }
		    }
		}).show();
	}
	
	// pass activityresult to facebook manager - required to maintain sessions
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		fbm.onActivityResult(requestCode, resultCode, data);
		bm.onActivityResult(requestCode, resultCode, data);
	}
}

	