package com.shuttl.location_pings.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shuttl.location_pings.data.model.entity.GPSLocation

@Dao
interface GPSLocationsDao {

    @Insert
    fun addLocation(location: GPSLocation)

    @Query("SELECT * FROM gps_locations")
    fun locations(): List<GPSLocation>

    @Query("SELECT * FROM gps_locations LIMIT :entries")
    fun getLimitedLocations(entries: Int): List<GPSLocation>

    @Query("SELECT * FROM gps_locations where time < :lastTime")
    fun getBatchLocations(lastTime: String): List<GPSLocation>

    @Query("DELETE FROM gps_locations")
    fun clearLocations()

    @Query("DELETE FROM gps_locations WHERE time = (SELECT MIN(time) from gps_locations)")
    fun deleteOldestLocation()

    @Query("SELECT COUNT(*) FROM gps_locations")
    fun getRowsCount(): Int

    @Query("DELETE FROM gps_locations WHERE time <= :lastTime ")
    fun deleteEntries(lastTime: String)

}