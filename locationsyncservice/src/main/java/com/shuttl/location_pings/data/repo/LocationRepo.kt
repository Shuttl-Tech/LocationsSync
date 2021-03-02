package com.shuttl.location_pings.data.repo

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.shuttl.location_pings.callbacks.LocationPingServiceCallback
import com.shuttl.location_pings.config.components.LocationRetrofit
import com.shuttl.location_pings.data.dao.GPSLocationsDao
import com.shuttl.location_pings.data.model.entity.GPSLocation
import com.shuttl.location_pings.data.model.request.SendLocationRequestBody
import com.shuttl.location_pings.isInternetConnected
import com.shuttl.location_pings.util.BatchCounter
import kotlinx.coroutines.*

class LocationRepo(private val locationsDao: GPSLocationsDao?) {

    val TAG: String = javaClass.name

    fun addLocation(location: GPSLocation, bufferSize: Int) = GlobalScope.async(Dispatchers.IO) {
        val rowsCount = locationsDao?.getRowsCount() ?: 0
        if (rowsCount >= bufferSize) {
            locationsDao?.deleteOldestLocation()
        }
        locationsDao?.addLocation(location)
    }

    fun clearLocations() = GlobalScope.async(Dispatchers.IO) {
        locationsDao?.clearLocations()
    }

    fun getAllLocations() = GlobalScope.async(Dispatchers.IO) {
        locationsDao?.locations()
    }

    fun getBatchedLocations(entries: Int) = GlobalScope.async(Dispatchers.IO) {
        locationsDao?.getLimitedLocations(entries)
    }

    fun syncLocations(apiKey: String = "",
                      url: String = "",
                      batchSize: Int,
                      context: Context,
                      canReuseLastLocation: Boolean,
                      callback: LocationPingServiceCallback<Any>?) {
        GlobalScope.launch(Dispatchers.IO) {
            var locations = locationsDao?.getLimitedLocations(batchSize)
            var reused: Boolean = false
            if (canReuseLastLocation && locations?.isEmpty() == true) {
                reused = true
                val lastLocation = GPSLocation.getLastLocation(context)
                if (lastLocation != null)
                    locations = listOf(lastLocation)
                else
                    locations = listOf()
            } else {
                reused = false
            }
            if (locations?.isNotEmpty() == true) {
                try {
                    if (TextUtils.isEmpty(url)) {
                        Log.e(TAG, "No Url Found")
                    }
                    if (BatchCounter.getBatchCount() >= 10) {
                        callback?.errorWhileSyncLocations(Exception("Internet is not available"))
                        BatchCounter.reset()
                    }
                    if (!context.isInternetConnected()) {
                        BatchCounter.increment()
                        return@launch
                    }
                    val obj = callback?.beforeSyncLocations(locations, reused)
                    val response = LocationRetrofit.getLocationApi()?.syncLocation(
                        url,
                        apiKey,
                        "application/json",
                        SendLocationRequestBody.create(obj)
                    )
                    if (response?.success == true) {
                        deleteEntries(locations.last().time)
                        callback?.afterSyncLocations(locations)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback?.errorWhileSyncLocations(e)
                }

            }
        }
    }

    fun deleteEntries(time: String) {
        locationsDao?.deleteEntries(time)
    }
}