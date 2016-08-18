package com.piled.weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlarmManager;
import android.app.Activity;
//import android.app.PendingIntent;
import android.content.Intent;
//import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import android.content.Context;

import java.net.URLEncoder;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity {

    private static final String TAG = "weather";
    
    private ListView mList;
    private TextView mDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG, "onCreate()");
        mList = (ListView)findViewById(R.id.forecastList);
        mDescription = (TextView)findViewById(R.id.description);
        //new ReadyTask(mapFragment).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        checkTheWeather("Nome, AK");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu():");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        final MenuItem searchMenu = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView)searchMenu.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(final String query) {
                if (query == null || query.length() == 0) {
                    return true;
                }
                final String updatedQuery = query.trim().replace("\n", "").replace("\r", "");
                checkTheWeather(updatedQuery);
                searchView.clearFocus();
                searchView.setIconified(true);
                searchMenu.collapseActionView();
                return true;
            }
            public boolean onQueryTextChange(final String query) {
                return false;
            }
        });
        return true;
    }
    
    private void checkTheWeather(String thePlace) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" + URLEncoder.encode(thePlace) +
           //nome%2C%20ak%
           "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //mTxtDisplay.setText("Response: " + response.toString());
                Log.d(TAG, "Got " + response.toString());
                JSONObject query = response.optJSONObject("query");
                Log.d(TAG, "query " + query.toString());
                JSONObject results = query.optJSONObject("results");
                Log.d(TAG, "results " + results.toString());
                JSONObject channel = results.optJSONObject("channel");
                Log.d(TAG, "channel " + channel.toString());
                String title = channel.optString("title");
                getActionBar().setTitle(title);
                JSONObject item = channel.optJSONObject("item");
                Log.d(TAG, "item " + item.toString());

                JSONArray forecast = item.optJSONArray("forecast");
                Log.d(TAG, "forecast " + forecast.toString());
                mList.setAdapter(new ForecastAdapter(forecast));
                String description = item.optString("description");
                Log.d(TAG, "description " + description);
                mDescription.setText(Html.fromHtml(Html.fromHtml(description).toString()));
                mDescription.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Got " + error.toString());
            }
        });
        queue.add(jsObjRequest);
    }
    
    private static class ForecastAdapter extends BaseAdapter {
        private static class ViewTag {
            TextView date;
            TextView day;
            TextView high;
            TextView low;
            TextView text;
        }
        JSONArray mForecast;
        public ForecastAdapter(JSONArray forecast) {
            mForecast = forecast;
        }
        @Override
        public int getCount() {
            return mForecast.length();
        }
        @Override
        public JSONObject getItem(final int position) {
            if (position >= getCount()) {
                return null;
            }
            return mForecast.optJSONObject(position);
        }
        @Override
        public long getItemId(final int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view;
            ViewTag tag;
            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item_view, null);
                tag = new ViewTag();
                tag.date = (TextView)view.findViewById(R.id.item_date);
                tag.day = (TextView)view.findViewById(R.id.item_day);
                tag.high = (TextView)view.findViewById(R.id.item_high);
                tag.low = (TextView)view.findViewById(R.id.item_low);
                tag.text = (TextView)view.findViewById(R.id.item_text);
                view.setTag(tag);
            } else {
                view = convertView;
                tag = (ViewTag)view.getTag();
            }
            JSONObject item = getItem(position);
            tag.date.setText(item.optString("date"));
            tag.day.setText(item.optString("day"));
            tag.high.setText(item.optString("high"));
            tag.low.setText(item.optString("low"));
            tag.text.setText(item.optString("text"));
            return view;
        }
    }
    
}
