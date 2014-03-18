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

/**
 * Listener for iBeacon protocol events
 * 
 * @author inakivazquez
 *
 */
public interface IBeaconListener {
	
	/**
	 * Called when entering a new region
	 * @param ibeacon Information about the iBeacon whose region has been entered
	 */
	public void enterRegion(IBeacon ibeacon);
	
	/**
	 * Called when exiting a region
	 * @param ibeacon Information about the iBeacon whose region has been left
	 */
	public void exitRegion(IBeacon ibeacon);

	/**
	 * Called when a new iBeacon has been found during scanning
	 * @param ibeacon The iBeacon found
	 */
	public void beaconFound(IBeacon ibeacon);

	/**
	 * Called upon any change in the scanning state
	 * @param state The state, either <code>SEARCH_STARTED</code>, <code>SEARCH_END_EMPTY</code> or <code>SEARCH_END_SUCCESS</code>
	 */
	public void searchState(int state);

	/**
	 * Called to notify an error in the iBeacon protocol, probably due to the BluetoothAdapter
	 * @param status The error status code
	 */
	public void operationError(int status);	
}
