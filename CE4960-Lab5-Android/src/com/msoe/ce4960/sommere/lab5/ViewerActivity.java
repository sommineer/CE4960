package com.msoe.ce4960.sommere.lab5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.msoe.ce4960.sommere.lab5.FileFetcher.FileFetcherListener;

public class ViewerActivity extends Activity implements FileFetcherListener {

	private String mFileName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFileName =  getIntent().getStringExtra("fileName");
		setContentView(R.layout.viewer_activity);
		Toast.makeText(this, "Fetching: " + mFileName, Toast.LENGTH_LONG).show();
		new FileFetcher(this).execute(mFileName);
	}

	public void onFileFetched(String fileName, byte[] data) {

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		File file = null;
		
		if(mExternalStorageWriteable){
			// Create a path where we will place our private file on external
		    // storage.
		    file = new File(getExternalFilesDir(null), fileName);

		    try {
		        // Very simple code to copy a picture from the application's
		        // resource into the external file.  Note that this code does
		        // no error checking, and assumes the picture is small (does not
		        // try to copy it in chunks).  Note that if external storage is
		        // not currently mounted this will silently fail.
		    	ByteArrayInputStream is = new ByteArrayInputStream(data);
		        OutputStream os = new FileOutputStream(file);
		        is.read(data);
		        os.write(data);
		        is.close();
		        os.close();
		    } catch (IOException e) {
		        // Unable to create file, likely because external storage is
		        // not currently mounted.
		        Log.w("ExternalStorage", "Error writing " + file, e);
		    }
		}
		
		String filenameArray[] = fileName.split("\\.");
		String extension = filenameArray[filenameArray.length-1];

		if(extension.equalsIgnoreCase("txt")){
			TextFragment textFragment  = new TextFragment(file);
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.viewer_container, textFragment);
			fragmentTransaction.commit();
		}else if(extension.equalsIgnoreCase("jpg")){
			Toast.makeText(getApplicationContext(), "Data length: " + data.length, Toast.LENGTH_LONG).show();
			ImageFragment imageFragment  = new ImageFragment(file);
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.viewer_container, imageFragment);
			fragmentTransaction.commit();
		}else{
			Toast.makeText(getApplicationContext(), "Unsupported file type: \"" + extension + "\"", Toast.LENGTH_SHORT);
		}

	}
}
