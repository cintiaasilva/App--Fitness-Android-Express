package co.tiagoaguiar.fitnesstracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import co.tiagoaguiar.fitnesstracker.model.Calc

class ImcActivity : AppCompatActivity() {

    private lateinit var editWeightImc: EditText
    private lateinit var editHeightImc: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imc)

        editWeightImc = findViewById(R.id.edit_weightImc)
        editHeightImc = findViewById(R.id.edit_heightImc)
        val btnCalcImc: Button = findViewById(R.id.btn_calcImc)

        btnCalcImc.setOnClickListener {
            if (!isValidFields()) {
                Toast.makeText(this, R.string.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = editWeightImc.text.toString().toInt()
            val height = editHeightImc.text.toString().toInt()

            val result = calculate(weight, height)

            val imcResponse = imcResponse(result)

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.imc_dialog, result))
                .setMessage(imcResponse)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setNegativeButton(R.string.save) { _, _ ->
                    Thread {
                        val app = application as App
                        val dao = app.db.calcDao()
                        val updateId = intent.extras?.getInt("updateId")

                        if (updateId != null) {
                            dao.update(Calc(id = updateId, type = "imc", res = result))
                        } else {
                            dao.insert(Calc(type = "imc", res = result))
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
        intent.putExtra("type", "imc")
        startActivity(intent)
    }

    private fun isValidFields(): Boolean {
        return (editWeightImc.text.toString().isNotEmpty()
                && editHeightImc.text.toString().isNotEmpty()
                && !editWeightImc.text.toString().startsWith("0")
                && !editHeightImc.text.toString().startsWith("0"))
    }

    private fun calculate(weight: Int, height: Int): Double {
        val heightDecimal = height / 100.0
        return weight / (heightDecimal * heightDecimal)
    }

    @StringRes
    private fun imcResponse(imc: Double): Int {
        return when {
            imc < 15.0 -> R.string.imc_severely_low_weight
            imc < 16.0 -> R.string.imc_very_low_weight
            imc < 18.5 -> R.string.imc_low_weight
            imc < 25.0 -> R.string.imc_normal
            imc < 30.0 -> R.string.imc_high_weight
            imc < 35.0 -> R.string.imc_so_high_weight
            imc < 40.0 -> R.string.imc_severely_high_weight
            else -> R.string.imc_extreme_weight
        }
    }
}