package com.parse.starter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class GuestbookActivity extends Activity {

	public ImageView start;
	public ImageView stop;

	TextView status;
	TextView dHeading;
	TextView dNumber;
	TextView dCost;

	public String dName;
	public String dCar;
	public String dCNIC;
	public String dContact;
	public String dID;
	public String dCreater;
	public String dOwner;
	public Boolean oldUser;
	public double initRat = 0;
	public int initCount = 0;

	public static final int RESULT_SETTINGS = 10;

	public int dInterval;
	public Handler mHandler;
	public double dFare;
	public String dCarName;

	public LocationManager mLocationManager = null;
	public static final int nLOCATION_INTERVAL = 20 * 1000;
	public static final float LOCATION_DISTANCE = 1;
	public static final int gLOCATION_INTERVAL = 10 * 1000;
	public static final int TIME_DIFF = 40 * 1000;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	// boolean isInternetAvailale = false;

	public Location gLoc;
	public Location bestLoc;

	public double bLat; // latitude
	public double bLong; // longitude
	public double bAcc; // accuracy
	public double bAlt; // altitude
	public double bSpeed; // speed
	public double bBearing;
	String bTime;
	String bProvider;

	public final Context mContext = this;
	boolean firstArr = true;

	LocationListener[] mLocationListeners = new LocationListener[] {
			new LocationListener(LocationManager.GPS_PROVIDER),
			new LocationListener(LocationManager.NETWORK_PROVIDER) };

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());

		initializeLocationManager();
		// checkInternetGPS();
		GPSloc();
		NetworkLoc();
		bestLoc = new Location(LocationManager.GPS_PROVIDER);

		mHandler = new Handler();

		status = (TextView) findViewById(R.id.status);
		dHeading = (TextView) findViewById(R.id.heading);
		dNumber = (TextView) findViewById(R.id.dNumber);
		dCost = (TextView) findViewById(R.id.dCost);

		showUserSettings();
		start = (ImageView) findViewById(R.id.green);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!profile_comp()) {
					Toast.makeText(mContext,
							"Go to Settings and\nCOMPLETE YOUR PROFILE",
							Toast.LENGTH_LONG).show();
				} else {
					if (!oldUser) {
						Toast.makeText(
								mContext,
								"WARNING:\nYour Ratings will remain O untill you\nAccept the Terms and Conditions",
								Toast.LENGTH_LONG).show();
					}

					if (dID == null) {
						uploadLoc();
					} else {
						mHandler.removeCallbacks(mStatusChecker);
						mStatusChecker.run();
					}
					status.setText("Available");
				}
			}
		});

		stop = (ImageView) findViewById(R.id.red);
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!profile_comp()) {
					Toast.makeText(mContext,
							"Go to Settings and\nCOMPLETE YOUR PROFILE",
							Toast.LENGTH_LONG).show();
				} else {
					mHandler.removeCallbacks(mStatusChecker);
					status.setText("UN-Available");
				}
			}
		});

	}

	/**
	 * Override Activity lifecycle method.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Override Activity lifecycle method.
	 * <p>
	 * To add more option menu items in your client, add the item to
	 * menu/activity_main.xml, and provide additional case statements in this
	 * method.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;

		case R.id.menu_about:
			Intent intent_about = new Intent(this, About.class);
			startActivity(intent_about);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * Override Activity lifecycle method.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// handle result codes

		switch (requestCode) {
		case RESULT_SETTINGS:
			showUserSettings();
			break;

		}

		// call super method to ensure unhandled result codes are handled
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	protected void onPause() {

		super.onPause();
	}

	@Override
	protected void onDestroy() {

		if (mLocationManager != null) {
			for (int i = 0; i < mLocationListeners.length; i++) {
				try {
					mLocationManager.removeUpdates(mLocationListeners[i]);
				} catch (Exception ex) {

				}
			}
		}
		mHandler.removeCallbacks(mStatusChecker);
		super.onDestroy();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Exit")
				.setMessage("Are you sure?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								Intent intent = new Intent(Intent.ACTION_MAIN);
								intent.addCategory(Intent.CATEGORY_HOME);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
								finish();
							}
						}).setNegativeButton("No", null).show();
	}

	public void showUserSettings() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		dName = sharedPrefs.getString("dName", null);
		dCar = sharedPrefs.getString("dCar", null);
		dCarName = sharedPrefs.getString("carName", null);
		dCNIC = sharedPrefs.getString("dCNIC", null);
		dContact = sharedPrefs.getString("dContact", null);
		oldUser = sharedPrefs.getBoolean("newUser", false);
		dID = sharedPrefs.getString("ID", null);

		String sInterval = sharedPrefs.getString("dInterval", "12");
		String sFare = sharedPrefs.getString("dFare", "0");
		try {
			dInterval = Integer.parseInt(sInterval);
			dFare = Double.parseDouble(sFare);
			dInterval = dInterval * 1000;
		} catch (NumberFormatException e) {
			Toast.makeText(mContext, "Enter Interval in digits only",
					Toast.LENGTH_SHORT).show();
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			// }

		}

		if (profile_comp()) {
			dHeading.setText("" + dName);
			dNumber.setText("" + dCar);
			dCost.setText("Rs." + dFare + " per Km");
		}

	}

	public boolean profile_comp() {
		if (dName == null || dContact == null || dCNIC == null || dCar == null
				|| dCarName == null) {
			return false;
		} else {
			return true;
		}

	}

	public void uploadLoc() {

		final ParseObject newPost = new ParseObject("Drivers");
		newPost.put("message", dName);
		newPost.put("Latitude", bestLoc.getLatitude());
		newPost.put("Longitude", bestLoc.getLongitude());
		newPost.put("Accuracy", bestLoc.getAccuracy());
		newPost.put("CNIC", dCNIC);
		newPost.put("Contact", dContact);
		newPost.put("CarReg", dCar);
		newPost.put("CarModel", dCarName);
		newPost.put("Fare", dFare);
		newPost.put("Ratings", initRat);
		newPost.put("Count", initCount);

		newPost.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					// saved successfully
					dID = newPost.getObjectId();
					storeID();
					Toast.makeText(getApplicationContext(),
							"driver updated", Toast.LENGTH_SHORT).show();
				} else {
//					Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG)
//							.show();
				}

			}
		});

		mHandler.removeCallbacks(mStatusChecker);
		mStatusChecker.run();

	}

	public void modifyLoc() {

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Drivers");

		// Retrieve the object by id
		query.getInBackground(dID, new GetCallback<ParseObject>() {
			public void done(final ParseObject newPost1, ParseException e) {
				if (e == null) {

					newPost1.put("Latitude", bestLoc.getLatitude());
					newPost1.put("Longitude", bestLoc.getLongitude());
					newPost1.put("Accuracy", bestLoc.getAccuracy());

					newPost1.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null) {
								// saved successfully
								dID = newPost1.getObjectId();
								storeID();
								Toast.makeText(getApplicationContext(),
										"driver updated", Toast.LENGTH_SHORT)
										.show();
							} else {
								Toast.makeText(mContext, e.toString(),
										Toast.LENGTH_LONG).show();
							}

						}
					});
				} else {
					Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}

	protected void storeID() {

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		Editor editor = sharedPrefs.edit();
		editor.putString("ID", dID);
		editor.commit();

	}

	Runnable mStatusChecker = new Runnable() {
		@Override
		public void run() {
			modifyLoc();
			mHandler.postDelayed(mStatusChecker, dInterval);
		}
	};

	public class LocationListener implements android.location.LocationListener {

		Location mLastLocation;

		public LocationListener(String provider) {

			mLastLocation = new Location(provider);

		}

		@Override
		public void onLocationChanged(Location location) {

			if (location.getProvider().equalsIgnoreCase("gps")) {
				gLoc.set(location);
				mLastLocation.set(location);
				firstArr = false;
			}

			if (location.getProvider().equalsIgnoreCase("network")) {

				// nLoc.set(location);
				if (firstArr) {
					mLastLocation.set(location);
				}

				else {

					long gTime = gLoc.getTime();
					Calendar cal = Calendar.getInstance(); // creates calendar
					long nowTime = cal.getTime().getTime();
					long diff = nowTime - gTime;

					if (diff > TIME_DIFF) {
						mLastLocation.set(location);

					} else {
						mLastLocation.set(gLoc);
					}
				}
			}

			bestLoc.set(mLastLocation);

			String msg = "Latitude: " + bestLoc.getLatitude() + "\nLongitude: "
					+ bestLoc.getLongitude() + "\n"
					+ getDate(bestLoc.getTime()) + "\nAccuracy: "
					+ bestLoc.getAccuracy() + "m\nProvider: "
					+ bestLoc.getProvider();

			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
					.show();

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

	public void initializeLocationManager() {

		if (mLocationManager == null) {
			mLocationManager = (LocationManager) getApplicationContext()
					.getSystemService(Context.LOCATION_SERVICE);
		}

	}

	public String getDate(long l) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEEE, MMMM d, yyyy HH:mm:ss");
		String dateString = formatter.format(new Date(l));

		return dateString;

	}

	public void NetworkLoc() {

		// nLoc = new Location(LocationManager.NETWORK_PROVIDER);
		isNetworkEnabled = mLocationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isNetworkEnabled) {
			Toast.makeText(getApplicationContext(),
					"Error: NO Network Provider", Toast.LENGTH_SHORT).show();
		} else {
			try {

				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, nLOCATION_INTERVAL,
						LOCATION_DISTANCE, mLocationListeners[1]);

			} catch (java.lang.SecurityException ex) {
				// Log.e(TAG, "fail to request location update, ignore");
			} catch (IllegalArgumentException ex) {
				// Log.d(TAG, "network provider does not exist, " +
				// ex.getMessage());
			}
		}
	}

	public void GPSloc() {
		gLoc = new Location(LocationManager.GPS_PROVIDER);
		isGPSEnabled = mLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!isGPSEnabled) {
			Toast.makeText(getApplicationContext(),
					"Warning:\nGPS not Enabled", Toast.LENGTH_LONG).show();
		} else {
			try {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, gLOCATION_INTERVAL,
						LOCATION_DISTANCE, mLocationListeners[0]);

			} catch (java.lang.SecurityException ex) {
				// Log.e(TAG, "fail to request location update, ignore");
			} catch (IllegalArgumentException ex) {
				// Log.d(TAG, "gps provider does not exist " + ex.getMessage());
			}
		}
	}

	public void handleEndpointException(IOException e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	}
}
