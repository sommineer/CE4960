package com.msoe.ce4960.sommere.lab5;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
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

		String filenameArray[] = fileName.split("\\.");
		String extension = filenameArray[filenameArray.length-1];

		if(extension.equalsIgnoreCase("txt")){
			TextFragment textFragment  = new TextFragment(new String(data));
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.viewer_container, textFragment);
			fragmentTransaction.commit();
		}else{
			Toast.makeText(getApplicationContext(), "Unsupported file type: \"" + extension + "\"", Toast.LENGTH_SHORT);
		}

	}
}
