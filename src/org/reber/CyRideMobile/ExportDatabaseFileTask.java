package org.reber.CyRideMobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

/**
 * Utility class for Exporting the database from the app data folder to
 * a folder on the SD Card.
 * 
 * @author brian
 */
public class ExportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {
	private Context ctx;
	private final ProgressDialog dialog;
	
	public ExportDatabaseFileTask(Context ctx) {
		this.ctx = ctx;
		dialog = new ProgressDialog(this.ctx);
	}

	// can use UI thread here
	protected void onPreExecute() {
		this.dialog.setMessage("Exporting database...");
		this.dialog.show();
	}

	// automatically done on worker thread (separate from UI thread)
	protected Boolean doInBackground(final String... args) {
		File dbFile = new File(Environment.getDataDirectory() + "/data/" + Utilities.PACKAGE + "/databases/cyride.db");
		File exportDir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + Utilities.PACKAGE);
		
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
			Toast.makeText(ctx, "Export successful!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(ctx, "Export failed", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Copies the file from the src to the dst
	 * 
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	private void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
	}
}
