package com.msoe.ce4960.sommere.lab5;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity{


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		}else{
			return super.onOptionsItemSelected(item);
		}
		
	}
}
