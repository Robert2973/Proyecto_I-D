package com.example.buap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider // Necesario para GitHub/Microsoft
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 9001
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // --- Inicializar Firebase ---
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // --- Configuración de Google Sign-In ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- Referencias a la UI ---
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val btnGoogleSignIn = findViewById<ImageView>(R.id.btnGoogleSignIn)
        // ** NUEVO: Referencia para el botón de GitHub **
        val btnGithubLogin = findViewById<ImageView>(R.id.btnGithubLogin) // Asumiendo que es un ImageView o Button

        // --- Login con Email y Contraseña ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tus datos", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Bienvenido $email", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // --- Ir a registro ---
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --- Login con Google ---
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // ** NUEVO: Login con GitHub **
        btnGithubLogin.setOnClickListener {
            signInWithGithub()
        }

        // --- Si ya hay sesión iniciada ---
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // --- Lanzar intent de Google Sign-In ---
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // --- Resultado de intento de Google ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
                Toast.makeText(this, "Error al iniciar con Google.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----------------------------------------------------------------------------------
    // 🚀 FUNCIONES DE AUTENTICACIÓN CON GITHUB (OAuth)
    // ----------------------------------------------------------------------------------

    private fun signInWithGithub() {
        // 1. Crear el proveedor para GitHub
        val provider = OAuthProvider.newBuilder("github.com")

        // 2. Opcional: Solicitar scopes para obtener email y perfil
        provider.setScopes(listOf("user", "user:email"))

        // 3. Iniciar el flujo de autenticación
        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                // Inicio de sesión exitoso. Se usa la función centralizada de guardado/redirección.
                Toast.makeText(this, "Inicio de sesión con GitHub exitoso.", Toast.LENGTH_SHORT).show()
                saveUserAndNavigate(authResult.user)
            }
            .addOnFailureListener { e ->
                // El inicio de sesión falló
                Log.e("GithubAuth", "Error en GitHub: ${e.message}", e)
                Toast.makeText(this, "Error al iniciar sesión con GitHub: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ----------------------------------------------------------------------------------
    // 💾 FUNCIONES DE MANEJO DE USUARIO EN FIRESTORE
    // ----------------------------------------------------------------------------------

    // Función unificada para guardar los datos del usuario después de la autenticación
    private fun saveUserAndNavigate(user: FirebaseUser?) {
        user?.let { firebaseUser ->
            val userDoc = db.collection("usuarios").document(firebaseUser.uid)

            // Intenta obtener el documento. Si no existe, lo crea.
            userDoc.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    val newUser = hashMapOf(
                        // Los datos vienen del proveedor (Google, GitHub, etc.)
                        "nombre" to (firebaseUser.displayName ?: "N/A"),
                        "email" to (firebaseUser.email ?: "N/A"),
                        "fotoPerfil" to (firebaseUser.photoUrl?.toString() ?: ""),
                        // Campos que Firebase no proporciona directamente y deben rellenarse después
                        "telefono" to (document.getString("telefono") ?: ""),
                        "edad" to (document.getString("edad") ?: "")
                    )
                    userDoc.set(newUser)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Nuevo usuario guardado o actualizado.")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error al guardar usuario: $e")
                        }
                }

                // Redirigir siempre después de la autenticación y el chequeo/guardado de datos
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    // --- Autenticación Firebase con Google (Modificada para usar saveUserAndNavigate) ---
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Llamar a la función unificada para guardar y navegar
                    saveUserAndNavigate(auth.currentUser)
                } else {
                    Toast.makeText(this, "Error en autenticación con Firebase.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}