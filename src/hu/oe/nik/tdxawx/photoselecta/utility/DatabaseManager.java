package hu.oe.nik.tdxawx.photoselecta.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Application;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper{
	
      private SQLiteDatabase database;
      
      //if true, resets the whole database
      public final boolean CLEAN_START = false; 
      
      private static final String DATABASE_NAME = "photoselecta";
      private static final int DATABASE_VERSION = 1;

      public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.database = getWritableDatabase();
        onCreate(this.database);
      }
      
      public void CloseDB() {
    	  if (database != null)
    		  database.close();
      }
      
      public ArrayList<String> getLatestSession() {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT path FROM photos WHERE session_id = (SELECT session_id FROM photos ORDER BY session_id DESC LIMIT 1) ORDER BY id DESC", null);
    		  cur.moveToFirst();
    		  if (cur.getCount() > 0) {
    			ArrayList<String> result = new ArrayList<String>();
    		  	for (int i=0; i < cur.getCount(); i++) {
    		  		String p = cur.getString(0);
    		  		result.add(p);
    		  		cur.moveToNext();
    		  	}
    		  	cur.close();
    		  	return result;
    		  } else {
    			return new ArrayList<String>();
    		  }
    	  } else {
    		  return new ArrayList<String>();
    	  }
      }
      
      public ArrayList<String> getPhotosByTag(CharSequence tag) {
    	  if (this.database != null && tag != null && tag != "") {
    		  int tag_id = getTagIdByName(tag.toString());
    		  Log.d("PS-ADAPTER", "PS Selected tag ID: "+String.valueOf(tag_id));
    		  int[] photo_ids = getPhotoIdsByTagId(tag_id);
    		  Log.d("PS-ADAPTER", "PS Number of photos with that tag: "+String.valueOf(photo_ids.length));
    		  ArrayList<String> result = new ArrayList<String>();
    		  for (int i = 0; i < photo_ids.length; i++) {
    			  int pid = photo_ids[i];
    			  result.add(getPhotoPathById(pid));
    		  }
    		  return result;
    	  } else {
    		  return new ArrayList<String>();
    	  }
      }
      
      public ArrayList<String> getPhotosByCategory(CharSequence category) {
    	  if (this.database != null && category != null && category != "") {
    		  int category_id = getCategoryIdByName(category.toString());
    		  Log.d("PS-ADAPTER", "PS Selected category ID and name: "+String.valueOf(category_id)+" "+String.valueOf(category));
    		  int[] photo_ids = getPhotoIdsByCategoryId(category_id);
    		  Log.d("PS-ADAPTER", "PS Number of photos with that category: "+String.valueOf(photo_ids.length));
    		  ArrayList<String> result = new ArrayList<String>();
    		  for (int i = 0; i < photo_ids.length; i++) {
    			  int pid = photo_ids[i];
    			  result.add(getPhotoPathById(pid));
    		  }
    		  return result;
    	  } else {
    		  return new ArrayList<String>();
    	  }
      }
      
      public int getPhotoIdByPath(String path) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT id FROM photos WHERE LOWER(path) = '"+path.toLowerCase()+"'", null);
    		  cur.moveToFirst();
    		  int result = cur.getInt(0);
    		  cur.close();
    		  return result;
    	  } else {
    		  return 0;
    	  }
      }
      
      public String getPhotoPathById(int id) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT path FROM photos WHERE id = '"+String.valueOf(id)+"'", null);
    		  if (cur.getCount() > 0) {
    		  cur.moveToFirst();
    		  String result = cur.getString(0);
    		  cur.close();
    		  return result;
    		  } else {
    		   return "";
    		  }
    	  } else {
    		  return "";
    	  }
      }
      
      public int getTagIdByName(String tagname) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT id FROM tags WHERE LOWER(name) = '"+tagname.toLowerCase()+"'", null);
    		  cur.moveToFirst();
    		  int result = cur.getInt(0);
    		  cur.close();
    		  return result;
    	  } else {
    		  return 0;
    	  }
      }
      
      public int getCategoryIdByName(String catname) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT id FROM categories WHERE LOWER(name) = '"+catname.toLowerCase()+"'", null);
    		  if (cur.getCount() == 0) {
    			  return 0;
    		  }
    		  cur.moveToFirst();
    		  int result = cur.getInt(0);
    		  cur.close();
    		  return result;
    	  } else {
    		  return 0;
    	  }
      }
      
      public long getLatestSessionID() {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT session_id FROM photos ORDER BY session_id DESC LIMIT 1", null);
    		  cur.moveToFirst();
    		  
    		  if (cur.getCount() > 0) {
    			  int result = cur.getInt(0);
    			  cur.close();
    			  return result;
    		  } else {
    			  cur.close();
    			  return 0;
    		  }
    	  } else {
    		  return 0;
    	  }
      }
      
      public int insertPhoto(String path, long session_id) {
    	  if (this.database != null) {
    		  long timestamp = Calendar.getInstance().getTimeInMillis();
    		  ContentValues cdata = new ContentValues();
    		  cdata.put("path", path);
    		  cdata.put("session_id", session_id);
    		  cdata.put("timestamp", timestamp);
    		  //cdata.put("category_id", 0);
    		  //database.execSQL("INSERT INTO photos (path, session_id, category_id) VALUES ('"+path+"', "+String.valueOf(session_id)+", "+String.valueOf(timestamp)+");");
    		  if (database.insert("photos", null, cdata) != -1) {
    			  return getPhotoIdByPath(path);
    		  }
    		  return 0;
    	  } else
    		  return 0;
      }
      
      public boolean setPhotoCategory(int photo_id, int category_id) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("UPDATE photos SET category_id = '"+String.valueOf(category_id)+"' WHERE id = '"+String.valueOf(photo_id)+"'", null);
    		  if (cur != null) {
    			  cur.close();
    			  return true;
    		  } else {
    			  return false;
    		  }
    	  } else
    		  return false;
      }
      
      public boolean insertNewTag(String tagname) {
    	  if (this.database != null) {
    		  database.rawQuery("DELETE FROM tags WHERE name = '"+tagname+"'", null);
    		  Cursor cur = database.rawQuery("INSERT INTO tags (name) VALUES ('"+tagname+"')", null);
    		  if (cur != null) {
    			  cur.close();
    			  return true;
    		  } else {
    			  return false;
    		  }
    	  } else
    		  return false;
      }
      
      public boolean insertImportChecksum(String hash) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("INSERT INTO imports (checksum) VALUES ('"+hash+"')", null);
    		  if (cur != null) {
    			  cur.close();
    			  Log.d("", "PS DB inserted checksum "+hash);
    			  return true;
    		  } else {
    			  return false;
    		  }
    	  } else
    		  return false;
      }
      
      public boolean checksumExists(String hash) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT * FROM imports WHERE checksum = '"+hash+"'", null);
    		  if (cur != null && cur.getCount() > 0) {
    			  cur.close();
    			  return true;
    		  } else {
    			  return false;
    		  }
    	  } else
    		  return false;
      }
      
      public boolean assignTagToPhoto(int photo_id, int tag_id) {
    	  if (this.database != null) {
    		  database.execSQL("DELETE FROM tag2photo WHERE tag_id = '"+String.valueOf(tag_id)+"' AND photo_id = '"+String.valueOf(photo_id)+"';");
    		  try {
    		  database.execSQL("INSERT INTO tag2photo (photo_id, tag_id) VALUES ("+String.valueOf(photo_id)+", "+String.valueOf(tag_id)+");");
    		  } catch (SQLException e) {
    			  Log.e("PS-DB", "PS EXCEPTION "+e.getMessage());
    			  return false;
    		  }
    		  return true;
    	  }
    	  return false;
      }
      
      public boolean assignCategoryToPhoto(int photo_id, int category_id) {
    	  if (this.database != null) {
    		  database.execSQL("DELETE FROM category2photo WHERE category_id = '"+String.valueOf(category_id)+"' AND photo_id = '"+String.valueOf(photo_id)+"';");
    		  try {
    		  database.execSQL("INSERT INTO category2photo (photo_id, category_id) VALUES ("+String.valueOf(photo_id)+", "+String.valueOf(category_id)+");");
    		  } catch (SQLException e) {
    			  Log.e("PS-DB", "PS EXCEPTION "+e.getMessage());
    			  return false;
    		  }
    		  return true;
    	  }
    	  return false;
      }
      
      public boolean deletePhotoByPath(String path) {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("DELETE FROM photos WHERE LOWER(path) = '"+path.toLowerCase()+"'", null);
    		  if (cur != null) {
    			  cur.close();
    			  File file = new File(path);
    			  if (file.exists()) {
    				  file.delete();
    			  }
    			  return true;
    		  } else {
    			  return false;
    		  }
    	  } else
    		  return false;
      }
      
      public boolean deletePhotoById(int id) {
    	  String path = this.getPhotoPathById(id);
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("DELETE FROM photos WHERE id = '"+id+"'", null);
    		  if (cur != null) {
    			  cur.close();
    			  database.rawQuery("DELETE FROM tag2photo WHERE photo_id = '"+id+"'", null);
    			  File file = new File(path);
    			  if (file.exists()) {
    				  file.delete();
    			  }
    			  return true;
    		  } else {
    			  return false;
    		  }
    	  } else
    		  return false;
      }
      
      public int[] getPhotoIds() {
    	  Cursor cur = database.rawQuery("SELECT id FROM photos", null);
		  if (cur != null) {
			   if (cur.getCount() == 0) {
				   cur.close();
				   return new int[0];
			   }
		       cur.moveToFirst();
		       int[] result = new int[cur.getCount()];
		       int n = 0;
		       do {
		    	   result[n] = cur.getInt(0);
		    	   n++;
		       } while (cur.moveToNext());
		       cur.close();
		       return result;
		  }
		  return new int[0];
      }
      
      public int[] getPhotoIdsByTagId(int tag_id) {
    	  Cursor cur = database.rawQuery("SELECT photo_id FROM tag2photo WHERE tag_id = '"+ String.valueOf(tag_id) +"'", null);
		  if (cur != null) {
			   if (cur.getCount() == 0) {
				   cur.close();
				   return new int[0];
			   }
		       cur.moveToFirst();
		       int[] result = new int[cur.getCount()];
		       int n = 0;
		       do {
		    	   result[n] = cur.getInt(0);
		    	   n++;
		       } while (cur.moveToNext());
		       cur.close();
		       return result;
		  }
		  return new int[0];
      }
      
      public int[] getPhotoIdsByCategoryId(int category_id) {
    	  Log.d("PS-DB","PS Fetching photos from category id #"+String.valueOf(category_id));
    	  Cursor cur = database.rawQuery("SELECT photo_id FROM category2photo WHERE category_id = '"+ String.valueOf(category_id) +"'", null);
		  if (cur != null) {
			   if (cur.getCount() == 0) {
				   Log.d("PS-DB","PS Cursor is of zero length!");
				   cur.close();
				   return new int[0];
			   }
			   int[] result = new int[cur.getCount()];
			   Log.d("PS-DB","PS Total results: "+String.valueOf(result.length));
		       cur.moveToFirst();
		       int n = 0;
		       do {
		    	   result[n] = cur.getInt(0);
		    	   n++;
		       } while (cur.moveToNext());
		       cur.close();
		       return result;
		  } else {
			  Log.d("PS-DB","PS Cursor is null!");
			  return new int[0];
		  }
      }
      
      public int[] getTagIds() {
    	  Cursor cur = database.rawQuery("SELECT id FROM tags", null);
		  if (cur != null) {
		       cur.moveToFirst();
		       int[] result = new int[cur.getCount()];
		       int n = 0;
		       do {
		    	   result[n] = cur.getInt(0);
		    	   n++;
		       } while (cur.moveToNext());
		       cur.close();
		       return result;
		  }
		  return new int[0];
      }
      
      public int[] getCategoryIds() {
    	  Cursor cur = database.rawQuery("SELECT id FROM categories", null);
		  if (cur != null) {
		       cur.moveToFirst();
		       int[] result = new int[cur.getCount()];
		       int n = 0;
		       do {
		    	   result[n] = cur.getInt(0);
		    	   n++;
		       } while (cur.moveToNext());
		       cur.close();
		       return result;
		  }
		  return new int[0];
      }
      
      public CharSequence[] getCategories() {
    	  CharSequence[] empty = {"-"};
    	  Cursor cur = database.rawQuery("SELECT name FROM categories", null);
		  if (cur != null) {
		       cur.moveToFirst();
		       CharSequence[] result = new CharSequence[cur.getCount()];
		       int n = 0;
		       do {
		    	   result[n] = cur.getString(0);
		    	   n++;
		       } while (cur.moveToNext());
		       cur.close();
		       return result;
		  }
		  return empty;
      }
      
      public CharSequence[] getTags() {
    	  CharSequence[] empty = { };
    	  Cursor cur = database.rawQuery("SELECT name FROM tags", null);
		  if (cur != null && cur.getCount() > 0) {
		       cur.moveToFirst();
		       CharSequence[] result = new CharSequence[cur.getCount()];
		       int n = 0;
		       do {
		    	   result[n] = cur.getString(0);
		    	   n++;
		       } while (cur.moveToNext());
		       cur.close();
		       return result;
		  }
		  return empty;
      }
      
      public CharSequence[] getTagsByPhoto(int photo_id) {
    	  Cursor cur = database.rawQuery("SELECT t.name FROM tags t " +
    	  		" JOIN tag2photo t2p ON t.id = t2p.tag_id " +
    	  		" WHERE t2p.photo_id = '"+String.valueOf(photo_id)+"'", null);
		  if (cur != null) {
		       cur.moveToFirst();
		       int cnt = cur.getCount();
		       if (cnt > 0) {
			       CharSequence[] result = new CharSequence[cnt];
			       int n = 0;
			       do {
			    	   result[n] = cur.getString(0);
			    	   n++;
			       } while (cur.moveToNext());
			       cur.close();
			       return result;
		       }
		  }
		  return null;
      }

      @Override
      public void onCreate(SQLiteDatabase database) {
    	  if (CLEAN_START) {
    		  this.dropAllTables();
    	  }
    	  this.init();
      }
      
      public void init() {
    	  	// `photos`
		   database.execSQL("CREATE TABLE IF NOT EXISTS "
		     + "photos"
		     + " (" +
		     "	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
		     "	session_id INT," +
		     "	path VARCHAR," +
		     "	timestamp INT" +
		     ");");	
		   
		   // `categories`
		   database.execSQL("CREATE TABLE IF NOT EXISTS "
		     + "categories"
		     + " (" +
		     "	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
		     "	name VARCHAR NOT NULL UNIQUE," +
		     "	description VARCHAR" +
		     ");");
		   
		   // categories to photos
		   database.execSQL("CREATE TABLE IF NOT EXISTS "
		     + "category2photo"
		     + " (" +
		     "	photo_id INT NOT NULL," +
		     "	category_id INT NOT NULL" +
		     ");");
		   
		   // `tags`
		   database.execSQL("CREATE TABLE IF NOT EXISTS "
		     + "tags"
		     + " (" +
		     "	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
		     "	name VARCHAR NOT NULL UNIQUE," +
		     "	description VARCHAR" +
		     ");");
		   
		   // tags to photos
		   database.execSQL("CREATE TABLE IF NOT EXISTS "
		     + "tag2photo"
		     + " (" +
		     "	photo_id INT NOT NULL," +
		     "	tag_id INT NOT NULL" +
		     ");");
		   
		   // imported photo checksums
		   database.execSQL("CREATE TABLE IF NOT EXISTS "
		     + "imports"
		     + " (" +
		     "	checksum VARCHAR NOT NULL UNIQUE" +
		     ");");
		   
		   // default categories
		   Cursor cur2 = database.rawQuery("SELECT COUNT(*) FROM categories", null);
		   if (cur2 != null) {
		       cur2.moveToFirst();
		       if (cur2.getInt(0) == 0) {
		    	   database.execSQL("INSERT INTO categories (name, description) VALUES ('#Beach', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('#Snow', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('#People', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('#Selfie', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('#Forest', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('#Night', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('#Sunset', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('#Clouds', '');");
				   database.execSQL("INSERT INTO categories (name, description) VALUES ('Uncategorized', '');");
		       }
		       cur2.close();
		   }
		   
		   // default tags
		   Cursor cur3 = database.rawQuery("SELECT COUNT(*) FROM tags", null);
		   if (cur3 != null) {
		       cur3.moveToFirst();
		       if (cur3.getInt(0) == 0) {
		    	   database.execSQL("INSERT INTO tags (name, description) VALUES ('Test', '');");
				   database.execSQL("INSERT INTO tags (name, description) VALUES ('Photo', '');");
				   database.execSQL("INSERT INTO tags (name, description) VALUES ('Some tag', '');");
		       }
		       cur3.close();
		   }
      }
      
      public void checkFiles() {
    	   Cursor cur = database.rawQuery("SELECT * FROM photos", null);
		   if (cur.moveToFirst()) {
			   do {
				  String path = cur.getString(2);
				  File f = new File(path);
		    	  if(!f.exists()) {
		    		  int photo_id = cur.getInt(0);
		    		  database.execSQL("DELETE FROM tag2photo WHERE photo_id = '"+String.valueOf(photo_id)+"';");
		    		  database.execSQL("DELETE FROM category2photo WHERE photo_id = '"+String.valueOf(photo_id)+"';");
		    		  database.execSQL("DELETE FROM photos WHERE id = '"+String.valueOf(photo_id)+"';");
		    	  }
			   } while (cur.moveToNext());
		   }
		   if (cur != null)
			   cur.close();
      }
      
      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.w(DatabaseManager.class.getName(),"Upgrading database from version " + oldVersion + " to "+ newVersion + ", which will destroy all old data");
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        onCreate(db);
      }
      
      /*
       * MOSTLY USELESS (or senseless) METHODS
       * 
       * for debugging purposes
       */
      
      //assigns tags randomly to all existing photos
      public void assignRandomCategories() {
    	  database.delete("category2photo", null, null);
    	  int[] pids = this.getPhotoIds();
    	  int[] cids = this.getCategoryIds();
    	  if (pids.length > 0 && cids.length > 0) {
	    	  for (int i = 0; i < pids.length; i++) {
	    		  int t = (int)Math.round(Math.random()*(cids.length-1));
	    		  database.execSQL("INSERT INTO category2photo (photo_id, category_id) VALUES ('"+ String.valueOf(pids[i]) +"', '"+ String.valueOf(cids[t]) +"');");
	    	  }
    	  }
      }
      
      public int getTaggedPhotosCount() {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT COUNT(*) FROM tag2photo WHERE photo_id > 0 AND tag_id > 0", null);
    		  cur.moveToFirst();
    		  int result = cur.getInt(0);
    		  cur.close();
    		  return result;
    	  } else {
    		  return 0;
    	  }
      }
      
      public int[] getTag2Photos() {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT photo_id, tag_id FROM tag2photo", null);
    		  if (cur.getCount() > 0) {
    			  int[] result = new int[cur.getCount()*2];
    			  cur.moveToFirst();
    			  int n = 0;
    			  while (!cur.isLast()) {
    				  result[n] = cur.getInt(0);
    				  n++;
    				  result[n] = cur.getInt(1);
    				  n++;
    				  cur.moveToNext();
    			  }
    			  cur.close();
    			  return result;
    		  } else {
    			  return new int[0];
    		  }
    	  } else {
    		  return new int[0];
    	  }
      }
      
      public int[] getCategory2Photos() {
    	  if (this.database != null) {
    		  Cursor cur = database.rawQuery("SELECT photo_id, category_id FROM category2photo", null);
    		  if (cur.getCount() > 0) {
    			  Log.d("PS-DB","PS Cat2Photo has length "+String.valueOf(cur.getCount()));
    			  int[] result = new int[cur.getCount()*2];
    			  cur.moveToFirst();
    			  int n = 0;
    			  while (!cur.isLast()) {
    				  result[n] = cur.getInt(0);
    				  n++;
    				  result[n] = cur.getInt(1);
    				  n++;
    				  cur.moveToNext();
    			  }
    			  cur.close();
    			  return result;
    		  } else {
    			  Log.d("PS-DB","PS Cat2Photo contains zero elements!");
    			  return new int[0];
    		  }
    	  } else {
    		  Log.d("PS-DB","PS Cat2Photo is null!");
    		  return new int[0];
    	  }
      }
      
      //drops everything
      public void dropAllTables() {
    	  database.delete("photos", null, null);
    	  database.delete("tags", null, null);
    	  database.delete("categories", null, null);
    	  database.delete("tag2photo", null, null);
    	  database.delete("category2photo", null, null);
    	  database.delete("imports", null, null);
    	  database.execSQL("DROP TABLE IF EXISTS `photos`;");
    	  database.execSQL("DROP TABLE IF EXISTS `tags`;");
    	  database.execSQL("DROP TABLE IF EXISTS `categories`;");
    	  database.execSQL("DROP TABLE IF EXISTS `tag2photo`;");
    	  database.execSQL("DROP TABLE IF EXISTS `category2photo`;");
    	  database.execSQL("DROP TABLE IF EXISTS `imports`;");
      }
}