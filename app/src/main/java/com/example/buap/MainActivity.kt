package com.example.buap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    private lateinit var imgPerfil: ImageView
    private lateinit var tvSaludo: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvInfo: TextView
    private lateinit var imgWeather: ImageView

    private val apiKey = "9fa57d7468d439f8a44db7dd62759201"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_transport)

        // Mapas y ubicación
        mapView = findViewById(R.id.miniMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapOverlay = findViewById<View>(R.id.mapOverlay)
        mapOverlay.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        imgPerfil = findViewById(R.id.imgPerfil)
        tvSaludo = findViewById(R.id.tvSaludo)
        tvCity = findViewById(R.id.tvCity)
        tvTemp = findViewById(R.id.tvTemp)
        tvInfo = findViewById(R.id.tvInfo)
        imgWeather = findViewById(R.id.imgWeather)

        // 🔹 Cargar datos del usuario desde Firestore
        cargarDatosUsuario()

        imgPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        obtenerClimaActual("Puebla")
    }

    // ================== CARGAR DATOS USUARIO FIRESTORE ==================
    private fun cargarDatosUsuario() {
        val user = auth.currentUser

        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val uid = user.uid

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: user.displayName ?: "Usuario"
                    val fotoPerfil = document.getString("fotoPerfil") ?: user.photoUrl?.toString() ?: ""
                    tvSaludo.text = "¡Hola, $nombre!"

                    if (fotoPerfil.isNotEmpty()) {
                        Glide.with(this)
                            .load(fotoPerfil)
                            .circleCrop()
                            .placeholder(R.drawable.circle_bg_shadow)
                            .into(imgPerfil)
                    } else {
                        imgPerfil.setImageResource(R.drawable.circle_bg_shadow)
                    }

                } else {
                    // Si no hay documento (caso nuevo usuario)
                    tvSaludo.text = "¡Hola!"
                    imgPerfil.setImageResource(R.drawable.circle_bg_shadow)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener usuario: ", e)
                Toast.makeText(this, "Error al cargar usuario", Toast.LENGTH_SHORT).show()
            }
    }

    // ================== CLIMA EN TIEMPO REAL ==================
    private fun obtenerClimaActual(ciudad: String? = null, lat: Double? = null, lon: Double? = null) {
        thread {
            try {
                val url = when {
                    lat != null && lon != null ->
                        "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=es"
                    ciudad != null ->
                        "https://api.openweathermap.org/data/2.5/weather?q=$ciudad&appid=$apiKey&units=metric&lang=es"
                    else -> return@thread
                }

                val response = URL(url).readText()
                val json = JSONObject(response)

                val main = json.getJSONObject("main")
                val temp = main.getDouble("temp").toInt()
                val weatherArray = json.getJSONArray("weather")
                val weather = weatherArray.getJSONObject(0)
                val description = weather.getString("description")
                val icon = weather.getString("icon")
                val cityName = json.getString("name")

                runOnUiThread {
                    tvCity.text = cityName
                    tvTemp.text = "$temp°C"
                    tvInfo.text = description.replaceFirstChar { it.uppercase() }

                    when {
                        icon.contains("01") -> imgWeather.setImageResource(R.drawable.ic_sun)
                        icon.contains("02") -> imgWeather.setImageResource(R.drawable.ic_partly_cloudy)
                        icon.contains("03") || icon.contains("04") -> imgWeather.setImageResource(R.drawable.ic_cloud)
                        icon.contains("09") || icon.contains("10") -> imgWeather.setImageResource(R.drawable.ic_rain)
                        icon.contains("11") -> imgWeather.setImageResource(R.drawable.ic_storm)
                        icon.contains("13") -> imgWeather.setImageResource(R.drawable.ic_snow)
                        else -> imgWeather.setImageResource(R.drawable.ic_weather_default)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tvCity.text = "Error"
                    tvTemp.text = "--°C"
                    tvInfo.text = "No se pudo obtener el clima"
                }
            }
        }
    }

    // ================== MAPA ==================
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

                    val geocoder = android.location.Geocoder(this)
                    val direcciones = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val ciudad = direcciones?.firstOrNull()?.locality ?: "Ubicación desconocida"

                    obtenerClimaActual(ciudad = ciudad, lat = location.latitude, lon = location.longitude)
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
        obtenerClimaActual("Puebla")
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        cargarDatosUsuario() // 🔹 Actualiza la información si se cambió en el perfil
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
