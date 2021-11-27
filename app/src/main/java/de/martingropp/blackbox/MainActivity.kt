package de.martingropp.blackbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_main)
        val blackBoxView: BlackBoxView = findViewById(R.id.blackBox)
        val scoreTextView: TextView = findViewById(R.id.scoreText)
        val checkButton: Button = findViewById<Button>(R.id.checkButton)

        fun restartGame() {
            val board = BlackBoxBoard(8, 8, 4)

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

            blackBoxView.board = board
            blackBoxView.showSolution = false

            checkButton.setOnClickListener {
                blackBoxView.showSolution = true
                checkButton.text = getString(R.string.button_new_game)
                checkButton.setOnClickListener {
                    checkButton.isEnabled = false
                    restartGame()
                }
            }
        }

        restartGame()
    }
}