package com.canyonsappclub.app;

import android.content.Context;
import android.view.View;
import android.widget.TabHost.TabContentFactory;

public class TabFactory implements TabContentFactory 
{
	private Context context;
	
	public TabFactory(Context context)
	{
		this.context = context;
	}
	
	@Override
	public View createTabContent(String tag)
	{
		View view = new View(context);
		view.setMinimumWidth(0);
		view.setMinimumHeight(0);
		return view;
	}
	
}
