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
	 * The version/model of the easiBeacon (if this is an easiBeacon)
	 */
	private String _versionModel;

	/**
	 * TX Power for the iBeacon
	 */
	private int _txPower;
	
	/**
	 * A calculated proximity of the iBeacon based on <code>_powerValue</code>
	 */	
	private int _proximity;

	/**
	 * The MAC address reported by the iBeacon
	 */	
	private String _macAddress;
	
	/**
	 * The battery reported by the iBeacon
	 */	
	private int _battery = -1;

	/**
	 * The broadcast rate reported by the iBeacon
	 */	
	private int _broadcastRate = -1;
	
	/**
	 * The RSSI at 1 meter reported by the iBeacon
	 */		
	private int _powerValue = -1;
	
	/**
	 * Indicates if currently connectable (easiBeacon only)
	 */		
	private boolean _connectable = false;
	
	/**
	 * A user selected name for the iBeacon (to be used)
	 */
	private String _name = null;

	/**
	 * A user selected color representation for the iBeacon (to be used)
	 */
	private int _color;

	/**
	 * Constructor
	 * 
	 * @param uuid The UUID for the iBeacon
	 * @param major The Major number (1-65535)
	 * @param minor The Minor number (1-65535)
	 * @param proximity Proximity in meters
	 */
	public IBeacon(byte[] uuid, int major, int minor, int proximity){
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

	public void setVersionModel(String _versionModel) {
		this._versionModel = _versionModel;
	}

	public int getVersion() {
		int i = Integer.parseInt(""+_versionModel.charAt(1));
		return i;
	}

	public int getModel(){
		int i = Integer.parseInt(""+_versionModel.charAt(0));
		return i;
	}
	
	public byte[] getUuid() {
		return _uuid;
	}
	
	public String getUuidHexString(){
		String s = "";
		for(int i=0;i<_uuid.length;i++)
			s += String.format("%02X", _uuid[i]);
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
	
	public String getName() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public boolean isEasiBeacon() {
		return _easiBeacon;
	}

	public void setEasiBeacon(boolean _easiBeacon) {
		this._easiBeacon = _easiBeacon;
	}

	public int getColor() {
		return _color;
	}

	public void setColor(int _color) {
		this._color = _color;
	}

	public int getTxPower() {
		return _txPower;
	}

	public void setTxPower(int _txPower) {
		this._txPower = _txPower;
	}

	public int getProximity() {
		return _proximity;
	}
	
	public void setProximity(int _proximity) {
		this._proximity = _proximity;
	}
	

	public int getBattery() {
		return _battery;
	}

	public void setBattery(int _battery) {
		this._battery = _battery;
	}

	public int getBroadcastRate() {
		return _broadcastRate;
	}

	public void setBroadcastRate(int _broadcastRate) {
		this._broadcastRate = _broadcastRate;
	}

	public int getPowerValue() {
		return _powerValue;
	}

	public void setPowerValue(int _powerValue) {
		this._powerValue = _powerValue;
	}
	
	public boolean isConnectable(){
		return _connectable;
	}
	
	public void setConnectable(boolean b){
		_connectable = b;
	}

	/**
	 * Two iBeacons are the same if UUID, major, minor and MAC addresses are the same.
	 * The MAC address comparison is to avoid detecting as the same two different iBeacons with factory settings.
	 */
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	   // Log.i(Utils.LOG_TAG, "Comparing");
	    final IBeacon ibeacon = (IBeacon) obj;
		if(this.isSameRegionAs(ibeacon) && _macAddress.equals(ibeacon.getMacAddress()))
			return true;
		return false;
	}
	
	/**
	 * Returns true if the iBeacon to compare represents the same region (same UUID, major and minor).
	 */
	public boolean isSameRegionAs(IBeacon ibeacon) {
	    if (ibeacon == null) {
	        return false;
	    }
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
