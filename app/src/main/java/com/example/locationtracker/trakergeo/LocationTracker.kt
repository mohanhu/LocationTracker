package com.example.locationtracker.trakergeo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class LocationTracker (
    private val activity : FragmentActivity
){
    fun launchTrackerToGetLocation(locationTrack:(String)->Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        // Check if the location permissions are granted
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            println("fusedLocationClient .locationAvailability >>> failed 1")
            launch.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }

        val lm = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        println("fusedLocationClient .locationAvailability >>> $isGpsEnable")

        if (!isGpsEnable){
            showGPSDisabledAlert()
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {  location: Location? ->
            println("fusedLocationClient.lastLocation >>>$location")
            if (location != null) {
                // Get the address from the location
                val address = getAddressFromLocation(location.latitude, location.longitude)
                address?.let {
                    println("fusedLocationClient Current address: $it")
                    locationTrack.invoke(it.toString())
                }
            }
        }
    }

    private val launch = (activity).registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ isGranted ->

        println("fusedLocationClient .locationAvailability >>> failed $isGranted")

       if (isGranted.isNotEmpty()){
           MaterialAlertDialogBuilder(activity)
               .setTitle("Location Permission")
               .setMessage("Require permission to track availability status")
               .setPositiveButton("Ok"){_,_->
                   val intent =  Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                       data = Uri.fromParts("package",activity.packageName,null)
                   }
                   activity.startActivity(intent)
               }
               .setNegativeButton("Cancel"){_,_-> }
               .show()
       }

    }

    private fun getAddressFromLocation(lat: Double, lon: Double): Address? {
        val geocoder = Geocoder(activity, Locale.getDefault())
        val addresses: MutableList<Address>? = geocoder.getFromLocation(lat, lon, 1)
        return if (addresses?.isNotEmpty() == true) addresses?.get(0) else null
    }

    private fun showGPSDisabledAlert() {
        val builder = MaterialAlertDialogBuilder(activity)
        builder.setMessage("GPS is disabled. Would you like to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}
