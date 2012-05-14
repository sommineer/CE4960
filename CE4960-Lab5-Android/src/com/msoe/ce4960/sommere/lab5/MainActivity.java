package com.msoe.ce4960.sommere.lab5;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.R.mipmap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends Activity{


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			serverAddress = InetAddress.getByName("155.92.67.104");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity, menu);


		return true;
	}

	private FileIndexFragment mFileIndexFragment;

	@Override
	public void onAttachFragment(Fragment fragment) {
		if(fragment instanceof FileIndexFragment){
			mFileIndexFragment = (FileIndexFragment) fragment;

			mFileIndexFragment.setIP(serverAddress);

		}
		super.onAttachFragment(fragment);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_refresh){

			if(mFileIndexFragment != null){
				mFileIndexFragment.refresh();
			}

			return true;
		}else if(item.getItemId() == R.id.menu_edi){
			final EditText input = new EditText(this);
			input.setText(serverAddress.getHostAddress());

			new AlertDialog.Builder(this)
			.setTitle("Server IP Address")
			.setMessage("Enter the Server IP Address")
			.setView(input)

			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Editable value = input.getText(); 
					try {
						if(value.toString().length() == 0){
							return;
						}
						serverAddress = InetAddress.getByName(value.toString());
						mFileIndexFragment.setIP(serverAddress);
						mFileIndexFragment.refresh();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	private InetAddress serverAddress;
}
