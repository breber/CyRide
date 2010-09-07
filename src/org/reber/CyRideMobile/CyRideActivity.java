package org.reber.CyRideMobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

public class CyRideActivity extends Activity {
	private JSONObject json;
	private CyRideDB db;
	private ListView lv;
	private TextView tv;
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

		tv = (TextView) findViewById(R.id.text);

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
//			getDataProcess(true);
			new ExportDatabaseFileTask().execute();
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
			//			status = ListViewStatus.TIMES_FOR_ROUTE;
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

	private String getDayOfWeek(int day) {
		if (day == 0) {
			return "Weekday";
		} else if (day == 1) {
			return "Saturday";
		} else {
			return "Sunday";
		}
	}

	@SuppressWarnings("unchecked")
	private void createList(int routeId, int stationId) {
		list = new ArrayList<String>();
		if (status == ListViewStatus.DATE) {
			tv.setText(R.string.welcome);
			list = new ArrayList<String>();
			list.add("Weekday");
			list.add("Saturday");
			list.add("Sunday");
		} else if (status == ListViewStatus.ROUTE) {
			tv.setText(getDayOfWeek(db.getDayOfWeek()));
			listIds = db.getRouteNames();
			list = new ArrayList<String>();
			for (int i = 0; i < listIds.size(); i++) {
				list.add(listIds.get(i).getName());				
			}
		} else if (status == ListViewStatus.STATIONS && routeId != -1) {
			tv.setText(getDayOfWeek(db.getDayOfWeek()) + " - " + selectedRoute.getName());
			listIds = db.getStationNamesForRoute(routeId);
			list = new ArrayList<String>();
			for (int i = 0; i < listIds.size(); i++) {
				list.add(listIds.get(i).getName());				
			}
		} else if (status == ListViewStatus.TIMES_FOR_STATION && routeId != -1 && stationId != -1) {
			tv.setText(getDayOfWeek(db.getDayOfWeek()) + " - " + selectedRoute.getName() + " - " + selectedStation.getName());
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
		final Dialog prompt = new ProgressDialog(this);
		prompt.setTitle("Loading...");

		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					json = new JSONObject(getData());
					handler.post(new Runnable() {
						@Override
						public void run() {
							try {
								addRecords();
								json = null;
								if (loadView) {
									createList(-1, -1);
								}
							} catch (JSONException e) {	} 
							finally {
								prompt.dismiss();
							}
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
		int temp = reader.read();

		while (temp != -1) {
			sb.append(temp);
			temp = reader.read();
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
				if (status == ListViewStatus.TIMES_FOR_STATION || status == ListViewStatus.TIMES_FOR_ROUTE) {
					v = vi.inflate(R.layout.timerow, null);
				} else {
					v = vi.inflate(R.layout.row, null);
				}
			}
			String text = items.get(position);
			if (text != null) {
				ImageView iv = (ImageView) v.findViewById(R.id.icon);
				TextView tt  = null;
				if (status == ListViewStatus.TIMES_FOR_STATION || status == ListViewStatus.TIMES_FOR_ROUTE) {
					tt = (TextView) v.findViewById(R.id.labeltime);
				} else {
					tt = (TextView) v.findViewById(R.id.label);
				}

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

	private class ExportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(CyRideActivity.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Loading database...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {
			File dbFile = new File(Environment.getDataDirectory() + "/data/org.reber.CyRideMobile/databases/cyride.db");
			InputStream db = CyRideActivity.this.getResources().openRawResource(R.raw.cyride);
			OutputStream out;
			try {
				out = new FileOutputStream(dbFile);
				byte buf[]=new byte[1024];
				int len;
				while((len = db.read(buf)) > 0)
					out.write(buf,0,len);
				out.close();
				db.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return true;
		}

		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				createList(-1, -1);
//				Toast.makeText(CyRideActivity.this, "Transfer successful!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(CyRideActivity.this, "Transfer failed.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}