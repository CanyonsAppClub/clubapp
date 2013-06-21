package com.canyonsappclub.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.graphics.drawable.Drawable;

public class ClubApplication extends Application
{
	TabPagerAdapter pagerAdapter;
	
	boolean remindersNeedRefresh = true;
	
	final ArrayList<HashMap<Integer,Object>> reminders = new ArrayList<HashMap<Integer,Object>>();
	//A directory of all our icons.
	final HashMap<Integer,String> iconPaths = new HashMap<Integer,String>();
	final HashMap<String,Drawable> icons = new HashMap<String,Drawable>();
	
}
