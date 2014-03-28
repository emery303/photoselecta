package hu.oe.nik.tdxawx.photoselecta;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreenActivity extends Activity {

	private ImageView splashpic;
	private TextView splashloader;
	
	@Override  
    public void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);  
        
        setContentView(R.layout.splash);
        
        splashpic = (ImageView)findViewById(R.id.splashpic);
        splashloader = (TextView)findViewById(R.id.splashloader);
        splashloader.setScaleX(0.0f);
  
        //Initialize a LoadViewTask object and call the execute() method  
        new LoadViewTask().execute();         
  
    }  
	
	
	private class LoadViewTask extends AsyncTask<Void, Integer, Void>  
    {    
        //The code to be executed in a background thread.  
        @Override  
        protected Void doInBackground(Void... params)  
        {    
            try  
            {    
                synchronized (this)  
                {    
                    int counter = 0;    
                    while(counter <= 100)  
                    {    
                        this.wait(50);    
                        counter++;    
                        publishProgress(counter);  
                    }  
                }  
            }  
            catch (InterruptedException e)  
            {  
                e.printStackTrace();  
            }  
            return null;  
        }  
  
        //Update the progress  
        @Override  
        protected void onProgressUpdate(Integer... values)  
        {  
            //splashpic.setAlpha((float)values[0]);
        	splashloader.setScaleX(((float)values[0])/100);
        }  
  
        //after executing the code in the thread  
        @Override  
        protected void onPostExecute(Void result)  
        {  
            //initialize the View  
            setContentView(R.layout.start);  
        }  
    }  
	
}
