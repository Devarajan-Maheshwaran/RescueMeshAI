package org.briarproject.briar.android.rescue.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.rescuemesh.api.emergency.EmergencyLocation;

import javax.annotation.Nullable;

import androidx.core.content.ContextCompat;

/**
 * Captures one consented current location for a single SOS. It never starts
 * background tracking and callers must check permission before invoking it.
 */
public class OneTimeLocationCapture {

	private final Context context;
	private final LocationManager locationManager;

	public OneTimeLocationCapture(Context context) {
		this.context = context.getApplicationContext();
		locationManager = (LocationManager) this.context.getSystemService(
				Context.LOCATION_SERVICE);
	}

	public boolean hasLocationPermission() {
		return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(context,
					Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED;
	}

	@SuppressLint("MissingPermission")
	public void capture(Callback callback) {
		if (!hasLocationPermission()) {
			callback.onFailure();
			return;
		}
		String provider = selectProvider();
		if (provider == null) {
			callback.onFailure();
			return;
		}
		Location last = locationManager.getLastKnownLocation(provider);
		if (isFresh(last)) {
			callback.onLocation(toEmergencyLocation(last));
			return;
		}
		locationManager.requestSingleUpdate(provider, new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				callback.onLocation(toEmergencyLocation(location));
			}
			@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
			@Override public void onProviderEnabled(String provider) {}
			@Override public void onProviderDisabled(String provider) { callback.onFailure(); }
		}, null);
	}

	@Nullable
	private String selectProvider() {
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			return LocationManager.GPS_PROVIDER;
		}
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			return LocationManager.NETWORK_PROVIDER;
		}
		return null;
	}

	private boolean isFresh(@Nullable Location location) {
		return location != null && System.currentTimeMillis() - location.getTime()
				<= 2L * 60 * 1000;
	}

	private EmergencyLocation toEmergencyLocation(Location location) {
		float accuracy = location.hasAccuracy() ? location.getAccuracy() : Float.MAX_VALUE;
		return new EmergencyLocation(location.getLatitude(), location.getLongitude(),
				accuracy, location.getTime());
	}

	public interface Callback {
		void onLocation(EmergencyLocation location);
		void onFailure();
	}
}
