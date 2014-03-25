package hu.oe.nik.tdxawx.photoselecta.utility;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

//import com.googlecode.tesseract.android.*;
//import com.googlecode.leptonica.android.*;

public class OCR {

	private static final String TESSERACT_DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/photoselecta/tesseract/");
	private String _path;
	
	public OCR(String path) {
		this._path = path;
	}
	
	public boolean containsText() { // does the image contain ANY recognizable text?
		if (this._path == null)
			return false;
		
		return true;
	}
	
	public String getRecognizedText() {
		int rotate = 0;
		
		try {
			ExifInterface exif = new ExifInterface(this._path);
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
			    rotate = 90;
			    break;
			case ExifInterface.ORIENTATION_ROTATE_180:
			    rotate = 180;
			    break;
			case ExifInterface.ORIENTATION_ROTATE_270:
			    rotate = 270;
			    break;
			}
		} catch (IOException ex) {
			Log.d("EXCEPTION: ",ex.getMessage());
		}
		
		return "";
		/*
		Bitmap bitmap = BitmapFactory.decodeFile(this._path);
		
		if (rotate != 0) {
		    int w = bitmap.getWidth();
		    int h = bitmap.getHeight();
		    Matrix mtx = new Matrix();
		    mtx.preRotate(rotate);
		    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
		}
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.init(TESSERACT_DATA_PATH, "hun");
		String result = baseApi.getUTF8Text();
		baseApi.end();
		
		return result;
		*/
	}
}
