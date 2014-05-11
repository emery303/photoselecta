package hu.oe.nik.tdxawx.photoselecta.imaging;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class ImageAnalyzer {

	private Bitmap image;
	private Bitmap scaledImage;
	private int _width;
	private int _height;
	private float aspect;
	
	public ImageAnalyzer() {
		this.image = null;
		this._width = 0;
		this._height = 0;
		this.aspect = 0;
	}
	
	public void setBitmap(Bitmap b) {
		this.image = b;
		this._width = b.getWidth();
		this._height = b.getHeight();
		this.aspect = (float)b.getHeight() / (float)b.getWidth();
		
		int scaledWidth = 200;
		int scaledHeight = Math.round(scaledWidth * this.aspect);
		this.scaledImage = Bitmap.createScaledBitmap(this.image, scaledWidth, scaledHeight, false);
	}
	
	public int detectEdges(CannyEdgeDetector edgeDetector, float lowThreshold, float highThreshold){
		edgeDetector.setLowThreshold(lowThreshold);
		edgeDetector.setHighThreshold(highThreshold);
		
		edgeDetector.setSourceImage(this.scaledImage);
		edgeDetector.process();
		
		return 0;
	}
	
	public byte[] makeResizedImage(int width, int height) {
	    Bitmap resized = Bitmap.createScaledBitmap(this.image, width, height, false);
	    ByteArrayOutputStream blob = new ByteArrayOutputStream();
	    resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
	    return blob.toByteArray();
	}
	
	public char getMainColor(int minimum_difference) {
		int md = minimum_difference;
		byte[] rdata = this.makeResizedImage(64,64);
		Bitmap thumbnail = BitmapFactory.decodeByteArray(rdata, 0, rdata.length);
		
		double red_dominant_pixels = 0;
		double blue_dominant_pixels = 0;
		double green_dominant_pixels = 0;
		
		for (int x=0; x<thumbnail.getWidth(); x++) {
			for (int y=0; y<thumbnail.getHeight(); y++) {
				int color = thumbnail.getPixel(x,y);
				int red = Color.red(color); //red
				int green = Color.green(color); //green
				int blue = Color.blue(color); //blue

				if (red > blue+md && red > green+md) red_dominant_pixels++;
				if (blue > red+md && blue > green+md) blue_dominant_pixels++;
				if (green > red+md && green > blue+md) green_dominant_pixels++;
			}
		}
			
		if (red_dominant_pixels > blue_dominant_pixels && red_dominant_pixels > green_dominant_pixels)
			return 'R';
		
		if (green_dominant_pixels > red_dominant_pixels && green_dominant_pixels > blue_dominant_pixels)
			return 'G';
		
		if (blue_dominant_pixels > red_dominant_pixels && blue_dominant_pixels > green_dominant_pixels)
			return 'B';
		
		return '0';
	}
	
	public int isBrightOrDark() { // 1 = bright, 0 = dark
		byte[] rdata = makeResizedImage(64,64);
		Bitmap thumbnail = BitmapFactory.decodeByteArray(rdata, 0, rdata.length);
		
		double global_luminance = 0;
		
		for (int x=0; x<thumbnail.getWidth(); x++) {
			for (int y=0; y<thumbnail.getHeight(); y++) {
				int color = thumbnail.getPixel(x,y);
				int red = Color.red(color); //red
				int green = Color.green(color); //green
				int blue = Color.blue(color); //blue
				double lum = (red+green+blue)/3; //luminance
				global_luminance += lum;
			}
		}
		global_luminance /= thumbnail.getWidth()*thumbnail.getHeight();
		
		if (global_luminance > 127)
			return 1;
		else
			return 0;
	}
}
