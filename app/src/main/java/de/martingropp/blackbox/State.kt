package de.martingropp.blackbox

import android.content.SharedPreferences
import android.util.Log

object State {
    var debug = false

    private var preferences: SharedPreferences? = null
    private var board: BlackBoxBoard? = null

    fun setPreferences(value: SharedPreferences) {
        this.preferences = value
    }

    fun getBoard(): BlackBoxBoard {
        if (preferences === null) {
            throw IllegalStateException("Preferences not set.")
        }

        val board = this.board
        return if (board !== null) board else restart()
    }

    private fun getInt(key: String, minValue: Int, maxValue: Int, defaultValue: Int): Int {
        val preferences = preferences
        if (preferences === null) {
            throw IllegalStateException("Preferences not set.")
        }

        val value = try {
            val strValue = preferences.getString(key, null)
            if (strValue === null) {
                defaultValue
            } else {
                Integer.valueOf(strValue)
            }
        } catch (e: Exception) {
            defaultValue
        }

        return if (value in minValue..maxValue) {
            value
        } else {
            defaultValue
        }
    }

    private fun getNumCols(): Int {
        return getInt("num_columns", 4, 12, 8)
    }

    private fun getNumRows(): Int {
        return getInt("num_rows", 4, 12, 8)
    }

    private fun getNumAtoms(): Int {
        return getInt("num_atoms", 1, 10, 4)
    }

    fun restart(): BlackBoxBoard {
        Log.i(null, "Settings ${preferences?.all}")
        val board = BlackBoxBoard(
            getNumCols(),
            getNumRows(),
            getNumAtoms()
        )

        this.board = board
        return board
    }

    fun settingsChanged(): Boolean {
        val board = this.board
        if (board === null) {
            throw IllegalStateException("Board not set.")
        }

        return (
            (board.numCols != getNumCols()) ||
            (board.numRows != getNumRows()) ||
            (board.numAtoms != getNumAtoms())
        )
    }
}