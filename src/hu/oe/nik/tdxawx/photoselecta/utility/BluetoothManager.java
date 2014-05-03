package hu.oe.nik.tdxawx.photoselecta.utility;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class BluetoothManager {

	private BluetoothAdapter btAdapter;
	private Activity activity;
	private boolean supports_bluetooth = true;
	private File file_to_send;
	
	private static final int BT_DISCOVERY = 120;
	private static final int REQUEST_BLUETOOTH = 1;
	
	public BluetoothManager(Activity a) {
		this.activity = a;
		this.btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			supports_bluetooth = false;
		}
	}
	
	public void enableBluetooth(){
		Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERY );
		activity.startActivityForResult(discoveryIntent, REQUEST_BLUETOOTH);
	}
	
	public void SendFile(String path) {
		if (supports_bluetooth) {
			File f = new File(path);
			if (f.exists()) {
				file_to_send = f;
				enableBluetooth();
			}
		}
	}
	
	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (resultCode == BT_DISCOVERY && requestCode == REQUEST_BLUETOOTH) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("image/jpeg");
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file_to_send) );
			PackageManager pm = activity.getPackageManager();
			List<ResolveInfo> applist = pm.queryIntentActivities( intent, 0);
			String packageName = null;
			String className = null;
			for(ResolveInfo info: applist){
			  packageName = info.activityInfo.packageName;
			  if( packageName.equals("com.android.bluetooth")){
			     className = info.activityInfo.name;
			     break;
			  }
			}
			intent.setClassName(packageName, className);
			activity.startActivity(intent);
		}
	}
}
