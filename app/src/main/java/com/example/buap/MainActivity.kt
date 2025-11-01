package com.example.buap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import android.view.View

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_transport)

        mapView = findViewById(R.id.miniMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapOverlay = findViewById<View>(R.id.mapOverlay)
        mapOverlay.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)
        val layoutWeather = findViewById<RelativeLayout>(R.id.layoutWeather)
        val imgWeather = findViewById<ImageView>(R.id.imgWeather)
        val tvCity = findViewById<TextView>(R.id.tvCity)
        val tvTemp = findViewById<TextView>(R.id.tvTemp)
        val tvInfo = findViewById<TextView>(R.id.tvInfo)
        val layoutAlertas = findViewById<LinearLayout>(R.id.layoutAlertas)

        val userName = intent.getStringExtra("USER_NAME") ?: "Usuario"
        tvSaludo.text = "¡Hola, $userName!"

        imgPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        tvCity.text = "Puebla"
        tvTemp.text = "25°C • Soleado"
        imgWeather.setImageResource(R.drawable.ic_sun)
        tvInfo.text =
            "Recuerda mantenerte informado sobre las condiciones de tu zona y tomar precauciones de seguridad."
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isMapToolbarEnabled = false

        mapView.post { mapView.requestLayout() }

        checkLocationPermissionAndShow()
    }

    private fun checkLocationPermissionAndShow() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showUserLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                showUserLocation()
            } else {
                showDefaultLocation()
            }
        }

    private fun showUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f))
                    googleMap?.addMarker(MarkerOptions().position(myLatLng).title("Tu ubicación"))
                } else {
                    showDefaultLocation()
                }
            }
        } catch (e: SecurityException) {
            showDefaultLocation()
        }
    }

    private fun showDefaultLocation() {
        val buap = LatLng(19.0139, -98.2435)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(buap, 15f))
        googleMap?.addMarker(MarkerOptions().position(buap).title("BUAP"))
    }

    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy(); mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory(); mapView.onLowMemory()
    }
}
