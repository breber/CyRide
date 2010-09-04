package org.reber.CyRideMobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CyRideActivity extends Activity {
	private JSONObject json;
	private CyRideDB db;
	private ListView lv;
	private Adapter adapter;
	
	private List<String> list;
	private List<NameIdWrapper> listIds;
	
	private static ListViewStatus status;
	
	private NameIdWrapper selectedRoute, selectedStation;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		db = new CyRideDB(this);

		Calendar c = Calendar.getInstance();
		int tempDOW = c.get(Calendar.DAY_OF_WEEK);
		if (tempDOW == Calendar.SUNDAY) {
			db.setDayOfWeek(2);
		} else if (tempDOW == Calendar.SATURDAY) {
			db.setDayOfWeek(1);
		} else {
			db.setDayOfWeek(0);
		}

		selectedRoute = new NameIdWrapper("", -1);
		selectedStation = new NameIdWrapper("", -1);
		list = new ArrayList<String>();
		listIds = new ArrayList<NameIdWrapper>();
		
		lv = (ListView) findViewById(R.id.ListView);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (status == ListViewStatus.DATE) {
					db.setDayOfWeek(arg2);
				} 
				if (status != ListViewStatus.TIMES_FOR_ROUTE) {
					moveStatusForward();
					if (status == ListViewStatus.ROUTE) {
						selectedRoute = new NameIdWrapper("",-1);
						selectedStation = new NameIdWrapper("",-1);
						createList(selectedRoute.getId(), selectedStation.getId());
					} else if (status == ListViewStatus.STATIONS) {
						selectedRoute = listIds.get(arg2);
						selectedStation = new NameIdWrapper("",-1);
						createList(selectedRoute.getId(), selectedStation.getId());
					} else if (status == ListViewStatus.TIMES_FOR_STATION) {
						selectedStation = listIds.get(arg2);
						createList(selectedRoute.getId(), selectedStation.getId());
					}
				}
			}
		});
		
		if (getCount() > 0) {
			status = ListViewStatus.ROUTE;
			createList(-1, -1);
		} else {
			status = ListViewStatus.ROUTE;
			getDataProcess(true);
		}
	}
	
	private void moveStatusForward() {
		if (status == ListViewStatus.DATE) {
			status = ListViewStatus.ROUTE;
		} else if (status == ListViewStatus.ROUTE) {
			status = ListViewStatus.STATIONS;
		} else if (status == ListViewStatus.STATIONS) {
			status = ListViewStatus.TIMES_FOR_STATION;
		} else if (status == ListViewStatus.TIMES_FOR_STATION) {
			status = ListViewStatus.TIMES_FOR_ROUTE;
		} else if (status == ListViewStatus.TIMES_FOR_ROUTE) {
			status = ListViewStatus.TIMES_FOR_ROUTE;
		}
		Log.d("STATUS", "Moved Forward To: " + status.name());
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (status == ListViewStatus.DATE) {
			super.onBackPressed();
		} else if (status == ListViewStatus.ROUTE) {
			status = ListViewStatus.DATE;
		} else if (status == ListViewStatus.STATIONS) {
			status = ListViewStatus.ROUTE;
		} else if (status == ListViewStatus.TIMES_FOR_STATION) {
			status = ListViewStatus.STATIONS;
		} else if (status == ListViewStatus.TIMES_FOR_ROUTE) {
			status = ListViewStatus.TIMES_FOR_STATION;
		}
		
		Log.d("STATUS", "Back Pressed - moved to: " + status.name());
		
		createList(selectedRoute.getId(), selectedStation.getId());
	}
	
	@SuppressWarnings("unchecked")
	private void createList(int routeId, int stationId) {
		list = new ArrayList<String>();
		if (status == ListViewStatus.DATE) {
			list = new ArrayList<String>();
			list.add("Weekday");
			list.add("Saturday");
			list.add("Sunday");
		} else if (status == ListViewStatus.ROUTE) {
			listIds = db.getRouteNames();
			list = new ArrayList<String>();
			for (int i = 0; i < listIds.size(); i++) {
				list.add(listIds.get(i).getName());				
			}
		} else if (status == ListViewStatus.STATIONS && routeId != -1) {
			listIds = db.getStationNamesForRoute(routeId);
			list = new ArrayList<String>();
			for (int i = 0; i < listIds.size(); i++) {
				list.add(listIds.get(i).getName());				
			}
		} else if (status == ListViewStatus.TIMES_FOR_STATION && routeId != -1 && stationId != -1) {
			list = db.getTimesForRouteAndStation(routeId, stationId);
		} else {
			listIds = db.getRouteNames();
		}
		
		Log.d("LIST", list.size()+"");
		
		int layout;
		
		if (status == ListViewStatus.TIMES_FOR_STATION || status == ListViewStatus.TIMES_FOR_ROUTE) {
			layout = android.R.layout.simple_list_item_2;
		} else {
			layout = android.R.layout.simple_list_item_1;
		}
		
		adapter = new CyRideListAdapter(this, layout, list);

		lv.setAdapter((ArrayAdapter<String>) adapter);
	}
	
	/**
	 * Gets called when the menu button is pressed.
	 * param menu
	 * The menu instance that we apply a menu to
	 * 
	 * @return
	 * true so that it uses our own implementation
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.cyridemenu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String menuTitle = item.getTitle().toString();

		if (menuTitle.equals("Get Data")) {
			getDataProcess(true);
		}
		if (menuTitle.equals("Count SQL Records")) {
			Dialog d = new Dialog(CyRideActivity.this);
			d.setCancelable(true);
			d.setTitle(getCount()+"");
			d.show();
		}
		if (menuTitle.equals("Open DB")) {
			db.open();
		}
		if (menuTitle.equals("Close DB")) {
			db.close();
		}
		if (menuTitle.equals("Delete Records")) {
			db.deleteAllRoutes();
			Dialog d = new Dialog(CyRideActivity.this);
			d.setCancelable(true);
			d.setTitle("DB Cleared");
			d.show();
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		db.close();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		db.open();
	}
	
	private void getDataProcess(final boolean loadView) {
		final Dialog prompt = new Dialog(this);
		prompt.setTitle("Loading...");

		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					json = new JSONObject(getData());
					Log.i("Data", "Got Data");	
					handler.post(new Runnable() {
						@Override
						public void run() {
							prompt.dismiss();
							
							try {
								addRecords();
								json = null;
								if (loadView) {
									createList(-1, -1);
								}
							} catch (JSONException e) {	}
						}
					});
				} catch (Exception e) {		} 
			}
		}).start();

		prompt.show();
	}
	
	private int getCount() {
		return db.getCountRoute();
	}

	private String getData() throws MalformedURLException, IOException {
		String urlStr = "http://cyridesql.appspot.com/getroutes";
		
		URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String temp = reader.readLine();
		
		while (temp != null) {
			sb.append(temp);
			temp = reader.readLine();
		}

		return sb.toString();
	}

	private void addRecords() throws JSONException {
		JSONArray array = json.getJSONArray("records");
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			db.insertRoute(obj.getInt("routeid"), obj.getString("routename"), obj.getString("station"), obj.getInt("stationid"), 
					obj.getString("timestring"), obj.getInt("time"), obj.getInt("dayofweek"), obj.getInt("rownum"));
		}
	}
	
	private class CyRideListAdapter extends ArrayAdapter<String> {
		private List<String> items;

		public CyRideListAdapter(Context context, int textViewResourceId, List<String> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			String text = items.get(position);
			if (text != null) {
				ImageView iv = (ImageView) v.findViewById(R.id.icon);
				TextView tt = (TextView) v.findViewById(R.id.label);
				
				if (tt != null) {
					tt.setText(text);                            
				}
				
				if (status == ListViewStatus.DATE) {
					iv.setImageResource(R.drawable.clear);
					return v;
				}
				
				String temp = "";
				if (status == ListViewStatus.ROUTE) {
					temp = text;
				} else {
					temp = selectedRoute.getName();
				}
				
				if (temp.contains("Red")) {
					iv.setImageResource(R.drawable.red);
				} else if (temp.contains("Blue")) {
					iv.setImageResource(R.drawable.blue);
				} else if (temp.contains("Green")) {
					iv.setImageResource(R.drawable.green);
				} else if (temp.contains("Brown") || temp.contains("Towers")) {
					iv.setImageResource(R.drawable.brown);
				} else if (temp.contains("Yellow")) {
					iv.setImageResource(R.drawable.yellow);
				} else if (temp.contains("Orange")) {
					iv.setImageResource(R.drawable.orange);
				} else if (temp.contains("Pink")) {
					iv.setImageResource(R.drawable.pink);
				} else if (temp.contains("Purple")) {
					iv.setImageResource(R.drawable.purple);
				}
			}
			return v;
		}
	}
}