package com.example.buap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.common.api.ApiException

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Código para Google Sign-In

    // Vistas de autenticación social
    private lateinit var btnGoogle: ImageView
    private lateinit var btnGitHub: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        // 2. Inicialización de Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Usa tu Web Client ID (server_client_id)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEdad = findViewById<EditText>(R.id.etEdad)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etEmail = findViewById<EditText>(R.id.etEmailRegister)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBack = findViewById<TextView>(R.id.tvBack)

        btnGoogle = findViewById(R.id.btnGoogleSignIn)
        btnGitHub = findViewById(R.id.btnGitHubSignIn)

        tvBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val edad = etEdad.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()


            googleSignInClient = GoogleSignIn.getClient(this, gso)


            when {
                nombre.isEmpty() || edad.isEmpty() || telefono.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    registrarUsuario(nombre, edad, telefono, email, password)
                }
            }
        }
        // 3. Listeners para Autenticación Social
        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        btnGitHub.setOnClickListener {
            // Llama a la función de GitHub (debes replicarla de LoginActivity)
            // Nota: El flujo de GitHub a menudo requiere un ActivityResultLauncher o un Intent
            signInWithGithub()
        }
    }

    private fun registrarUsuario(nombre: String, edad: String, telefono: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    val user = hashMapOf(
                        "nombre" to nombre,
                        "edad" to edad,
                        "telefono" to telefono,
                        "email" to email,
                        "fotoPerfil" to ""
                    )

                    userId?.let {
                        // Guardar datos del perfil completo y navegar
                        saveFullRegistrationData(it, user)
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Iniciar flujo de GitHub (OAuth)
    private fun signInWithGithub() {
        val provider = OAuthProvider.newBuilder("github.com")
        provider.setScopes(listOf("user", "user:email"))

        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                Toast.makeText(this, "Registro con GitHub exitoso.", Toast.LENGTH_SHORT).show()
                saveUserAndNavigate(authResult.user)
            }
            .addOnFailureListener { e ->
                Log.e("GithubAuth", "Error en GitHub: ${e.message}", e)
                Toast.makeText(this, "Error al registrar con GitHub: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
                Toast.makeText(this, "Error al iniciar con Google.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    private fun saveUserAndNavigate(user: FirebaseUser?) {
        user?.let { firebaseUser ->
            val userDoc = db.collection("usuarios").document(firebaseUser.uid)

            userDoc.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Si no existe, crear el perfil básico con datos del proveedor social
                    val newUser = hashMapOf(
                        "nombre" to (firebaseUser.displayName ?: "N/A"),
                        "email" to (firebaseUser.email ?: "N/A"),
                        "fotoPerfil" to (firebaseUser.photoUrl?.toString() ?: ""),
                        "telefono" to "", // Dejar vacío
                        "edad" to ""      // Dejar vacío
                    )
                    userDoc.set(newUser)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Nuevo usuario social guardado.")
                            Toast.makeText(this, "Cuenta social registrada con éxito.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error al guardar usuario social: $e")
                        }
                }

                // Redirigir
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
    private fun saveFullRegistrationData(userId: String, userData: HashMap<String, String>) {
        db.collection("usuarios").document(userId).set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

