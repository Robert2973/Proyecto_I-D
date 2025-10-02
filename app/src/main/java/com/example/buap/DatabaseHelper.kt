package com.example.buap

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "reportes.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_REPORTES = "reportes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_HORA = "hora"
        private const val COLUMN_DIRECCION = "direccion"
        private const val COLUMN_RIESGO = "riesgo"
        private const val COLUMN_DESCRIPCION = "descripcion"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
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
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REPORTES")
        onCreate(db)
    }

    // Insertar un reporte
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

    // Obtener todos los reportes
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
}
