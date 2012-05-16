package com.msoe.ce4960.sommere.lab5;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Acts as a client to the SSFTP server over TCP
 * 
 * Experiences:
 * This lab wasn't difficult at all as TCP is much easier to work with than UDP,
 * at least on the layer that Java uses it.
 * 
 * Deficiencies:
 * There are two main issues with the application.  The IP address of the server
 * is hard coded in, and not all file types are supported.  Initially, it was
 * planned to add a server selector dialog, but it would require reworking too
 * much of the code to complete.  Android natively supports only a few select
 * file types, and those are handled when possible.
 * 
 * Ideas for Improvement:
 * Overall the lab is pretty good, there isn't anything that I can think of
 * in need for improvement.
 * 
 * Usage:
 * Update the ip address so it points to the server and then launch the application.
 * The usage is fairly self explanatory from there.
 * 
 * Benchmarks:
 * To compare the performance of the UDP server to the TCP server, the following
 * benchmarks were run:
 * 
 * | File Name            | File Size (Bytes) | UDP Avg. (us) | TCP Avg. (us) |
 * |----------------------|-------------------|---------------|---------------|
 * | test.txt             |                67 |         201.2 |         419.3 |
 * | Desert.jpg			  |            845941 |   2,543,874.7 |     420,785.1 |
 * | 01 Couch Potato.mp3  |           4364416 |  13,116,291.1 |   2,316,369.2 |
 * |----------------------|-------------------|---------------|---------------|
 * 
 * Each file was transfered 10 times on each protocol and the average was taken.
 * These numbers show that for transfers that only take a packet or two, UDP is
 * able to transfer the data in half the amount of time.  But for larger
 * transfers, such as images and music files, TCP transfers at a much faster
 * speed.  TCP is about 6 times faster than UDP for these transfers.  One
 * way to determine whether TCP or UDP should be used is to find out if the
 * overhead from TCP will be the majority of your packets.  If it is, then UDP
 * may be the better option.  But with TCP you get a whole host of reliability
 * and other features, which make it ideal for the larger transfers.
 * 
 * 
 * @author Erik Sommer
 */

/**
 * Main activity of the application.  Handles the initialization
 * @author Erik Sommer
 *
 */
public class MainActivity extends Activity{


	/**
	 * Manages the list of files
	 */
	private FileIndexFragment mFileIndexFragment;

	/**
	 * Address of the server
	 */
	private InetAddress mServerAddress;

	/**
	 * Handles when a fragment attaches to the activity
	 * @param fragment	the fragment that was attached
	 */
	@Override
	public void onAttachFragment(Fragment fragment) {
		
		super.onAttachFragment(fragment);
		
		// Check if the fragment is an instance of one it is looking for
		if(fragment instanceof FileIndexFragment){
			
			// If so, Store a reference
			mFileIndexFragment = (FileIndexFragment) fragment;

			// Set the IP for the fragment to use
			mFileIndexFragment.setIP(mServerAddress);

		}
	}

	/**
	 * Executed when the activity is started
	 * @param savedInstanceState	state to restore, if any
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// Get the server address
		try {
			mServerAddress = InetAddress.getByName("192.168.1.8");
		} catch (UnknownHostException e) {
			// Log an error if the server can't be found and stop the activity
			Log.e(getClass().getName(), "Unable to find server.", e);
			finish();
		}
		
		// Set the XML view for the program
		setContentView(R.layout.main);
	}
	
	/**
	 * Handles when the options menu needs to be created
	 * @param	menu	the menu to inflate the layout into
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the layout
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity, menu);
		
		return true;
	}

	/**
	 * Handles when a menu item is selected
	 * @param item	 the menu item is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Check if it's the refresh button
		if(item.getItemId() == R.id.menu_refresh){

			// If it is, trigger a refresh
			if(mFileIndexFragment != null){
				mFileIndexFragment.refresh();
			}

			return true;
			
		}else if(item.getItemId() == R.id.menu_edit){
			
			// Otherwise, if it's the edit button
			final EditText input = new EditText(this);
			input.setText(mServerAddress.getHostAddress());

			// Build a server selector dialog
			new AlertDialog.Builder(this)
			.setTitle("Server IP Address")
			.setMessage("Enter the Server IP Address")
			.setView(input)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Editable value = input.getText(); 
					try {
						
						// Validate the user input and set the server
						if(value.toString().length() == 0){
							return;
						}
						
						mServerAddress = InetAddress.getByName(value.toString());
						mFileIndexFragment.setIP(mServerAddress);
						mFileIndexFragment.refresh();
					} catch (UnknownHostException e) {
						Log.e(getClass().getName(), "Unable to find server", e);
					}
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).show();
			return true;
		}else{
			return super.onOptionsItemSelected(item);
		}
	}
}
