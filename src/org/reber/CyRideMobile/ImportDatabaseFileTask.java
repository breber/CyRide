package org.reber.CyRideMobile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

/**
 * Utility class for Importing the database from the resource folder 
 * in the APK to the data folder of the app.
 * 
 * @author brian
 */
public class ImportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {
	private Context ctx;
	private final ProgressDialog dialog;
	
	public ImportDatabaseFileTask(Context ctx) {
		this.ctx = ctx;
		dialog = new ProgressDialog(this.ctx);
	}

	// can use UI thread here
	protected void onPreExecute() {
		this.dialog.setMessage("Loading database...");
		this.dialog.show();
	}

	// automatically done on worker thread (separate from UI thread)
	protected Boolean doInBackground(final String... args) {
		File dbFile = new File(Environment.getDataDirectory() + "/data/" + Utilities.PACKAGE + "/databases/cyride.db");
		InputStream db = ctx.getResources().openRawResource(R.raw.cyride);
		OutputStream out;
		try {
			out = new FileOutputStream(dbFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len = db.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
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
			// Not necessarily a good idea - unsafe cast to CyRideActivity, but it should
			// be fine for this project because I am the only one that will be calling this method...
			((CyRideActivity) ctx).createList();
		} else {
			Toast.makeText(ctx, "Transfer failed.", Toast.LENGTH_SHORT).show();
		}
	}
}
