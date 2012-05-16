package com.msoe.ce4960.sommere.lab5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.msoe.ce4960.sommere.lab5.FileFetcher.FileFetcherListener;

/**
 * Handles the display of the downloaded file
 * @author Erik Sommer
 *
 */
public class ViewerActivity extends Activity implements FileFetcherListener {

	/**
	 * Name of the file that was displayed
	 */
	private String mFileName;

	/**
	 * Initializes the activity
	 * @param savedInstanceState	state to restore
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the file name
		mFileName =  getIntent().getStringExtra(KEY_FILE_NAME);
		
		// Notify the user the file is being fetched
		Toast.makeText(this, "Fetching: " + mFileName, Toast.LENGTH_LONG).show();
		
		// Set the view
		setContentView(R.layout.viewer_activity);
		
		// Start fetching the file
		try {
			new FileFetcher(this, InetAddress.getByName(getIntent().getStringExtra("ipAddress"))).execute(mFileName);
		} catch (UnknownHostException e) {
			Log.e(getClass().getName(), "Could not find server", e);
		}
	}

	/**
	 * Key for the name of the file
	 */
	public static final String KEY_FILE_NAME = "fileName";
	
	/**
	 * Key for the ip address of the server
	 */
	public static final String KEY_SERVER_IP = "ipAddress";
	
	/**
	 * Handles when the file has been fetched
	 * @param fileName	the name of the file that has been fetched
	 * @param data		the data of the file
	 */
	public void onFileFetched(String fileName, byte[] data) {

		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		// Determine whether the image can be written
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// Data can be read and written
			mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// Data can only be read
			mExternalStorageWriteable = false;
		} else {
			// Data can't be read or written
			mExternalStorageWriteable = false;
		}

		File file = null;

		if(mExternalStorageWriteable){
			// Create the file
			file = new File(getExternalFilesDir(null), fileName);

			try {
				// Convert the byte array to a stream and write the file
				ByteArrayInputStream is = new ByteArrayInputStream(data);
				OutputStream os = new FileOutputStream(file);
				is.read(data);
				os.write(data);
				is.close();
				os.close();
			} catch (IOException e) {
				Log.w(getClass().getName(), "I/O error occured", e);
			}
		}

		// Find the extension of the file
		String filenameArray[] = fileName.split("\\.");
		String extension = filenameArray[filenameArray.length-1];

		if(extension.equalsIgnoreCase("txt")){
			// Use a text viewer if it is text
			TextFragment textFragment  = new TextFragment(file);
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.viewer_container, textFragment);
			fragmentTransaction.commit();
		}else if(extension.equalsIgnoreCase("jpg")){
			// Use an image view if it is a jpg
			ImageFragment imageFragment  = new ImageFragment(file);
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.viewer_container, imageFragment);
			fragmentTransaction.commit();
		}else if(extension.equalsIgnoreCase("mp3")){
			// Play back the file if it is an MP3
			
				FileDescriptor fd = null;
				
				try {
					// Get the stream
					FileInputStream fis = new FileInputStream(file);
					fd = fis.getFD();


					if (fd != null) {
						// Start playback
						MediaPlayer mediaPlayer = new MediaPlayer();
						mediaPlayer.setDataSource(fd);
						mediaPlayer.prepare();
						mediaPlayer.start();
					}
				
				} catch (FileNotFoundException e) {
					Log.e(getClass().getName(), "File not found", e);
				} catch (IOException e) {
					Log.e(getClass().getName(), "I/O exception occured", e);
				}
		}else{
			// Otherwise, try starting an intent with the file, maybe an
			// application on the device can handle it
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setData(Uri.fromFile(file));
			startActivity(intent);
			finish();
			Toast.makeText(getApplicationContext(), 
					"Unsupported file type: \"" + extension + "\"", Toast.LENGTH_SHORT);
		}
	}
}
