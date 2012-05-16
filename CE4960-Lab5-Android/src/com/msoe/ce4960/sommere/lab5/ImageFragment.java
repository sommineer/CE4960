package com.msoe.ce4960.sommere.lab5;

import java.io.File;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Display an image that has been received
 * @author Erik Sommer
 *
 */
public class ImageFragment extends Fragment {

	/**
	 * Image file to display
	 */
	private File mFile;

	/**
	 * Constructor.
	 * @param file	the file to display
	 */
	public ImageFragment(File file){
		mFile = file;

	}

	/**
	 * Creates the view with the image
	 * @param inflater				the inflater used to inflate the view
	 * @param container				the container for the view
	 * @param savedInstanceState	the state to restore
	 * @return	the view to display
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Create the view
		View view = inflater.inflate(R.layout.image_fragment, null);

		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		// Determine whether the image can be read
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// Data can be read and written
			mExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// Data can only be read
			mExternalStorageAvailable = true;
		} else {
			// Data can't be read or written
			mExternalStorageAvailable = false;
		}

		// If getting the file is possible
		if(mExternalStorageAvailable){
			
			// Get and decode the file
			File file = mFile;
			Bitmap myBitMap = BitmapFactory.decodeFile(file.getAbsolutePath());
			
			// Display the file
			((ImageView)view.findViewById(R.id.imageview_fragment)).setImageBitmap(myBitMap);
		}

		return view;
	}


}
