package com.example.buap

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Usuario(
    val nombre: String,
    val edad: String,
    val direccion: String,
    val telefono: String,
    val imagen: String?
)


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "reportes.db"
        private const val DATABASE_VERSION = 2

        // Tabla reportes
        private const val TABLE_REPORTES = "reportes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_HORA = "hora"
        private const val COLUMN_DIRECCION = "direccion"
        private const val COLUMN_RIESGO = "riesgo"
        private const val COLUMN_DESCRIPCION = "descripcion"

        // Tabla usuario
        private const val TABLE_USUARIO = "usuario"
        private const val COLUMN_USER_NOMBRE = "nombre"
        private const val COLUMN_USER_EDAD = "edad"
        private const val COLUMN_USER_DIRECCION = "direccion"
        private const val COLUMN_USER_TELEFONO = "telefono"
        private const val COLUMN_USER_IMAGEN = "imagen"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear tabla reportes
        val createTableReportes = """
            CREATE TABLE $TABLE_REPORTES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT,
                $COLUMN_FECHA TEXT,
                $COLUMN_HORA TEXT,
                $COLUMN_DIRECCION TEXT,
                $COLUMN_RIESGO TEXT,
                $COLUMN_DESCRIPCION TEXT
            )
        """.trimIndent()

        // Crear tabla usuario
        val createTableUsuario = """
            CREATE TABLE $TABLE_USUARIO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NOMBRE TEXT,
                $COLUMN_USER_EDAD TEXT,
                $COLUMN_USER_DIRECCION TEXT,
                $COLUMN_USER_TELEFONO TEXT,
                $COLUMN_USER_IMAGEN TEXT
            )
        """.trimIndent()

        db?.execSQL(createTableReportes)
        db?.execSQL(createTableUsuario)

        // Insertar usuario por defecto
        val cv = ContentValues().apply {
            put(COLUMN_USER_NOMBRE, "Usuario")
            put(COLUMN_USER_EDAD, "Desconocida")
            put(COLUMN_USER_DIRECCION, "Puebla")
            put(COLUMN_USER_TELEFONO, "123456789")
            put(COLUMN_USER_IMAGEN, "")
        }
        db?.insert(TABLE_USUARIO, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REPORTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIO")
        onCreate(db)
    }

    // ================= REPORTES =================

    fun insertReporte(reporte: Reporte): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, reporte.nombre)
            put(COLUMN_FECHA, reporte.fecha)
            put(COLUMN_HORA, reporte.hora)
            put(COLUMN_DIRECCION, reporte.direccion)
            put(COLUMN_RIESGO, reporte.riesgo)
            put(COLUMN_DESCRIPCION, reporte.descripcion)
        }
        return db.insert(TABLE_REPORTES, null, values)
    }

    fun getAllReportes(): List<Reporte> {
        val reportes = mutableListOf<Reporte>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_REPORTES ORDER BY $COLUMN_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val reporte = Reporte(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)),
                    hora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA)),
                    direccion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECCION)),
                    riesgo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RIESGO)),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION))
                )
                reportes.add(reporte)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return reportes
    }

    // ================= USUARIO =================

    fun getUsuario(): Usuario {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USUARIO LIMIT 1", null)
        val usuario = if (cursor.moveToFirst()) {
            Usuario(
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NOMBRE)),
                edad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EDAD)),
                direccion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_DIRECCION)),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_TELEFONO)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_IMAGEN))
            )
        } else {
            Usuario("Usuario", "Desconocida", "Puebla", "123456789", null)
        }
        cursor.close()
        return usuario
    }

    fun actualizarUsuario(nombre: String, edad: String, direccion: String, telefono: String, imagen: String?) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_USER_NOMBRE, nombre)
            put(COLUMN_USER_EDAD, edad)
            put(COLUMN_USER_DIRECCION, direccion)
            put(COLUMN_USER_TELEFONO, telefono)
            if (imagen != null) put(COLUMN_USER_IMAGEN, imagen)
        }
        db.update(TABLE_USUARIO, cv, null, null)
    }
}
