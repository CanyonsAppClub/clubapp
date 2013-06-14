package com.canyonsappclub.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReminderItemAdapter extends BaseAdapter 
{
	LayoutInflater inflator;
	int layoutToInflate;
	ArrayList<HashMap<Integer,Object>> data;
	
	public final static int PROPERTY_NAME = 0;
	public final static int PROPERTY_SUBTITLE = 1;
	public final static int PROPERTY_DATE = 2;
	public final static int PROPERTY_ICON = 3;
	
	public ReminderItemAdapter(Context context, int layoutToInflate, ArrayList<HashMap<Integer,Object>> contents)
	{
		inflator = LayoutInflater.from(context);
		this.layoutToInflate = layoutToInflate;
		data = contents;
	}
	
	
	@Override
	public int getCount()
	{
		return data.size();
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		if(view == null)
		{ view = inflator.inflate(layoutToInflate,null); }
		
		HashMap<Integer,Object> item = data.get(position);
		
		TextView name = (TextView)view.findViewById(R.id.reminderName);
		TextView location = (TextView)view.findViewById(R.id.reminderSubtitle);
		TextView date = (TextView)view.findViewById(R.id.reminderDate);
		ImageView icon = (ImageView)view.findViewById(R.id.reminderIcon);
		
		name.setText((CharSequence) item.get(PROPERTY_NAME));
		location.setText((CharSequence) item.get(PROPERTY_SUBTITLE));
		date.setText((CharSequence) item.get(PROPERTY_DATE));
		icon.setImageDrawable((Drawable) item.get(PROPERTY_ICON));
		
		return view;
	}

}
