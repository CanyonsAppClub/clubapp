package com.canyonsappclub.app;

import java.io.File;
import java.io.FileInputStream;
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

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
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
	
	final static String baseUrl = "http://appclub-test.sianware.com/";

	private ClubApplication app;
	
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
			final String[] requests = new String[]{baseUrl+"app/json/events/",baseUrl+"app/json/locations/"};
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
				ProcessLocationIconJson(responses[1].string);
			}
			
			if(statuses[0].status == 0 && responses[0].string != null)
			{
				ProcessRemindersJson(responses[0].string);
				
				spinnerLayout.post(new Runnable()
				{
					@Override
					public void run() 
					{
						switchFromSpinner();
					}
				});
			}
			
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
		
		//First, let's put stuff we have from cache in our list, if we have such stuff
		LoadFromCache();
		
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
	
	private void LoadFromCache()
	{
		Thread loadFromCacheThread = new Thread(new Runnable()
		{

			@Override
			public void run() {
				String locationIconJson = GetStringFromFile(app.getCacheDir()+"/locationIconCache.json");
				String remindersJson = GetStringFromFile(app.getCacheDir()+"/remindersCache.json");
				ProcessLocationIconJson(locationIconJson);
				ProcessRemindersJson(remindersJson);
			}
			
		});
		loadFromCacheThread.start();
		try {
			loadFromCacheThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private String GetStringFromFile(String filename)
	{
		String contents = null;
		File file = new File(filename);
		try {
			FileInputStream inputStream = new FileInputStream(file);
			byte[] fileBuffer = new byte[(int) file.length()];
			inputStream.read(fileBuffer);
			inputStream.close();
			contents = new String(fileBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contents;
	}
	
	private void SaveStringToFile(String filename, String contents)
	{
		File outFile = new File(filename);
		FileOutputStream outStream;
		byte[] contentBytes = contents.getBytes();
		try 
		{
			outStream = new FileOutputStream(outFile);
			outStream.write(contentBytes);
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void ProcessLocationIconJson(String json)
	{
		//Parse the location icon references.
		try
		{
			JSONArray mainJSONArray = new JSONArray(json);
			int mainArrayLength = mainJSONArray.length();
			for(int i = 0; i < mainArrayLength; i++)
			{
				JSONObject object = mainJSONArray.getJSONObject(i);
				int id = object.getInt("pk");
				
				JSONObject fields = object.getJSONObject("fields");
				String path = fields.getString("icon_file");
				
				//Download icon and put it in a drawable
				URL  imageUrl = new URL(baseUrl + "media/" + path);
				InputStream imageStream = (InputStream)imageUrl.getContent();
				Drawable imageDrawable = Drawable.createFromStream(imageStream, ":)");
				imageStream.close();
				//Save image to cache
				Bitmap bitmap = ((BitmapDrawable)(imageDrawable)).getBitmap();
				File ourFile = new File(app.getCacheDir()+"/loc_img/"+path);
				ourFile.getParentFile().mkdirs(); //Make our directories if they don't exist.
				FileOutputStream outStream = new FileOutputStream(ourFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
				outStream.close();
				
				app.icons.put(path, imageDrawable);
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
		
		//Save location icon json in cache.
		SaveStringToFile(app.getCacheDir()+"/locationIconCache.json", json);
	}
	
	private void ProcessRemindersJson(String json)
	{
		//Now parse our events
		try 
		{
			JSONArray mainJSONArray = new JSONArray(json);
			int mainArrayLength = mainJSONArray.length();
			for(int i = 0; i < mainArrayLength; i++)
			{
				JSONObject object = mainJSONArray.getJSONObject(i);
				JSONObject fields = object.getJSONObject("fields");
				
				HashMap<Integer,Object> event = new HashMap<Integer,Object>();
				event.put(ReminderItemAdapter.PROPERTY_NAME, Html.fromHtml(fields.getString("event_title")).toString());
				event.put(ReminderItemAdapter.PROPERTY_SUBTITLE, Html.fromHtml(fields.getString("event_subtitle")).toString());
				event.put(ReminderItemAdapter.PROPERTY_DATE, Html.fromHtml(fields.getString("start_date")).toString());
				int iconId = fields.getInt("location");
				event.put(ReminderItemAdapter.PROPERTY_ICON, app.icons.get(app.iconPaths.get(iconId))); 
				app.reminders.add(event);
				
			}

		}
		catch (Exception e)
		{
			//TODO: Return parse error
			e.printStackTrace();
		}
	
		//Save reminders json file in cache
		SaveStringToFile(app.getCacheDir()+"/remindersCache.json", json);
	
	}
}
