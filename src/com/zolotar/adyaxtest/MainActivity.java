package com.zolotar.adyaxtest;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity implements OnSeekBarChangeListener {

	private SeekBar globalSeek;
	private SeekBar windowSeek;
	private Button saveDB;
	private Button restoreDB;
	private Button getFromHttp;
	private String toast;
	private int systemBrightness;
	private DBHelper dbHelper;
	private final String LOG_TAG = "myLogs";
	private SQLiteDatabase sqlDB;
	private ContentValues cv;
	private AlertDialog.Builder alert;
	private int windowProgress;
	private ScrollView scroll;

	private static String url = "http://flask-docs.ru/phonegap/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		globalSeek = (SeekBar) findViewById(R.id.global);
		windowSeek = (SeekBar) findViewById(R.id.window);
		saveDB = (Button) findViewById(R.id.saveDB);
		restoreDB = (Button) findViewById(R.id.restoreFromDB);
		getFromHttp = (Button) findViewById(R.id.getFromHttp);
		scroll = (ScrollView) findViewById(R.id.ScrollView1);

		alert = new AlertDialog.Builder(this);

		dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, null, 1);

		cv = new ContentValues();
		sqlDB = dbHelper.getWritableDatabase();

		try {
			systemBrightness = android.provider.Settings.System.getInt(
					getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS);

		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}

		int globalProgress = systemBrightness * 100 / 255;
		globalSeek.setProgress(globalProgress);

		if (getProgress() != -1) {
			windowSeek.setProgress(getProgress());

		} else
			windowSeek.setProgress(100);

		globalSeek.setOnSeekBarChangeListener(this);
		windowSeek.setOnSeekBarChangeListener(this);

	}

	/**
	 * Set an action sliders
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		toast = String.valueOf(progress);

		switch (seekBar.getId()) {

		case (R.id.global):
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.screenBrightness = progress / 100.0f;
			getWindow().setAttributes(lp);
			break;

		case (R.id.window):
			scroll.setBackgroundColor(Color.rgb((int) (progress * 2.55),
					(int) (progress * 2.55), (int) (progress * 2.55)));
			break;
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		switch (seekBar.getId()) {
		case R.id.global:
			String s = "Global = " + toast + " %";
			Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
			break;
		case R.id.window:
			s = "Window = " + toast + " %";
			Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
			break;
		}

	}

	/**
	 * Set an action buttons
	 */
	public void buttonListener(View v) {

		switch (v.getId()) {

		case (R.id.saveDB):
			dbHelper.save();
			Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
			break;

		case (R.id.restoreFromDB):
			dbHelper.restore();
			break;

		case (R.id.getFromHttp):
			new GetFromHTTP().execute();
			break;

		}

	}

	/**
	 * Destroy all the processes when the application closes
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		dbHelper.close();
		finish();

	}

	/**
	 * returns the last value from the database to the window brightness
	 */
	public int getProgress() {

		Cursor cursor = sqlDB.query(DBHelper.DATABASE_TABLE, null, null, null,
				null, null, null);

		if (cursor.moveToFirst()) {
			int valueColIndexWindow = cursor
					.getColumnIndex(DBHelper.VALUE_WINDOW);

			do {
				windowProgress = cursor.getInt(valueColIndexWindow);
			} while (cursor.moveToNext());

		} else
			return -1;

		return windowProgress;
	}

	/**
	 * Creates a database
	 * 
	 * @author Zolotar Sergey
	 * 
	 */
	private class DBHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "MyDB.db";
		private static final String DATABASE_TABLE = "brightnesTable";
		private static final String TYPE_GLOBAL = "typeGlobal";
		private static final String TYPE_WINDOW = "typeWindow";
		private static final String VALUE_GLOBAL = "valueGlobal";
		private static final String VALUE_WINDOW = "valueWindow";

		private static final String DATABASE_CREATE = "create table "
				+ DATABASE_TABLE + " (" + TYPE_GLOBAL + " text, " + TYPE_WINDOW
				+ " text, " + VALUE_GLOBAL + " integer, " + VALUE_WINDOW
				+ " integer);";

		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(LOG_TAG, "--- onCreate database ---");

			db.execSQL(DATABASE_CREATE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}

		/**
		 * Stores the data in the database
		 */
		public void save() {

			String globalKey = "global";
			String windowKey = "window";
			int globalValues = globalSeek.getProgress();
			int windowValues = windowSeek.getProgress();

			cv.put(DBHelper.TYPE_GLOBAL, globalKey);
			cv.put(DBHelper.TYPE_WINDOW, windowKey);
			cv.put(DBHelper.VALUE_GLOBAL, globalValues);
			cv.put(DBHelper.VALUE_WINDOW, windowValues);

			sqlDB.insert(DBHelper.DATABASE_TABLE, null, cv);

		}

		/**
		 * Extract a value from the database and set to windowSeek
		 */
		public void restore() {

			Cursor cursor = sqlDB.query(DBHelper.DATABASE_TABLE, null, null,
					null, null, null, null);

			if (cursor.moveToFirst()) {

				int valueColIndexWindow = cursor
						.getColumnIndex(DBHelper.VALUE_WINDOW);

				do {

					windowSeek.setProgress(cursor.getInt(valueColIndexWindow));

				} while (cursor.moveToNext());

			} else {

				// show alert “There is no data in database”.
				alert.setTitle("Внимание");
				alert.setMessage("База данных пуста!");
				alert.setNegativeButton(android.R.string.cancel,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
				alert.show();
			}

			cursor.close();

		}

	}

	private class GetFromHTTP extends AsyncTask<Void, Void, Void> {

		private int global;
		private int window;
		private JSONObject jsonStr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(Void... arg0) {

			ParseJSON sh = new ParseJSON();

			// Get data from the url
			jsonStr = sh.getJson(url);

			if (jsonStr != null) {
				try {

					JSONObject brightness = jsonStr.getJSONObject("brightness");

					global = brightness.getInt("global");

					window = brightness.getInt("window");

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// check if we got data from the url and set the values received in
			// progress
			if (jsonStr == null) {
				alert.setTitle(android.R.string.httpErrorBadUrl);
				alert.setMessage("Проверьте подключение к интернету!");
				alert.setNegativeButton(android.R.string.cancel,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});
				alert.show();

			} else {
				globalSeek.setProgress(global);
				windowSeek.setProgress(window);
			}

		}

	}

}
