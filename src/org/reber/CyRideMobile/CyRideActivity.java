package org.reber.CyRideMobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

/**
 * The entry point for the CyRide Android app.
 * 
 * @author brianreber
 */
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
					boolean moved = moveStatusForward();
					if (moved && status == ListViewStatus.ROUTE) {
						selectedRoute = new NameIdWrapper("",-1);
						selectedStation = new NameIdWrapper("",-1);
						createList();
					} else if (moved && status == ListViewStatus.STATIONS) {
						selectedRoute = listIds.get(arg2);
						selectedStation = new NameIdWrapper("",-1);
						createList();
					} else if (moved && status == ListViewStatus.TIMES_FOR_STATION) {
						selectedStation = listIds.get(arg2);
						createList();
					}
				}
			}
		});

		if (getCount() > 0) {
			status = ListViewStatus.ROUTE;
			createList();
		} else {
			status = ListViewStatus.ROUTE;
			new ImportDatabaseFileTask().execute();
		}
	}

	/**
	 * Move the status of the ListView forward
	 */
	private boolean moveStatusForward() {
		if (status == ListViewStatus.DATE) {
			status = ListViewStatus.ROUTE;
			return true;
		} else if (status == ListViewStatus.ROUTE) {
			status = ListViewStatus.STATIONS;
			return true;
		} else if (status == ListViewStatus.STATIONS) {
			status = ListViewStatus.TIMES_FOR_STATION;
			return true;
		} else if (status == ListViewStatus.TIMES_FOR_STATION) {
			//			status = ListViewStatus.TIMES_FOR_ROUTE;
			return false;
		} else if (status == ListViewStatus.TIMES_FOR_ROUTE) {
			status = ListViewStatus.TIMES_FOR_ROUTE;
			return true;
		}
		return false;
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

		createList();
	}

	/**
	 * Gets the String representation of the given int day
	 * 
	 * @param day
	 * @return
	 * Weekday if day = 0<br />
	 * Saturday if day = 1<br />
	 * Sunday if day = 2<br />
	 */
	private String getDayOfWeek(int day) {
		if (day == 0) {
			return "Weekday";
		} else if (day == 1) {
			return "Saturday";
		} else if (day == 2){
			return "Sunday";
		}

		throw new IllegalArgumentException("day should be between 0-3");
	}

	@SuppressWarnings("unchecked")
	/**
	 * Creates the ListView based on the current status
	 */
	private void createList() {
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
		} else if (status == ListViewStatus.STATIONS && selectedRoute.getId() != -1) {
			tv.setText(getDayOfWeek(db.getDayOfWeek()) + " - " + selectedRoute.getName());
			listIds = db.getStationNamesForRoute(selectedRoute.getId());
			list = new ArrayList<String>();
			for (int i = 0; i < listIds.size(); i++) {
				list.add(listIds.get(i).getName());				
			}
		} else if (status == ListViewStatus.TIMES_FOR_STATION && selectedRoute.getId() != -1 && selectedStation.getId() != -1) {
			tv.setText(getDayOfWeek(db.getDayOfWeek()) + " - " + selectedRoute.getName() + " - " + selectedStation.getName());
			list = db.getTimesForStation(selectedRoute.getId(), selectedStation.getId());
		} else {
			listIds = db.getRouteNames();
		}

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
	 * 
	 * @param menu
	 * The menu instance that we apply a menu to
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

		if (menuTitle.equals("Update Data From Server")) {
			getDataProcess();
		}
		if (menuTitle.equals("Count SQL Records")) {
			Dialog d = new Dialog(CyRideActivity.this);
			d.setCancelable(true);
			d.setTitle(getCount()+"");
			d.show();
		}
		if (menuTitle.equals("Export DB")) {
			exportDB();
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

	/**
	 * Gets data from the AppEngine Database and resets the in-app database with the new
	 * data from the server.
	 */
	private void getDataProcess() {
		final ProgressDialog prompt = new ProgressDialog(this);
		prompt.setMessage("Loading...");

		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					db.deleteAllRoutes();
					json = new JSONObject(getData());
					addRecords();
					json = null;
					handler.post(new Runnable() {
						@Override
						public void run() {
							createList();
							prompt.dismiss();
						}
					});
				} catch (Exception e) {		} 
			}
		}).start();

		prompt.show();
	}

	/**
	 * Gets the number of rows in the database
	 * 
	 * @return
	 * The number of rows in the database
	 */
	private int getCount() {
		return db.getCountRoute();
	}

	/**
	 * Gets the string of data from the server
	 */
	private String getData() throws MalformedURLException, IOException {
		String urlStr = "http://cyridesql.appspot.com/getroutes";

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(urlStr);
		HttpResponse response;

		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				return convertStreamToString(instream);
			}
		} catch (IOException e) {		}

		//If we get here, that means there was an error
		return null;
	}

	/**
	 * Converts an InputStream to a String
	 * 
	 * @param is
	 * The InputStream to convert
	 * @return
	 * The String contained in the InputStream
	 */
	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * Adds the records from the JSON string to the database
	 * 
	 * @throws JSONException
	 */
	private void addRecords() throws JSONException {
		JSONArray array = json.getJSONArray("records");
		List<Route> routes = new ArrayList<Route>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			routes.add(new Route(obj.getString("routename"), obj.getInt("routeid"), obj.getString("station"), obj.getInt("stationid"), 
					obj.getString("timestring"), obj.getInt("time"), obj.getInt("dayofweek"), obj.getInt("rownum")));
		}

		db.insertRoute(routes);
	}

	/**
	 * Exports the database to the SD card
	 */
	private void exportDB() {
		new ExportDatabaseFileTask().execute();
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
				TextView tt  = null;
				tt = (TextView) v.findViewById(R.id.label);

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
				} else if (temp.contains("Brown")) {
					iv.setImageResource(R.drawable.brown);
				} else if (temp.contains("Yellow")) {
					iv.setImageResource(R.drawable.yellow);
				} else if (temp.contains("Orange")) {
					iv.setImageResource(R.drawable.orange);
				} else if (temp.contains("Pink")) {
					iv.setImageResource(R.drawable.pink);
				} else if (temp.contains("Purple")) {
					iv.setImageResource(R.drawable.purple);
				} else if (temp.contains("Towers")) {
					iv.setImageResource(R.drawable.darkerbrown);
				} else if (temp.contains("Silver")) {
					iv.setImageResource(R.drawable.silver);
				} else if (temp.contains("Gray")) {
					iv.setImageResource(R.drawable.gray);
				} else if (temp.contains("Cardinal")) {
					iv.setImageResource(R.drawable.cardinal);
				} else if (temp.contains("Gold")) {
					iv.setImageResource(R.drawable.gold);
				}
			}
			return v;
		}
	}

	private class ImportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {
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
				createList();
			} else {
				Toast.makeText(CyRideActivity.this, "Transfer failed.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class ExportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(CyRideActivity.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Exporting database...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			File dbFile =
				new File(Environment.getDataDirectory() + "/data/org.reber.CyRideMobile/databases/cyride.db");

			File exportDir = new File(Environment.getExternalStorageDirectory(), "cyride");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File file = new File(exportDir, dbFile.getName());

			try {
				file.createNewFile();
				this.copyFile(dbFile, file);
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				Toast.makeText(CyRideActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(CyRideActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
			}
		}

		void copyFile(File src, File dst) throws IOException {
			FileChannel inChannel = new FileInputStream(src).getChannel();
			FileChannel outChannel = new FileOutputStream(dst).getChannel();
			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} finally {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			}
		}
	}
}