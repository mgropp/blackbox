package de.martingropp.blackbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.view.Menu
import android.view.MenuItem


class MainActivity : AppCompatActivity() {
    private fun startGame(restart: Boolean) {
        val board = if (restart) State.restart() else State.getBoard()

        val blackBoxView: BlackBoxView = findViewById(R.id.blackBox)
        val scoreTextView: TextView = findViewById(R.id.scoreText)
        val checkButton: Button = findViewById(R.id.checkButton)

        fun updateInfoTextView() {
            val numMarkers = board.getMarkers().size
            val numAtoms = board.numAtoms
            val numHints = board.getNumUniqueHints()
            scoreTextView.text = getString(
                R.string.info_text,
                numHints,
                numMarkers,
                numAtoms
            )
        }

        checkButton.text = getString(R.string.button_check)
        updateInfoTextView()
        board.addMarkerListener(object : BlackBoxBoard.MarkerListener {
            override fun markerChanged(x: Int, y: Int, marker: Boolean) {
                updateInfoTextView()
                checkButton.isEnabled = (board.getMarkers().size == board.numAtoms)
            }
        })
        board.addHintListener(object : BlackBoxBoard.HintListener {
            override fun hintAdded(x: Int, y: Int, hint: Hint) {
                updateInfoTextView()
            }
        })

        blackBoxView.debug = State.debug
        blackBoxView.board = board
        blackBoxView.showSolution = false

        checkButton.setOnClickListener {
            blackBoxView.showSolution = true
            checkButton.text = getString(R.string.button_new_game)
            checkButton.setOnClickListener {
                checkButton.isEnabled = false
                startGame(true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        State.setPreferences(pref)
        Log.i(null, "initial preferences ${pref.all}")

        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        //supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        startGame(false)
    }

    override fun onPostResume() {
        super.onPostResume()

        if (State.settingsChanged()) {
            AlertDialog.Builder(this)
                .setMessage("Settings changed. Restart game?")
                .setPositiveButton(
                    "Yes"
                ) { _, _ ->
                    startGame(true)
                }
                .setNegativeButton(
                    "No"
                ) { _, _ ->
                }
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            showSettings()
        }
        return true
    }

    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}