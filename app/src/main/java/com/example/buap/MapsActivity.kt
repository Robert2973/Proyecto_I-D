package com.example.buap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps) // Mantener tu layout actual

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botón Reporte / Encuesta
        findViewById<Button>(R.id.btnEncuesta).setOnClickListener {
            val intent = Intent(this, ReporteActivity::class.java)
            startActivity(intent)
        }

        // Botón Regresar
        findViewById<ImageView>(R.id.imageViewBack).setOnClickListener { finish() }

        // --- Tipos de mapa ---
        val btnNormal = findViewById<Button>(R.id.btnNormal)
        val btnSatellite = findViewById<Button>(R.id.btnSatellite)
        val btnHybrid = findViewById<Button>(R.id.btnHybrid)
        val btnTerrain = findViewById<Button>(R.id.btnTerrain)

        // --- Capas que sí funcionan ---
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

        // Marcador ejemplo BUAP Puebla
        val buap = LatLng(19.0413, -98.2062)
        mMap.addMarker(MarkerOptions().position(buap).title("BUAP Puebla"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buap, 15f))

        // Configuración inicial
        mMap.isTrafficEnabled = true
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
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
