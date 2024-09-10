package com.example.locationtracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.locationtracker.databinding.ActivityLocationBinding
import com.example.locationtracker.trakergeo.LocationTracker

class LocationActivity : AppCompatActivity() {

    private val binding : ActivityLocationBinding by lazy { ActivityLocationBinding.inflate(layoutInflater) }


    private val locationTracker by lazy { LocationTracker(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        locationTracker.launchTrackerToGetLocation{
            binding.tvLocation.text = it
        }
    }
}