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

public class ImageFragment extends Fragment {

	private File mFile;

	public ImageFragment(File file){
		mFile = file;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.image_fragment, null);

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

		if(mExternalStorageAvailable){
			File file = mFile;

			Bitmap myBitMap = BitmapFactory.decodeFile(file.getAbsolutePath());
			((ImageView)view.findViewById(R.id.imageview_fragment)).setImageBitmap(myBitMap);
		}


		return view;
	}


}
