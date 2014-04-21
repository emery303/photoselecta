package hu.oe.nik.tdxawx.photoselecta;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("App settings");
		addPreferencesFromResource(R.xml.settings);
		setContentView(R.layout.settingslayout);
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(SettingsActivity.this, MainActivity.class);
		i.putExtra("RETURNED_FROM_SETTINGS", 1);
		setResult(1, i);
		finish();
	}
}
