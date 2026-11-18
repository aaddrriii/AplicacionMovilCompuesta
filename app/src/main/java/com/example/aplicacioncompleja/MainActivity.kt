package com.example.aplicacioncompleja

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var txtNombre: TextView
    private lateinit var txtCalificacion: TextView
    private lateinit var txtAprobado: TextView

    private var nombre = ""
    private var calificacion = 0.0
    private var aprobado = false
    private val executor = Executors.newSingleThreadExecutor()

    private val apiUrl = "https://6afdc35e2a9c141d36b90046.mockapi.io/api/adrian"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtNombre = findViewById(R.id.txtNombre)
        txtCalificacion = findViewById(R.id.txtCalificacion)
        txtAprobado = findViewById(R.id.txtAprobado)

        findViewById<Button>(R.id.btnObtener).setOnClickListener { obtenerDatosAPI() }
        findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardarDatos() }
        findViewById<Button>(R.id.btnRecuperar).setOnClickListener { recuperarDatos() }
    }


    private fun obtenerDatosAPI() {
        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                val code = connection.responseCode
                if (code !in 200..299) throw Exception("HTTP $code")
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = if (response.trim().startsWith("[")) {
                    val array = JSONArray(response)
                    if (array.length() == 0) throw Exception("La API devolvió una lista vacía")
                    array.getJSONObject(0)
                } else JSONObject(response)

                nombre = jsonObject.getString("Nombre")
                calificacion = jsonObject.getDouble("Calificación")
                aprobado = jsonObject.getBoolean("Aprobado")
                runOnUiThread {
                    mostrarDatos()
                    Toast.makeText(this, "Datos recibidos de la API", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "No se pudieron obtener datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                connection?.disconnect()
            }
        }
    }


    private fun mostrarDatos() {
        txtNombre.text = "Nombre: $nombre"
        txtCalificacion.text = "Calificación: $calificacion"
        txtAprobado.text = "Aprobado: ${if (aprobado) "Sí" else "No"}"
    }

    private fun guardarDatos() {
        getSharedPreferences("DatosAplicacion", MODE_PRIVATE).edit()
            .putString("Nombre", nombre)
            .putFloat("Calificación", calificacion.toFloat())
            .putBoolean("Aprobado", aprobado)
            .apply()
        Toast.makeText(this, "Datos guardados localmente", Toast.LENGTH_SHORT).show()
    }

    private fun recuperarDatos() {
        val prefs = getSharedPreferences("DatosAplicacion", MODE_PRIVATE)
        nombre = prefs.getString("Nombre", "") ?: ""
        calificacion = prefs.getFloat("Calificación", 0f).toDouble()
        aprobado = prefs.getBoolean("Aprobado", false)
        mostrarDatos()
        Toast.makeText(this, "Datos recuperados", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}
