package com.canyonsappclub.app;

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;

public class MainActivity extends FragmentActivity
{
	//TabHost tabHost;
	ViewPager viewPager;
	HashMap<String, TabInfo> tabInfoMap = new HashMap<String, TabInfo>();
	
	final OnPageChangeListener pageChangeListener = new OnPageChangeListener()
	{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int index)
		{
			//tabHost.setCurrentTab(index);
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Fragment calendarFragment = new CalendarFragment();
		Fragment remindersFragment = new RemindersFragment();
		
		TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
		pagerAdapter.addFragment(calendarFragment,"Calendar");
		pagerAdapter.addFragment(remindersFragment, "Reminders");

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOffscreenPageLimit(2);
		viewPager.setCurrentItem(0);
		
		viewPager.setOnPageChangeListener(pageChangeListener);
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
}
