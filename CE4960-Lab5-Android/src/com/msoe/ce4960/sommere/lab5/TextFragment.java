package com.msoe.ce4960.sommere.lab5;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextFragment extends Fragment {

	private String mData;
	
	public TextFragment(String data){
		mData = data;
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.text_fragment, null);
		
		((TextView)view.findViewById(R.id.text_fragment)).setText(mData);
		
		return view;
	}
	
	
}
