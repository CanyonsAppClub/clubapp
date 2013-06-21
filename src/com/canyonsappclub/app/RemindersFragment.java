package com.canyonsappclub.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

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

import android.content.Context;
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
	
	final static String baseUrl = "http://cdn.canyonsappclub.com/";
	final String requestUrl = "http://cdn.canyonsappclub.com/sample/calendar.json";
	final HttpClient client = new DefaultHttpClient();
	
	//private ArrayList<HashMap<Integer,Object>> reminders;
	//private HashMap<Integer,String> iconPaths;
	//private HashMap<String,Drawable> locationIcons;
	private ClubApplication app;

	//We need acess to our context within our functions and threads.
	private Context ourContext;
	
	HttpResponse ExecuteRequest(String requestUrl)
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
			return null;
		} 
		catch (IOException e)
		{
			// TODO
			e.printStackTrace();
			return null;
		}
		return response;
	}
	
	String DoRequest(String requestUrl)
	{
		HttpResponse response = ExecuteRequest(requestUrl);
		String responseString;
		try {
			responseString = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return responseString;
	}
	
	
	
	
	final Runnable fetchRemindersRunnable = new Runnable()
	{
		@Override
		public void run() 
		{
			final String[] requests = new String[]{baseUrl+"app/events/",baseUrl+"app/loc_icon_ref/"};
			final String[] responses = new String[requests.length];
			final CountDownLatch latch = new CountDownLatch(requests.length);
			for(final int i = 0; i < requests.length; i++)
			{
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						responses[i] = DoRequest(requests[i]);
						latch.countDown();
					}
				}).start();
			}
			latch.wait();
			
			
			//Parse the location icon refrenses.
			String iconAbsoluteUrl;
			
			JSONObject mainJSONObject = new JSONObject(responses[1]);
			JSONObject iconRefrenceObject = mainJSONObject.getJSONObject("icon-refrence");
			iconAbsoluteUrl = iconRefrenceObject.getString("absolute_url");
			JSONArray locations = iconRefrenceObject.getJSONArray("locations");
			int locationsLength = locations.length();
			
			for(int i = 0; i < locationsLength; i++)
			{
				JSONObject location = locations.getJSONObject(i);
				int id = location.getInt("id");
				String path = location.getString("location_icon");
				
				//Download and save icon.
				String response = DoRequest( baseUrl + iconAbsoluteUrl + path);
				bytep[] data = response.getBytes("UTF8");
				HttpResponse iconResponse = ExecuteRequest(baseUrl + iconAbsoluteUrl + path);
				S;
				
						
				URL url = new URL(baseUrl + iconAbsoluteUrl + path);
				InputStream content;
				content = (InputStream)url.getContent();
				
				content.
				
				//Save to cache
				File ourFile = new File(ourContext.getCacheDir(), path);
				FileOutputStream outStream = new FileOutputStream(ourFile);
				
				
				
				Drawable.
				
				drawable = Drawable.createFromStream(content, ":)");
					
				iconPaths.put(id, path);
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
					event.put(ReminderItemAdapter.PROPERTY_DATE, TimeManager.convertFromIsoFormat(eventObject.getString("timeperiod")));
					
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
		
		//We'll need this later.
		app = (ClubApplication)inflater.getContext().getApplicationContext();
		
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
		
		adapter = new ReminderItemAdapter(inflater.getContext(), R.layout.item_reminder, app.reminders);
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
