package com.librarysdk.sdk.workers

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.work.Worker
import androidx.work.WorkerParameters

class LocationFusedWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), LocationListener {

    private lateinit var locationManager: LocationManager

    override fun doWork(): Result {
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Lokasyon güncellemelerini başlat
        startLocationUpdates()

        // İşin tamamlandığını belirtin (burada sonsuz bir döngü olmayacak şekilde planlanmalı)
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // Lokasyon güncellemeleri başlat
        if (androidx.core.app.ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            androidx.core.app.ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 10000, 10f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        // Lokasyon değiştiğinde yapılacak işler
        val latitude = location.latitude
        val longitude = location.longitude

        // Burada lokasyon bilgilerini kullanarak bir işlem yapabilirsiniz
        // Örneğin, veritabanına kaydetme ya da bir API çağrısı
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStopped() {
        super.onStopped()
        locationManager.removeUpdates(this)
    }
}