package co.tiagoaguiar.fitnesstracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import co.tiagoaguiar.fitnesstracker.databinding.ActivityTmbBinding
import co.tiagoaguiar.fitnesstracker.model.Calc

class TmbActivity : AppCompatActivity() {

    private val binding by lazy { ActivityTmbBinding.inflate(layoutInflater) }
    private lateinit var lifestyle: AutoCompleteTextView
    private lateinit var editWeightTmb: EditText
    private lateinit var editHeightTmb: EditText
    private lateinit var editAgeTmb: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        editWeightTmb = binding.editWeightTmb
        editHeightTmb = binding.editHeightTmb
        editAgeTmb = binding.editAgeTmb
        lifestyle = binding.autoLifestyle
        val btnCalcTbm: Button = binding.btnCalcTmb

        //Campo de seleção
        val items = resources.getStringArray(R.array.tbm_lifestyle)
        lifestyle.setText(items.first())
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lifestyle.setAdapter(adapter)

        //Campo botão
        btnCalcTbm.setOnClickListener {
            if (!isValidFields()) {
                Toast.makeText(this, R.string.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //buscar os elementos da tela e converter para inteiro
            val weight = editWeightTmb.text.toString().toInt()
            val height = editHeightTmb.text.toString().toInt()
            val age = editAgeTmb.text.toString().toInt()

            val result = calculateTmb(weight, height, age)
            val response = tmbRequest(result)

            AlertDialog.Builder(this)
                .setMessage(getString(R.string.tmb_dialog, response))
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setNegativeButton(R.string.save) { _, _ ->
                    Thread {
                        val app = application as App
                        val dao = app.db.calcDao()
                        val updateId = intent.extras?.getInt("updateId")

                        if (updateId != null) {
                            dao.update(Calc(id = updateId, type = "tmb", res = response))
                        } else {
                            dao.insert(Calc(type = "tmb", res = response))
                        }

                        runOnUiThread {
                            openListCalcActivity()
                        }

                    }.start()
                }
                .create()
                .show()
            
            val service = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            service.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    private fun tmbRequest(tmb: Double): Double {
        val items = resources.getStringArray(R.array.tbm_lifestyle)
        return when {
            lifestyle.text.toString() == items[0] -> tmb * 1.2
            lifestyle.text.toString() == items[1] -> tmb * 1.375
            lifestyle.text.toString() == items[2] -> tmb * 1.55
            lifestyle.text.toString() == items[3] -> tmb * 1.725
            lifestyle.text.toString() == items[4] -> tmb * 1.9
            else -> 0.0
        }
    }

    private fun calculateTmb(weight: Int, height: Int, age: Int): Double {
        return 66 +(13.8 * weight) + (5 * height) - (6.8 * age)
    }

    private fun isValidFields(): Boolean {
        return (editWeightTmb.text.toString().isNotEmpty()
                && editHeightTmb.text.toString().isNotEmpty()
                && editAgeTmb.text.toString().isNotEmpty()
                && !editWeightTmb.text.toString().startsWith("0")
                && !editHeightTmb.text.toString().startsWith("0")
                && !editAgeTmb.text.toString().startsWith("0"))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_search){
            finish()
            openListCalcActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openListCalcActivity() {
        val intent = Intent(this, ListCalcActivity::class.java)
        intent.putExtra("type", "tmb")
        startActivity(intent)
    }
}