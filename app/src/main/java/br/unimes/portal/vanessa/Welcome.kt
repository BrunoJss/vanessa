package br.unimes.portal.vanessa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.unimes.portal.vanessa.databinding.ActivityWelcomeBinding


class Welcome : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnWelcomecad.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
        binding.btnWelcomelogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}