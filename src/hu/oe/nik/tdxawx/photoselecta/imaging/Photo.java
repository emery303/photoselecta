package hu.oe.nik.tdxawx.photoselecta.imaging;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Photo {
	
	private int filesize;
	private int _width;
	private int _height;
	private String _path;
	private Bitmap bitmap;
	
	public boolean bestInSession;
	
	public int Brightness;
	public char MainColor;
	public int Edgyness;
	
	public Photo(Bitmap b, String path) {
		this.bitmap = b;
		this._path = path;
		this.filesize = b.getByteCount();
		this._width = b.getWidth();
		this._height = b.getHeight();
		this.bestInSession = false;
	}
	
	public int getSize() { return this.filesize; }	
	public int getWidth() { return this._width; }
	public int getHeight() { return this._height; }
	public Bitmap getBitmap() { return this.bitmap; }
	public String getPath() { return this._path; }
	
	public Bitmap makeThumbnailWithMeta(int width, int height) {
		Bitmap.Config bitmapConfig = this.bitmap.getConfig();
		if(bitmapConfig == null) {
		    bitmapConfig = Bitmap.Config.ARGB_8888;
		}
		Bitmap bcopy = this.bitmap.copy(bitmapConfig, true);
		bcopy = Bitmap.createScaledBitmap(bcopy, width, height, false);
		
		Canvas canvas = new Canvas(bcopy);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Text Color
        paint.setStrokeWidth(64); // Text Size
        paint.setShadowLayer(2, 1, 1, Color.BLACK);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        // some more settings...

        //canvas.drawBitmap(this.bitmap, 0, 0, paint);
        String text = "";
        
        if (this.Brightness == 1)
        	text = "BRIGHT";
        else
        	text = "DARK";
        
        canvas.drawText(text, 10, 20, paint);
        
        return bcopy;
	}
	
}
