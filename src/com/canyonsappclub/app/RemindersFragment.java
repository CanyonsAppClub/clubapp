package com.canyonsappclub.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;

public class RemindersFragment extends ListFragment
{
	
	//LinearLayout remindersLayout, spinnerLayout; 
	LinearLayout spinnerLayout;
	ListView remindersList;
	
	int spinnerLayoutHeight = 0;
	
	ReminderItemAdapter adapter; 
	
	
	//final String requestUrl = "http://cdn.canyonsappclub.com/sample/calendar.json";
	final HttpClient client = new DefaultHttpClient();
	
	private ArrayList<HashMap<Integer,Object>> reminders;
	
	final Runnable fetchRemindersRunnable = new Runnable()
	{
		@Override
		public void run() 
		{
			HttpGet request = new HttpGet(requestUrl);
			HttpResponse response;
			try 
			{
				response = client.execute(request);
			}
			catch (ClientProtocolException e)
			{
				//TODO
				e.printStackTrace();
				return;
			} 
			catch (IOException e)
			{
				// TODO
				e.printStackTrace();
				return;
			}
			
			String responseString;
			
			try {
				responseString = EntityUtils.toString(response.getEntity());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			String imgUrl;
			
			try 
			{
				JSONObject mainJSONObject = new JSONObject(responseString);
				JSONObject calendarObject = mainJSONObject.getJSONObject("calendar");
				imgUrl = calendarObject.getString("imgurl");
				JSONArray  eventsArray =	calendarObject.getJSONArray("events");
				
				
				
				reminders.clear();
				int eventsLength = eventsArray.length();
				for(int i = 0; i < eventsLength; i++)
				{
					JSONObject eventObject = eventsArray.getJSONObject(i);
					HashMap<Integer,Object> event = new HashMap<Integer,Object>();
					event.put(ReminderItemAdapter.PROPERTY_NAME, eventObject.getString("title"));
					event.put(ReminderItemAdapter.PROPERTY_SUBTITLE,eventObject.getString("subtitle"));
					event.put(ReminderItemAdapter.PROPERTY_DATE, eventObject.getString("time_period"));
					
					URL url = null;
					Drawable drawable = null;
					try {
						url = new URL("http://cdn.canyonsappclub.com/" + imgUrl + eventObject.getString("icon"));
						InputStream content;
						content = (InputStream)url.getContent();
						drawable = Drawable.createFromStream(content, ":)");
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
					event.put(ReminderItemAdapter.PROPERTY_ICON, drawable );
					reminders.add(event);
				}
				
			}
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//adapter.notifyDataSetChanged();
			
			spinnerLayout.post(new Runnable()
			{
				@Override
				public void run() 
				{
					switchFromSpinner();
				}
			});
			
		}
	};
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_reminders, container, false);

		//remindersLayout = (LinearLayout) view.findViewById(R.id.remindersLayout);
		spinnerLayout = (LinearLayout) view.findViewById(R.id.spinnerLayout);

		remindersList = (ListView)view.findViewById(android.R.id.list);
		
		ViewTreeObserver observer = spinnerLayout.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout() 
			{
				spinnerLayoutHeight = spinnerLayout.getHeight();				
				spinnerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}	
		});
		
		
		ClubApplication app = (ClubApplication)inflater.getContext().getApplicationContext();
		this.reminders = app.reminders;
		
		if(app.remindersNeedRefresh)
		{
			Thread reminderFetchThread = new Thread(fetchRemindersRunnable);
			reminderFetchThread.start();
			app.remindersNeedRefresh = false;
		}
		else
		{
			switchFromSpinner();
		}
		
		adapter = new ReminderItemAdapter(inflater.getContext(), R.layout.item_reminder, reminders);
		setListAdapter(adapter);
		
        return view;
    }
	
	//Should be called in UI thread
	private void switchFromSpinner()
	{	
		ReminderItemAdapter adapter = (ReminderItemAdapter) remindersList.getAdapter();
		if(adapter != null)
		{ adapter.notifyDataSetChanged(); }
		
		ViewTreeObserver observer = remindersList.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{

			@Override
			public void onGlobalLayout() 
			{
				remindersList.post(new Runnable(){

					@Override
					public void run() {
						
						LayoutParams params = remindersList.getLayoutParams();
						params.height = spinnerLayoutHeight + remindersList.getHeight();
						remindersList.setLayoutParams(params);
						
						final TranslateAnimation animation = new TranslateAnimation(0,0,0,-spinnerLayoutHeight);
						animation.setDuration(500);
						animation.setFillEnabled(true);
						animation.setFillAfter(true);
						remindersList.setAnimation(animation);
						spinnerLayout.setAnimation(animation);
						spinnerLayout.postDelayed(new Runnable()
						{
							@Override
							public void run() 
							{
								spinnerLayout.clearAnimation();
								remindersList.clearAnimation();
								spinnerLayout.setVisibility(View.GONE);
							}
						}, 500);
						
					}
					
				});
				
				remindersList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
			
		});
		
		
		
		
		
	}

}
