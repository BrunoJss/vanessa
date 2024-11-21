package br.unimes.portal.vanessa

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.unimes.portal.vanessa.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mediaPlayer: MediaPlayer

    @SuppressLint("DiscouragedApi")
    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val anoAtual = calendario.get(Calendar.YEAR)

        val datePicker = DatePickerDialog(
            this,
            { _, year, _, _ ->
                binding.edtAno.setText(year.toString())
            },
            anoAtual,
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.findViewById<View>(
            resources.getIdentifier("android:id/day", null, null)
        )?.visibility = View.GONE

        datePicker.datePicker.findViewById<View>(
            resources.getIdentifier("android:id/month", null, null)
        )?.visibility = View.GONE

        datePicker.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        mediaPlayer = MediaPlayer.create(this, R.raw.fantasy)

        binding.edtAno.setOnClickListener {
            mostrarDatePicker()
        }

        binding.imbVoltar.setOnClickListener {
            finish()
        }

        binding.imbApagar.setOnClickListener {
            binding.edtNome.setText("")
            binding.edtSobrenome.setText("")
            binding.edtCadEmail.setText("")
            binding.edtCadSenha.setText("")
            binding.rdgTipoveiculoRegister.clearCheck()
            binding.spnMarcaRegister.setSelection(0)
            binding.spnModeloRegister.setSelection(0)
            binding.edtPlaca.setText("")
            binding.edtAno.setText("")
            binding.edtNome.clearFocus()
            binding.edtSobrenome.clearFocus()
        }

        binding.imbSalvar.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val sobrenome = binding.edtSobrenome.text.toString().trim()
            val email = binding.edtCadEmail.text.toString().trim()
            val password = binding.edtCadSenha.text.toString().trim()

            val selectedRadioButtonId = binding.rdgTipoveiculoRegister.checkedRadioButtonId
                val tipo = when (selectedRadioButtonId) {
                    binding.rdbMotoRegister.id -> "Moto"
                    binding.rdbCarroRegister.id -> "Carro"
                    else -> "Não especificado"
                }
            val marca = binding.spnMarcaRegister.selectedItem.toString()
            val modelo = binding.spnModeloRegister.selectedItem.toString()

            val placa = binding.edtPlaca.text.toString().trim()
            val ano = binding.edtAno.text.toString().trim()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuário criado com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Erro ao salvar dados no banco", Toast.LENGTH_SHORT).show()
                }

            val db = com.google.firebase.Firebase.firestore
            val usuarios = hashMapOf(
                "usuario_nome" to nome,
                "usuario_sobrenome" to sobrenome,
                "usuario_email" to email,
                "usuario_senha" to password,
                "veiculo_tipo" to tipo,
                "veiculo_marca" to marca,
                "veiculo_modelo" to modelo,
                "veiculo_placa" to placa,
                "veiculo_ano" to ano
            )
            db.collection("usuario")
                .document("usuario")
                .set(usuarios)
                .addOnSuccessListener{ Toast.makeText(this, "Usuário salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    mediaPlayer.start()
                }
                .addOnFailureListener{ Toast.makeText(this, "Falha ao gravar dados no banco.", Toast.LENGTH_SHORT).show()}

        }

        binding.edtPlaca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val placa = s.toString().trim()
                if (!validarPlaca(placa)) {
                    binding.edtPlaca.error = "Formato inválido. Ex.: ABC-1234 ou ABC1A23"
                }
            }


        })

        lifecycleScope.launch {
            val (marcas, modelosPorMarca) = withContext(Dispatchers.IO) { carregarDadosCSV() }

            withContext(Dispatchers.Main) {
                val marcaAdapter =
                    ArrayAdapter(this@Register, R.layout.spinner_item, marcas)
                marcaAdapter.setDropDownViewResource(R.layout.spinner_item)
                binding.spnMarcaRegister.adapter = marcaAdapter
                binding.spnMarcaRegister.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val marcaSelecionada = marcas[position]
                            val modelos = modelosPorMarca[marcaSelecionada] ?: emptyList()

                            val modeloAdapter = ArrayAdapter(
                                this@Register,
                                R.layout.spinner_item,
                                modelos.toList()
                            )
                            modeloAdapter.setDropDownViewResource(R.layout.spinner_item)
                            binding.spnModeloRegister.adapter = modeloAdapter
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            }
        }
    }

    private fun carregarDadosCSV(): Pair<List<String>, Map<String, Set<String>>> {
        val marcas = mutableListOf<String>()
        val modelosPorMarca = mutableMapOf<String, MutableSet<String>>()

        val reader = BufferedReader(InputStreamReader(assets.open("lista.csv")))

        reader.readLine()

        reader.forEachLine { line ->
            val columns = line.split(",")
            val marca = columns[0].trim()
            val modelo = columns[1].trim()

            if (marca.isNotEmpty() && modelo.isNotEmpty()) {
                marcas.addIfAbsent(marca)
                modelosPorMarca.computeIfAbsent(marca) { mutableSetOf() }.add(modelo)
            }
        }

        reader.close()
        return Pair(marcas, modelosPorMarca)
    }

    private fun <T> MutableList<T>.addIfAbsent(element: T) {
        if (!contains(element)) {
            add(element)
        }
    }
    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }
}
private fun validarPlaca(placa: String): Boolean {
    val regexAntiga = Regex("^[A-Z]{3}-\\d{4}$")
    val regexMercosul = Regex("^[A-Z]{3}\\d[A-Z0-9]{2}\\d$")
    return regexAntiga.matches(placa) || regexMercosul.matches(placa)


}
