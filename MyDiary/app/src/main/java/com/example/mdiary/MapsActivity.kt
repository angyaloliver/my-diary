package com.example.mdiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mdiary.data.AppDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var markers: MutableList<Marker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        addMarkers()
    }

    private fun addMarkers(){
        Thread {
            var diaryItemList =
                AppDatabase.getInstance(this).diaryItemDAO().getAllDiaryItems()

            runOnUiThread{
                for(item in diaryItemList){
                    if(item.latitude != null && item.longitude != null){
                        val position = LatLng(item.latitude!!, item.longitude!!)
                        var marker: Marker = mMap.addMarker(MarkerOptions().position(position).title(item.title))
                        markers.add(marker)
                        marker.showInfoWindow()
                    }
                }
                moveCamera()
            }
        }.start()
    }

    private fun moveCamera(){
        val builder = LatLngBounds.Builder()
        for (marker in markers) {
            builder.include(marker.position)
        }
        val bounds = builder.build()
        val padding = 300
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.animateCamera(cu);
    }
}
