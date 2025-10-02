package com.example.buap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botón para abrir ReporteActivity
        val btnEncuesta = findViewById<Button>(R.id.btnEncuesta)
        btnEncuesta.setOnClickListener {
            val intent = Intent(this, ReporteActivity::class.java)
            startActivity(intent)
        }

        // Botón regresar
        val imageViewBack = findViewById<ImageView>(R.id.imageViewBack)
        imageViewBack.setOnClickListener { finish() }

        // Botón para cambiar tipo de mapa
        val btnTipoMapa = findViewById<Button>(R.id.btnTipoMapa)
        btnTipoMapa.setOnClickListener {
            mostrarOpcionesMapa()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Ejemplo: agregar un marcador en BUAP Puebla
        val buap = LatLng(19.0413, -98.2062)
        mMap.addMarker(MarkerOptions().position(buap).title("BUAP Puebla"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buap, 15f))

        // Habilitar opciones visuales
        mMap.isTrafficEnabled = true       // Mostrar tráfico
        mMap.isBuildingsEnabled = true     // Edificios 3D
        mMap.isIndoorEnabled = true        // Mapas interiores
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

    private fun mostrarOpcionesMapa() {
        val tiposMapa = arrayOf("Normal", "Satélite", "Híbrido", "Terreno")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona tipo de mapa")
        builder.setItems(tiposMapa) { _, which ->
            when (which) {
                0 -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                1 -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                2 -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                3 -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }
        builder.show()
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
}
