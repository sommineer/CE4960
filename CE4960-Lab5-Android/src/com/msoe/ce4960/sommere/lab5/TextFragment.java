package com.msoe.ce4960.sommere.lab5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Handles the display of text data
 * @author Erik Sommer
 *
 */
public class TextFragment extends Fragment {

	/**
	 * File to read the text from
	 */
	private File mFile;

	/**
	 * Constructor.
	 * @param file	the file to display
	 */
	public TextFragment(File file){
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
		View view = inflater.inflate(R.layout.text_fragment, null);

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
			
			File file = mFile;
			BufferedReader reader;
			
			try {
				// Create a reader for the file
				reader = new BufferedReader(new FileReader(file));

				StringBuilder builder = new StringBuilder();
				char buffer[] = new char[1000];

				// Read in the file
				while(reader.read(buffer) != -1){
					builder.append(buffer);
				}

				// Set the text
				((TextView)view.findViewById(R.id.text_fragment))
				.setText(builder.toString());
				
			} catch (FileNotFoundException e) {
				Log.e(getClass().getName(), "File could not be found", e);
			} catch (IOException e) {
				Log.e(getClass().getName(), "I/O error reading the file", e);
			}
		}

		return view;
	}


}
