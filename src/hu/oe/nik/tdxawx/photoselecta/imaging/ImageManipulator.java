package hu.oe.nik.tdxawx.photoselecta.imaging;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.R.string;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

public class ImageManipulator {
	
	private byte[] imageData;
	private int[] pixelData;
	private int width;
	private int height;
	private int quality;
	
	private int _tw = 0;
	private int _th = 0;
	private int _tc = 0;
	private ArrayList<String> _ts = new ArrayList<String>();	
	
	public ImageManipulator(int width, int height, byte[] imageData, int quality) {
		this.width = width;
		this.height = height;
		this.quality = quality;
		this.imageData = imageData;
		this.pixelData = pixelsFromBytes(imageData);
	}
	
	private int[] pixelsFromBytes(byte[] imageData) {
		ByteArrayOutputStream previewStream = new ByteArrayOutputStream();
	    Rect rect = new Rect(0, 0, width, height); 
	    YuvImage yuvimage = new YuvImage(imageData, ImageFormat.NV21, width, height, null);
	    yuvimage.compressToJpeg(rect, this.quality, previewStream);
	    Bitmap b = BitmapFactory.decodeByteArray(previewStream.toByteArray(), 0, previewStream.size());
	    int[] pixels = new int[this.width * this.height];
	    b.getPixels(pixels, 0, this.width, 0, 0, this.width, this.height);
	    return pixels;
	}
	
	public int[] getPixelData() {
		if (this.pixelData != null)
			return this.pixelData;
		else
			return new int[this.width * this.height];
	}
	
	public int[] getAreaSizeAt(int x, int y, int tolerance) {
		_th = 0;
		_tw = 0;
		_ts.clear();
		_tc = this.pixelData[(y)*this.width+(x)];
		traceNeighbors(x, y, tolerance);
		int[] result = {_tw, _th};
		return result;
	}
	
	private void traceNeighbors(int x, int y, int tolerance) {
		if (x > 1 && x < this.width-1 && y > 1 && y < this.height-1 && !_ts.contains(String.valueOf(x)+";"+String.valueOf(y))) {
			int c0 = this._tc;
			int c1 = this.pixelData[(y+1)*this.width+x];
			int c2 = this.pixelData[(y-1)*this.width+x];
			int c3 = this.pixelData[(y)*this.width+(x+1)];
			int c4 = this.pixelData[(y)*this.width+(x-1)];
			_ts.add(String.valueOf(x)+";"+String.valueOf(y));
			if (Math.abs(Color.red(c0) - Color.red(c1)) <= tolerance
		     && Math.abs(Color.green(c0) - Color.green(c1)) <= tolerance
		     && Math.abs(Color.blue(c0) - Color.blue(c1)) <= tolerance) {
				_th++;
				traceNeighbors(x, y+1, tolerance);
			}
			if (Math.abs(Color.red(c0) - Color.red(c2)) <= tolerance
		     && Math.abs(Color.green(c0) - Color.green(c2)) <= tolerance
		     && Math.abs(Color.blue(c0) - Color.blue(c2)) <= tolerance) {
				_th++;
				traceNeighbors(x, y-1, tolerance);
			}
			if (Math.abs(Color.red(c0) - Color.red(c3)) <= tolerance
		     && Math.abs(Color.green(c0) - Color.green(c3)) <= tolerance
		     && Math.abs(Color.blue(c0) - Color.blue(c3)) <= tolerance) {
				_tw++;
				traceNeighbors(x+1, y, tolerance);
			}
			if (Math.abs(Color.red(c0) - Color.red(c4)) <= tolerance
		     && Math.abs(Color.green(c0) - Color.green(c4)) <= tolerance
		     && Math.abs(Color.blue(c0) - Color.blue(c4)) <= tolerance) {
				_tw++;
				traceNeighbors(x-1, y, tolerance);
			}
			
			return;
			
		} else {
			return;
		}
	}
	
}
