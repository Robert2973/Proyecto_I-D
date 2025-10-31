package com.example.buap

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ReporteActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etRiesgo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnEnviar: Button
    private lateinit var ivBack: ImageView

    // Elementos de tomar foto
    private lateinit var btnTomarFoto: Button
    private lateinit var imgFoto: ImageView
    private lateinit var tvPlaceholder: TextView

    private lateinit var dbHelper: DatabaseHelper
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_reporte)

        // Inicializar base de datos
        dbHelper = DatabaseHelper(this)

        // Referencias
        etNombre = findViewById(R.id.etNombre)
        etFecha = findViewById(R.id.etFecha)
        etHora = findViewById(R.id.etHora)
        etDireccion = findViewById(R.id.etDireccion)
        etRiesgo = findViewById(R.id.etRiesgo)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnEnviar = findViewById(R.id.btnEnviar)
        ivBack = findViewById(R.id.imageViewBack)

        // Referencias a tomar foto
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        imgFoto = findViewById(R.id.imgFoto)
        tvPlaceholder = findViewById(R.id.tvPlaceholder)

        // Botón regresar
        ivBack.setOnClickListener { finish() }

        // DatePicker para fecha
        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                val mes = m + 1
                etFecha.setText(String.format("%02d/%02d/%04d", d, mes, y))
            }, year, month, day)

            datePicker.show()
        }

        // TimePicker para hora
        etHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, h, m ->
                etHora.setText(String.format("%02d:%02d", h, m))
            }, hour, minute, true)

            timePicker.show()
        }

        // Botón tomar foto
        btnTomarFoto.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        // Botón enviar
        btnEnviar.setOnClickListener {
            enviarReporte()
        }
    }

    // Pedir permiso de cámara
    private fun checkCameraPermissionAndOpen() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        } else {
            openCamera()
        }
    }

    // Abrir Camara
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error al crear el archivo", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(intent)
            }
        }
    }

    // Crear un archivo temporal para la foto
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Resultado de la camara
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath ?: return@registerForActivityResult)
            val uri = Uri.fromFile(file)
            imgFoto.setImageURI(uri)
            tvPlaceholder.visibility = TextView.GONE
        }
    }

    // Enviar reporte
    private fun enviarReporte() {
        val nombre = etNombre.text.toString()
        val fecha = etFecha.text.toString()
        val hora = etHora.text.toString()
        val direccion = etDireccion.text.toString()
        val riesgo = etRiesgo.text.toString()
        val descripcion = etDescripcion.text.toString()
        val fotoPath = currentPhotoPath


        if (nombre.isEmpty() || fecha.isEmpty() || hora.isEmpty() ||
            direccion.isEmpty() || riesgo.isEmpty() || descripcion.isEmpty() || fotoPath.isNullOrEmpty()
        ) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val reporte = Reporte(
            nombre = nombre,
            fecha = fecha,
            hora = hora,
            direccion = direccion,
            riesgo = riesgo,
            descripcion = descripcion,
            foto = fotoPath
        )

        val id = dbHelper.insertReporte(reporte)
        if (id > 0) {
            Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
            limpiarCampos()
        } else {
            Toast.makeText(this, "Error al enviar reporte", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarCampos() {
        etNombre.text.clear()
        etFecha.text.clear()
        etHora.text.clear()
        etDireccion.text.clear()
        etRiesgo.text.clear()
        etDescripcion.text.clear()
        imgFoto.setImageDrawable(null)
        tvPlaceholder.visibility = TextView.VISIBLE
        currentPhotoPath = null
    }
}
