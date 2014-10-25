/*
 * Copyright 2014 Easi Technologies and Consulting Services, S.L.
 *  
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.easibeacon.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Basic iBeacon discovery protocol
 * 
 * @author inakivazquez
 *
 */
public class IBeaconProtocol {
	/**
	 * The BLE advertisement prefix length
	 */
	public static final int ADV_PREFIX_LENGTH = 9;
	
	/**
	 * UUID length
	 */	
	public static final int ADV_UUID_LENGTH = 16;
	
	/**
	 * Scanning period for iBeacon discovery in miliseconds
	 */		
	public static int SCANNING_PERIOD = 10000;

	/**
	 * The prefix for identifying easiBeacons
	 */	
	public static final String EASIBEACON_IDPREFIX = "easiBeacon_";
	
	/**
	 * State of the search process to notify the listener: started
	 */	
	public static final int SEARCH_STARTED = 1;

	/**
	 * State of the search process to notify the listener: no iBeacons found
	 */	
	public static final int SEARCH_END_EMPTY = 2;
	
	/**
	 * State of the search process to notify the listener: at least one iBeacon found
	 */		
	public static final int SEARCH_END_SUCCESS = 3;
	
	/**
	 * Singleton attribute for the instance of this class
	 */	
	private static IBeaconProtocol _ibp = null;
	
	/**
	 * Reference to the BluetoothAdapter
	 */	
	private BluetoothAdapter _bluetoothAdapter;

	/**
	 * Reference to a listener to send iBeacon events
	 */	
	private IBeaconListener _listener;

	/**
	 * UUID to filter advertisements, default value
	 */	
	private byte[] _uuid = null;

	/**
	 * <code>true</code> if currently in a scanning process
	 */	
	private boolean _scanning;

	/**
	 * Reference to the previous nearest iBeacon, to identify if region has changed
	 */	
	private IBeacon _previousNearestIBeacon = null;

	/**
	 * Ordered array of iBeacons found
	 */	
	private ArrayList<IBeacon> _arrOrderedIBeacons = new ArrayList<IBeacon>();
	
	// For controlling and resetting the timeout
	private Handler _timeoutHandler;
	
	/**
	 * Private empty constructor
	 */
	private IBeaconProtocol(){};
	
	/**
	 * Obtains the reference to the singleton <code>IBeaconProtocol</code>
	 * 
	 * @param c Context of the Android app
	 * @return The singleton instance
	 */
	public static IBeaconProtocol getInstance(Context c){
		if(_ibp == null){
			_ibp = new IBeaconProtocol();
		}
		return _ibp;
	}
	
	/**
	 * Returns the listener configured previously if any
	 * 
	 * @return the listener
	 */
	public IBeaconListener getListener() {
		return _listener;
	}

	/**
	 * Configures the listener that will receive events involving iBeacon regions
	 * @param l the listener to configure
	 */
	public void setListener(IBeaconListener l) {
		this._listener = l;
	}
	
	/**
	 * Obtains the list of  discovered iBeacons ordered by estimated proximity
	 * 
	 * @return the {@link java.util.ArrayList} of iBeacons
	 */
	public ArrayList<IBeacon> getIBeaconsByProximity(){
		return _arrOrderedIBeacons;
	}
	
	/**
	 * Removes information about previous nearest beacon in order to "forget" the current region and detect it again.
	 * Used mainly for administration purposes.
	 */
	public void reset(){
		_previousNearestIBeacon = null;
	}
	
	/**
	 * Informs if the system is currently scanning for iBeacons
	 * 
	 * @return <code>true</code> if currently scanning iBeacons. <code>false</code> otherwise.
	 */
	public boolean isScanning(){
		return _scanning;
	}
	
	/**
	 * Configures the Bluetooth adapter and stores a reference to it
	 * 
	 * @param c the current context
	 * @return <code>true</code> if initialization was successful. <code>false</code> otherwise.
	 */
	public static boolean configureBluetoothAdapter(Context c){
		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager =
		        (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
		_ibp._bluetoothAdapter = bluetoothManager.getAdapter();
		if (_ibp._bluetoothAdapter == null || !_ibp._bluetoothAdapter.isEnabled()) {
		    return false;
		}		
		return true;
	}
	
	/**
	 * Callback for processing BLE events, to identify iBeacons during the scanning process.
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
	    @Override
	    public void onLeScan(final BluetoothDevice device, int rssi,
	            byte[] scanRecord) {
	    	
			Log.i(Utils.LOG_TAG,"BLE packet received");
	    	
	    	IBeacon newBeacon = parseAdvertisementData(scanRecord);
	    	if(newBeacon == null)
	    		return;
	    	
	    	newBeacon.setMacAddress(device.getAddress());

	    	// If already discovered, then just refresh the RSSI of the existing instance and return
	    	if(_arrOrderedIBeacons.contains(newBeacon)){
	    		int newDistance = (int)calculateDistance(newBeacon.getPowerValue(), rssi);
	    		IBeacon previousIBeaconInfo = findIfExists(newBeacon);
	    		int oldDistance = previousIBeaconInfo.getProximity();
	    		if(newDistance < oldDistance){
	    			Log.i(Utils.LOG_TAG,"Updating distance");
	    			previousIBeaconInfo.setProximity(newDistance);
		    		// Sort again
			    	Collections.sort(_arrOrderedIBeacons, new IBeaconProximityComparator());
	    		}
	    		return;
	    	}
	    	
    		newBeacon.setEasiBeacon(false);
	    	if(device.getName() != null){
		    	if(device.getName().startsWith(EASIBEACON_IDPREFIX)){
		    		newBeacon.setEasiBeacon(true);
		    		String version = device.getName().substring(EASIBEACON_IDPREFIX.length());
		    		newBeacon.setVersionModel(version);
		    		if(newBeacon.getVersion() == 1){
		    			// Version 1 is always connectable
		    			newBeacon.setConnectable(true);
		    		}else if(newBeacon.getVersion() == 2){
		    			newBeacon.setConnectable(getConnectable(scanRecord));
		    			if(!newBeacon.isConnectable())
		    				newBeacon.setEasiBeacon(false); //If not connectable we will report it as unknown 
		    		}		    		
		    	}
	    	}
	    	// Review this
	    	Log.i(Utils.LOG_TAG,device.getName() + " " + device.getAddress() + " " + newBeacon.getPowerValue() + " " + rssi + " Connectable: " + newBeacon.isConnectable());
	    	newBeacon.setProximity((int)calculateDistance(newBeacon.getPowerValue(), rssi));
	    	
	    	if(!_arrOrderedIBeacons.contains(newBeacon)){
		    	_arrOrderedIBeacons.add(newBeacon);
		    	Collections.sort(_arrOrderedIBeacons, new IBeaconProximityComparator());
		    	_listener.beaconFound(newBeacon);
		    	
		    	// Every time a new beacon is found, reset the timeout
		    	_timeoutHandler.removeCallbacks(timeoutTask);
				_timeoutHandler.postDelayed(timeoutTask, IBeaconProtocol.SCANNING_PERIOD);

	    	} 	
	   }
	       
	};
	
	/**
	 * Finds an ibeacon in the list of previously discovered ibeacons
	 * 
	 * @param b ibeacon to find
	 * @return <code>null</code> if the ibeacon is not found, the existing copy of the ibeacon otherwise.
	 */
	private IBeacon findIfExists(IBeacon b){
		for(int i=0;i<_arrOrderedIBeacons.size();i++){
			IBeacon existing = (IBeacon)_arrOrderedIBeacons.get(i);
			if(existing.equals(b))
				return existing;
		}
		return null;
	}
	
	/**
	 * Notifies the listener about possible region-based events
	 */
	private void notifyListener(){
		IBeacon newNearestBeacon = null;
		if(_arrOrderedIBeacons.size() > 0)
			newNearestBeacon = _arrOrderedIBeacons.get(0);
		
    	// Case 1: enter iBeacon region from nowhere
    	if(_previousNearestIBeacon == null && newNearestBeacon != null){
    		_listener.enterRegion(newNearestBeacon);
    		_previousNearestIBeacon = newNearestBeacon;
    	}
    	// Case 2: keep in the same iBeacon region, update proximity
    	else if(_previousNearestIBeacon != null && newNearestBeacon != null && _previousNearestIBeacon.equals(newNearestBeacon)){
    		_previousNearestIBeacon = newNearestBeacon;
    	}
    	// Case 3: enter a different iBeacon region (roaming)
    	else if(_previousNearestIBeacon != null && newNearestBeacon != null && !_previousNearestIBeacon.equals(newNearestBeacon)){
    		_listener.exitRegion(_previousNearestIBeacon);
    		_listener.enterRegion(newNearestBeacon);
    		_previousNearestIBeacon = newNearestBeacon;
    	}
    	// Case 4: leave iBeacon region
    	else if(_previousNearestIBeacon != null && newNearestBeacon == null){
    		_listener.exitRegion(_previousNearestIBeacon);
    		_previousNearestIBeacon = null;
    	}	    	
	}
	
	/**
	 * Obtains a reference to the {@link android.bluetooth.BluetoothDevice} based on the MAC
	 * @param mac the Bluetooth MAC address of the device
	 * @return the device with that MAC address
	 */
	public BluetoothDevice getDevice(String mac){
		return _bluetoothAdapter.getRemoteDevice(mac);
	}
	
	/**
	 * Sets a UUID to filter ibeacons based on that UUID
	 * @param uuid the UUID to filter ibeacon advertisements
	 */
	public void setScanUUID(byte[] uuid){
		_uuid = uuid;
	}
	
	private Runnable timeoutTask = new Runnable() {
		@Override
		public void run() {
			_scanning = false;
			_bluetoothAdapter.stopLeScan(mLeScanCallback);
			if(_arrOrderedIBeacons.size() == 0)
				_listener.searchState(SEARCH_END_EMPTY);
			else
				_listener.searchState(SEARCH_END_SUCCESS);
			notifyListener();
		}
	};
	
	/**
	 * Starts or stops the scanning process looking for iBeacons
	 * @param enable <code>true</code> to start scanning, <code>false</code> to stop the scanning process
	 */
	public void scanIBeacons(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			_timeoutHandler = new Handler();
			_timeoutHandler.postDelayed(timeoutTask, IBeaconProtocol.SCANNING_PERIOD);

			_scanning = true;
			_arrOrderedIBeacons.clear();
			_bluetoothAdapter.startLeScan(mLeScanCallback);
			_listener.searchState(SEARCH_STARTED);
		} else {
			_scanning = false;
			_bluetoothAdapter.stopLeScan(mLeScanCallback);
			_listener.searchState(SEARCH_END_SUCCESS);
		}
		// Cannot obtain error status=133 this way
		Log.i(Utils.LOG_TAG,"The status:" + _bluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT));
	}
	
	/**
	 * Direct call to start scanning for iBeacons
	 */
	public void startScan(){
		if(_bluetoothAdapter != null)
			scanIBeacons(true);
	}
	
	/**
	 * Direct call to stop scanning for iBeacons
	 */
	public void stopScan(){
		if(_bluetoothAdapter != null)
			scanIBeacons(false);
	}

	/**
	 * Obtains BLE advertisement data and checks if it is an iBeacon
	 * @param data the advertisement data
	 * @return the IBeacon found or null if not an iBeacon
	 */
	private IBeacon parseAdvertisementData(byte[] data){
		Log.i(Utils.LOG_TAG,Arrays.toString(data));
		// First, check the prefix for our beacons
		if(data[0]==0x02 && data[1]==0x01 && data[4]==(byte)0xFF && data[7]==0x02){ // iBeacon candidate
			byte[] uuid = Arrays.copyOfRange(data, ADV_PREFIX_LENGTH, ADV_PREFIX_LENGTH + ADV_UUID_LENGTH);
			// Now filter beacons based on UUID if any
			if(_uuid == null || Arrays.equals(_uuid, uuid)){
				int offset = ADV_PREFIX_LENGTH + ADV_UUID_LENGTH;
				IBeacon ibeacon = new IBeacon();
				ibeacon.setUuid(uuid);
				int major = ((data[offset] << 8) & 0x0000ff00) | (data[offset+1] & 0x000000ff);
				ibeacon.setMajor(major);
				int minor = ((data[offset+2] << 8) & 0x0000ff00) | (data[offset+3] & 0x000000ff);
				ibeacon.setMinor(minor);
				ibeacon.setPowerValue(data[offset+4]);
				return ibeacon;
			}
		}
		return null;
	}
	
	/**
	 * Returns the connectable state of an iBeacon (easiBeacon only)
	 * 
	 * @param data advertisement data
	 * @return <code>true</code> if the easiBeacon is in connectable mode, <code>false</code> otherwise.
	 */
	private boolean getConnectable(byte[] data){
		// If byte 31 of the advertisement is 0 then is not connectable
		if(data[31] != 0) return true;
		return false;
	}
	
	/**
	 * Roughly estimates the distance to the iBeacon
	 * Calculation obtained from http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
	 *  
	 * @param txPower RSSI of the iBeacon at 1 meter
	 * @param rssi measured RSSI by the user device
	 * @return
	 */
	private double calculateDistance(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi*1.0/txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio,10);
		}
		else {
			double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;    
			return accuracy;
		}
	} 
	
	/**
	 * Implements a proximity comparator of distance for iBeacons
	 * @author inakivazquez
	 *
	 */
	private class IBeaconProximityComparator implements Comparator<IBeacon> {
	    @Override
	    public int compare(IBeacon b1, IBeacon b2) {
	        return b1.getProximity()-b2.getProximity();
	    }
	}
	

}
