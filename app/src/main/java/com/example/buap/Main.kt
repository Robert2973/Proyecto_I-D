package com.example.buap

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class Main : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_transport)

        // Header
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)

        // Clima
        val imgWeather = findViewById<ImageView>(R.id.imgWeather)
        val tvCity = findViewById<TextView>(R.id.tvCity)
        val tvTemp = findViewById<TextView>(R.id.tvTemp)

        // Texto informativo
        val tvInfo = findViewById<TextView>(R.id.tvInfo)

        // Categorías de riesgo
        val riskCategories = findViewById<GridLayout>(R.id.riskCategories)

        // Botón Open Map
        val btnOpenMap = findViewById<Button>(R.id.btnOpenMap)

        // Alertas
        val layoutAlertas = findViewById<LinearLayout>(R.id.layoutAlertas)

        // Saludo dinámico
        val userName = intent.getStringExtra("USER_NAME") ?: "Usuario"
        tvSaludo.text = "Hi $userName"

        // Click en perfil
        imgPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        // Ejemplo de clima
        tvCity.text = "Puebla"
        tvTemp.text = "25°C • Soleado"
        imgWeather.setImageResource(R.drawable.ic_sun)

        // Ejemplo de texto informativo
        tvInfo.text = "Recuerda mantenerte informado sobre las condiciones de tu zona y tomar precauciones de seguridad."

        // Click botón abrir mapa
        btnOpenMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        // Ejemplo de alertas dinámicas
        val alertas = listOf(
            "⚠️ Asalto reportado cerca de FCFM",
            "🌊 Encharcamiento en Av. San Claudio"
        )

        for (alerta in alertas) {
            val tv = TextView(this)
            tv.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tv.text = alerta
            tv.setPadding(12, 12, 12, 12)
            tv.setBackgroundResource(R.drawable.rounded_card_light)
            val params = tv.layoutParams as LinearLayout.LayoutParams
            params.topMargin = 8
            tv.layoutParams = params
            layoutAlertas.addView(tv)
        }

        // ---------- NUEVO: Click para categorías de riesgo ----------
        for (i in 0 until riskCategories.childCount) {
            val category = riskCategories.getChildAt(i) as LinearLayout
            category.setOnClickListener {
                val textView = category.getChildAt(1) as TextView
                val categoryName = textView.text.toString()

                val message = when (categoryName) {
                    "Inseguridad" -> """
                        La inseguridad en algunas zonas del campus puede representar un riesgo para los estudiantes y el personal. 
                        Es importante mantenerse alerta, caminar acompañado y evitar áreas poco iluminadas por la noche. 
                        Reporta cualquier incidente sospechoso a las autoridades del campus. 
                        Recuerda seguir las recomendaciones de seguridad y mantener tus pertenencias bajo control en todo momento.
                    """.trimIndent()

                    "Seguridad" -> """
                        Mantener la seguridad dentro y alrededor del campus es responsabilidad de todos. 
                        Conoce los protocolos de emergencia, sigue las instrucciones de los responsables de seguridad y participa en campañas de prevención. 
                        La información sobre puntos seguros y rutas de evacuación puede ayudarte a reaccionar adecuadamente ante cualquier situación de riesgo.
                    """.trimIndent()

                    else -> "Información no disponible para esta categoría."
                }

                // Crear y mostrar AlertDialog
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle(categoryName)
                builder.setMessage(message)
                builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                builder.show()
            }
        }
    }
}
