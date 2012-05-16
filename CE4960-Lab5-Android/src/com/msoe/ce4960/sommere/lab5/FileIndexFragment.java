package com.msoe.ce4960.sommere.lab5;

import java.net.InetAddress;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.msoe.ce4960.sommere.lab5.FileFetcher.FileFetcherListener;

/**
 * Fragment that creates and manages the index of files that are available from
 * the server.
 * @author Erik Sommer
 *
 */
public class FileIndexFragment extends ListFragment implements FileFetcherListener {

	/**
	 * Address of the server
	 */
	private InetAddress serverAddress;
	
	/**
	 * Handles when the activity is created.  Sets up the {@link ListView} and
	 * fetches the index
	 * @param	savedInstanceState	the state to restore
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Set so only one item can be chosen
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// Request the index
		new FileFetcher(this, serverAddress).execute(".");
	}
	
	/**
	 * Handles when the view is created
	 * @param inflater				the inflater to use to inflate the view
	 * @param container				the container for the view
	 * @param savedInstanceState	the state to restore
	 * @return	the view to display
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Inflate the view and return
		return inflater.inflate(R.layout.file_index_fragment, null);
	}

	/**
	 * Handles when the index has been fetched
	 * @param fileName	the name of the file
	 * @param data		the data that was received
	 */
	public void onFileFetched(String fileName, byte[] data) {
		
		// Create a new string with the data
		String dataString = new String(data);
		
		// Split the string based on the delimiters
		String[] files = dataString.split("\\r?\\n");
		
		// Create a new adapter for the list
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_activated_1, files);
		
		// Update the adapter
		setListAdapter(adapter);
	}
	
	/**
	 * Handles when an item has been clicked
	 * @param l			the {@link ListView} that contains the item that has been 
	 * 					clicked
	 * @param v			the view that was clicked
	 * @param position	the position of the view that was clicked
	 * @param id		the id of the view that was clicked
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		// Get a new intent for the viewer
		Intent intent = new Intent(getActivity(), ViewerActivity.class);
		
		// Add the file name and the server IP
		intent.putExtra(ViewerActivity.KEY_FILE_NAME, 
				(String)getListAdapter().getItem(position));
		intent.putExtra(ViewerActivity.KEY_SERVER_IP, 
				serverAddress.getHostAddress());
		
		// Start the viewer
		startActivity(intent);
	}

	/**
	 * Triggers a refresh of the server index
	 */
	public void refresh(){
		new FileFetcher(this, serverAddress).execute(".");
	}

	/**
	 * Sets the server address
	 * @param serverAddress	the address of the server
	 */
	public void setIP(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}
}
