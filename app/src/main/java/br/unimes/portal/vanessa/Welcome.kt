package br.unimes.portal.vanessa

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.unimes.portal.vanessa.databinding.ActivityWelcomeBinding


class Welcome : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer.create(this, R.raw.yoshi)

        binding.imgFeflogoWelcome.setOnClickListener{
            mediaPlayer.start()
        }
        binding.btnRegisterWelcome.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
        binding.btnLoginWelcome.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

    }
    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }
}