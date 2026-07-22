package org.briarproject.briar.android.rescue.status;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Phase-1 device-radio snapshot.
 *
 * <p>This reports only local radio readiness. It does not infer peer count,
 * route availability, or message delivery. Those indicators must come from
 * verified Briar plugin/synchronisation state before being displayed.</p>
 */
public class RescueDeviceStatus {

	private final boolean bluetoothAvailable;
	private final boolean bluetoothEnabled;
	private final boolean wifiEnabled;

	private RescueDeviceStatus(boolean bluetoothAvailable,
			boolean bluetoothEnabled, boolean wifiEnabled) {
		this.bluetoothAvailable = bluetoothAvailable;
		this.bluetoothEnabled = bluetoothEnabled;
		this.wifiEnabled = wifiEnabled;
	}

	public static RescueDeviceStatus read(Context context) {
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		WifiManager wifi = (WifiManager) context.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		return new RescueDeviceStatus(bluetooth != null,
				bluetooth != null && bluetooth.isEnabled(),
				wifi != null && wifi.isWifiEnabled());
	}

	public boolean isBluetoothAvailable() {
		return bluetoothAvailable;
	}

	public boolean isBluetoothEnabled() {
		return bluetoothEnabled;
	}

	public boolean isWifiEnabled() {
		return wifiEnabled;
	}
}
