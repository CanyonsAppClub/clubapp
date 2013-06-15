package com.canyonsappclub.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;

public class ClubApplication extends Application
{
	TabPagerAdapter pagerAdapter;
	
	boolean remindersNeedRefresh = true;
	final ArrayList<HashMap<Integer,Object>> reminders = new ArrayList<HashMap<Integer,Object>>();
}
