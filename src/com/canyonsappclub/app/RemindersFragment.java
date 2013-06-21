package com.canyonsappclub.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;

class Status
{
	int status;
}

class StringContainer
{
	String string;
}

class ManyRequestsThread extends Thread
{
	private final CountDownLatch latch;
	private final String requestString;
	private final StringContainer responseString;
	private final Status status;
		
	public ManyRequestsThread(CountDownLatch stopLatch, String requestString, StringContainer responseString, Status status)
	{
		this.latch = stopLatch;
		this.requestString = requestString;
		this.responseString = responseString;
		this.status = status;
	}
	
	public void run()
	{
		try
		{
			responseString.string = RemindersFragment.DoRequest(requestString,status);
			Log.d("Canyons App Club", responseString.string);
			latch.countDown();
		}
		finally //Will run even if the code in the try statement fails.
		{
			latch.countDown();
		}
	}
}

public class RemindersFragment extends ListFragment
{
	
	//LinearLayout remindersLayout, spinnerLayout; 
	LinearLayout spinnerLayout;
	ListView remindersList;
	
	int spinnerLayoutHeight = 0;
	
	ReminderItemAdapter adapter; 
	
	final static String baseUrl = "http://cdn.canyonsappclub.com/";
	//final String requestUrl = "http://cdn.canyonsappclub.com/sample/calendar.json";
	//final HttpClient client = new DefaultHttpClient();

	private ClubApplication app;

	//We need acess to our context within our functions and threads.
	//private Context ourContext;
	
	static HttpResponse ExecuteRequest(String requestUrl, Status status)
	{
		HttpClient client = new DefaultHttpClient();
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
			status.status = 1;
			return null;
		} 
		catch (IOException e)
		{
			// TODO
			e.printStackTrace();
			status.status = 2;
			return null;
		}
		return response;
	}
	
	static String DoRequest(String requestUrl, Status status)
	{
		HttpResponse response = ExecuteRequest(requestUrl, status);
		if(response == null){ return null; }
		String responseString;
		try {
			responseString = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status.status = 3;
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status.status = 4;
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
			final StringContainer[] responses = new StringContainer[requests.length];
			final Status[] statuses = new Status[requests.length];
			
			final CountDownLatch latch = new CountDownLatch(requests.length);
			for(int i = 0; i < requests.length; i++)
			{
				responses[i] = new StringContainer();
				statuses[i] = new Status();
				Thread requestThread = new ManyRequestsThread(latch,requests[i],responses[i],statuses[i]);
				requestThread.start();
			}
			try {
				latch.await();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			if(statuses[1].status == 0 && responses[1].string != null)
			{
				//Parse the location icon refrenses.
				try
				{
					String iconAbsoluteUrl;
					JSONObject mainJSONObject = new JSONObject(responses[1].string);
					JSONObject iconRefrenceObject = mainJSONObject.getJSONObject("icon-reference");
					iconAbsoluteUrl = iconRefrenceObject.getString("absolute_url");
					JSONArray locations = iconRefrenceObject.getJSONArray("locations");
					int locationsLength = locations.length();

					for(int i = 0; i < locationsLength; i++)
					{
						JSONObject location = locations.getJSONObject(i);
						int id = location.getInt("id");
						String path = location.getString("location_icon");

						//Download icon
						//Status status = new Status();
						//String response = DoRequest( baseUrl + iconAbsoluteUrl + path, status);
						//Is this wasting time converting from string or is it all good?
						//byte[] data = response.get
						URL  imageUrl = new URL(baseUrl + iconAbsoluteUrl + path);
						InputStream imageStream = (InputStream)imageUrl.getContent();
						Drawable imageDrawable = Drawable.createFromStream(imageStream, ":)");
						try
						{
							File ourFile = new File(app.getCacheDir(), path.split("/")[1]);
							FileOutputStream outStream = new FileOutputStream(ourFile);
							byte buffer[] = new byte[1024];
							int position;
							
									
									imageStream.read(buffer, position, 1024 - position))
							{
								if(position == 1024)
								{
									
								}
							}
							
							byte buffer[] = new byte[1024];
							int position = 0;
							while( length = Drawable)
							
							outStream.close();
							
						} catch(IOException e){ e.printStackTrace(); }
						
						//Save to cache
						
						
						//Make our Drawable

						app.icons.put(path, bitmap);
						app.iconPaths.put(id, path);
					}
				} catch(JSONException e)
				{
					e.printStackTrace();
				} catch(FileNotFoundException e)
				{
					e.printStackTrace();
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			
			if(statuses[0].status == 0 && responses[0].string != null)
			{
				//Now parse our events
				try 
				{
					JSONObject eventsJSONObject = new JSONObject(responses[0].string);
					JSONObject calendarObject = eventsJSONObject.getJSONObject("calendar");
					//imgUrl = calendarObject.getString("imgurl");
					JSONArray  eventsArray =	calendarObject.getJSONArray("events");


					app.reminders.clear();
					int eventsLength = eventsArray.length();
					for(int i = 0; i < eventsLength; i++)
					{
						JSONObject eventObject = eventsArray.getJSONObject(i);
						HashMap<Integer,Object> event = new HashMap<Integer,Object>();
						event.put(ReminderItemAdapter.PROPERTY_NAME, eventObject.getString("title"));
						event.put(ReminderItemAdapter.PROPERTY_SUBTITLE,eventObject.getString("subtitle"));
						event.put(ReminderItemAdapter.PROPERTY_DATE, eventObject.getString("time_period"));
						event.put(ReminderItemAdapter.PROPERTY_ICON, app.icons.get(eventObject.getInt("location_id"))); 
						app.reminders.add(event);
					}

				}
				//			catch (JSONException e) 
				//			{
				//				// TODO Auto-generated catch block
				//				e.printStackTrace();
				//			}
				catch (Exception e)
				{
					//TODO: Return parse error
					e.printStackTrace();
				}
			}
			
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
		//ourContext = inflater.getContext();
		
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
