package com.msoe.ce4960.sommere.lab5;

import java.net.InetAddress;

import com.msoe.ce4960.sommere.lab5.FileFetcher.FileFetcherListener;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileIndexFragment extends ListFragment implements FileFetcherListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onFileFetched(String fileName, byte[] data) {
		
		String dataString = new String(data);
		
		String[] files = dataString.split("\\r?\\n");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, files);
		setListAdapter(adapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		return inflater.inflate(R.layout.file_index_fragment, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		new FileFetcher(this, serverAddress).execute(".");
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), ViewerActivity.class);
		intent.putExtra("fileName", (String)getListAdapter().getItem(position));
		intent.putExtra("ipAddress", serverAddress.getHostAddress());
		startActivity(intent);
	}
	
	public void refresh(){
		new FileFetcher(this, serverAddress).execute(".");
	}

	public void setIP(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	private InetAddress serverAddress;
}
