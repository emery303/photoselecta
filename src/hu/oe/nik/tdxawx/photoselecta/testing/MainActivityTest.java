package hu.oe.nik.tdxawx.photoselecta.testing;

import hu.oe.nik.tdxawx.photoselecta.MainActivity;
import hu.oe.nik.tdxawx.photoselecta.R;
import hu.oe.nik.tdxawx.photoselecta.ViewPhotosActivity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;
import android.widget.ImageButton;
import junit.framework.TestCase;

public class MainActivityTest extends ActivityUnitTestCase<MainActivity> {

	private Intent intent;
	
	public MainActivityTest() { 
		super(MainActivity.class);
	}

	@Override
    protected void setUp() throws Exception {
        super.setUp();
        intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
    }
	
	@MediumTest
	public void testGalleryOpened() {
		startActivity(intent, new Bundle(), null);
		ImageButton btn_gallery = ((ImageButton)(getActivity().findViewById(R.id.start_viewphotos)));
		assertNotNull("Gallery button is null", btn_gallery);
		btn_gallery.performClick();

        Intent startedIntent = getStartedActivityIntent();
        assertNotNull("Intent was null", startedIntent);
        assertTrue(isFinishCalled());
	}

}
