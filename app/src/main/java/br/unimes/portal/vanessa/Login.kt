package br.unimes.portal.vanessa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.unimes.portal.vanessa.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            val email=binding.edtEmail.text.toString()
            val password=binding.edtPassword.text.toString()
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Bem Vindo!", Toast.LENGTH_SHORT).show()
                    binding.edtEmail.text.clear()
                    binding.edtPassword.text.clear()
                    Toast.makeText(this, "carregando...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Welcome::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Toast.makeText(this, "404 ERROR", Toast.LENGTH_SHORT).show()
                }
        }
        binding.btnEsqueciSenha.setOnClickListener {
            val email=binding.edtEmail.text.toString()
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Redefinição encaminhada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Errou E-mail", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
