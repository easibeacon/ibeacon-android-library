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

import java.io.Serializable;

import android.util.Log;

/**
 * Represents an iBeacon device
 * 
 * @author Inaki Vazquez
 *
 */
public class IBeacon implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The UUID of the iBeacon
	 */
	private byte[] _uuid;

	/**
	 * The Major number of the iBeacon
	 */
	private int _major;

	/**
	 * The Minor number of the iBeacon
	 */
	private int _minor;

	/**
	 * <code>true</code> if the iBeacon is an easiBeacon
	 */
	private boolean _easiBeacon = false;

	/**
	 * RSSI at 1 meter reported by the iBeacon
	 */
	private int _powerValue;
	
	/**
	 * A calculated proximity of the iBeacon based on <code>_powerValue</code>
	 */	
	private double _proximity;
	
	/***
	  * RSSI value
	  */
	private int _rssi;

	/**
	 * The MAC address reported by the iBeacon
	 */	
	private String _macAddress;
	
	/**
	 * RSSI at 1 meter in straight line for easiBeacons
	 */	
	public static final int EASIBEACON_POWER_VALUE = -75;
	
	/**
	 * Constructor
	 * 
	 * @param uuid The UUID for the iBeacon
	 * @param major The Major number (1-65535)
	 * @param minor The Minor number (1-65535)
	 * @param proximity Proximity in meters
	 */
	public IBeacon(byte[] uuid, int major, int minor, double proximity){
		_uuid = uuid;
		_major = major;
		_minor = minor;
		
		_proximity = proximity;
	}
	
	/**
	 * Constructor
	 * 
	 * @param uuid The UUID for the iBeacon
	 * @param major The Major number (1-65535)
	 * @param minor The Minor number (1-65535)
	 */
	public IBeacon(byte[] uuid, int major, int minor){
		this(uuid, major, minor, -1);
	}
	
	/**
	 * Empty constructor
	 */
	public IBeacon(){
		this(null, -1, -1, -1);
	}
	
	public String getMacAddress() {
		return _macAddress;
	}

	public void setMacAddress(String _macAddress) {
		this._macAddress = _macAddress;
	}

	public byte[] getUuid() {
		return _uuid;
	}
	
	public String getUuidHexString(){
		String s = "";
		for(int i=0;i<_uuid.length;i++)
			s += String.format("%2X", _uuid[i]);
		return s;	
	}
	
	public String getUuidHexStringDashed(){
		String uuid = getUuidHexString();
		String newUuid = uuid.substring(0,8) + "-" +
				uuid.substring(8, 12) + "-" +
				uuid.substring(12, 16) + "-" +
				uuid.substring(16, 20) + "-" + uuid.substring(20);
		return newUuid;
				
	}
	
	public void setUuid(byte[] _uuid) {
		this._uuid = _uuid;
	}
	
	public int getMajor() {
		return _major;
	}
	
	public void setMajor(int _major) {
		this._major = _major;
	}
	
	public int getMinor() {
		return _minor;
	}
	
	public void setMinor(int _minor) {
		this._minor = _minor;
	}
	
	public boolean isEasiBeacon() {
		return _easiBeacon;
	}

	public void setEasiBeacon(boolean _easiBeacon) {
		this._easiBeacon = _easiBeacon;
	}

	public int getPowerValue() {
		return _powerValue;
	}

	public void setPowerValue(int _pv) {
		this._powerValue = _pv;
	}

	public double getProximity() {
		return _proximity;
	}
	
	public void setProximity(double _proximity) {
		this._proximity = _proximity;
	}
	
	
	public int getRssiValue() {
		return _rssi;
	}

	public void setRssiValue(int _pv) {
		this._rssi = _pv;
	}
	
	

	/**
	 * Two iBeacons are the same if UUID, major and minor are the same.
	 */
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    Log.i(Utils.LOG_TAG, "Comparing");
	    final IBeacon ibeacon = (IBeacon) obj;
		if(getUuidHexString().equals(ibeacon.getUuidHexString())
				&& _major == ibeacon.getMajor()
				&& _minor == ibeacon.getMinor())
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return "UUID:" + this.getUuidHexString() + " M:" + this.getMajor() + " m:" + this.getMinor() + " p:" + this.getProximity();
	}

}
