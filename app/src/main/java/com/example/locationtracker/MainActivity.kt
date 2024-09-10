package com.example.locationtracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.locationtracker.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint("ServiceCast")
class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    lateinit var lm : LocationManager
    lateinit var loc : Location

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED ){

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),111
            )
        }

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

        println("isProviderEnabled >>>0> $isGpsEnabled")
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L,0f){location->
            println("isProviderEnabled >>>2> $location")
            reverseCode(location)
        }
        if (isGpsEnabled){
            println("isProviderEnabled >>>0.2> ${lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)}")
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                loc = it
                println("isProviderEnabled >>>1> $loc")
                reverseCode(loc)
            }
        }
        else{
            showGPSDisabledAlert()
        }
    }
    private fun reverseCode(location: Location) {
        try {
            lifecycleScope.launch(Dispatchers.IO){
                val gc = Geocoder(this@MainActivity, Locale.getDefault())
                println("isProviderEnabled >>> start >> $location")

                val address = gc.getFromLocation(location.latitude,location.longitude,3)
                println("isProviderEnabled >>>4> $address")
                withContext(Dispatchers.Main){
                    delay(1000)
                    binding.tvLocation.text = address?.get(0)?.getAddressLine(0) +"\n" +address?.get(0)?.locality
                }
            }
        }
        catch (e:Exception){
            println("isProviderEnabled >>>5> $e")
        }
    }

    private fun showGPSDisabledAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("GPS is disabled. Would you like to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}