package com.example.buap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var direccionSeleccionada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // --- Botón Reporte / Encuesta ---
        findViewById<Button>(R.id.btnEncuesta).setOnClickListener {
            val intent = Intent(this, ReporteActivity::class.java)
            intent.putExtra("direccionSeleccionada", direccionSeleccionada)
            startActivity(intent)
        }

        // --- Botón Regresar ---
        findViewById<ImageView>(R.id.imageViewBack).setOnClickListener { finish() }

        // --- Tipos de mapa ---
        val btnNormal = findViewById<Button>(R.id.btnNormal)
        val btnSatellite = findViewById<Button>(R.id.btnSatellite)
        val btnHybrid = findViewById<Button>(R.id.btnHybrid)
        val btnTerrain = findViewById<Button>(R.id.btnTerrain)

        // --- Capas ---
        val btnTraffic = findViewById<Button>(R.id.btnTraffic)
        val btnRelief = findViewById<Button>(R.id.btnRelief)
        val btnDefault = findViewById<Button>(R.id.btnDefault)

        // Listeners para tipos de mapa
        btnNormal.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_NORMAL }
        btnSatellite.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE }
        btnHybrid.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_HYBRID }
        btnTerrain.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN }

        // Listeners para capas
        btnTraffic.setOnClickListener {
            mMap.isTrafficEnabled = !mMap.isTrafficEnabled
            actualizarBotonActivo(btnTraffic, mMap.isTrafficEnabled)
        }

        btnRelief.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN }

        btnDefault.setOnClickListener {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.isTrafficEnabled = false
            mMap.isBuildingsEnabled = true
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isTrafficEnabled = true
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        enableMyLocation()

        // --- Evento de clic en el mapa ---
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            val geocoder = Geocoder(this, Locale.getDefault())
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addressList.isNullOrEmpty()) {
                direccionSeleccionada = addressList[0].getAddressLine(0)
                Toast.makeText(this, "Dirección seleccionada:\n$direccionSeleccionada", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            // Obtener ubicación actual y mover cámara
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarBotonActivo(button: Button, activo: Boolean) {
        if (activo) {
            button.setBackgroundResource(R.drawable.rounded_button_blue)
        } else {
            button.setBackgroundResource(R.drawable.rounded_card_glass)
        }
    }
}
