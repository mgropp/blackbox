package de.martingropp.blackbox

import kotlin.random.Random

import android.util.Log


class BlackBoxBoard(
    val numCols: Int,
    val numRows: Int,
    val numAtoms: Int
) {
    interface MarkerListener {
        fun markerChanged(x: Int, y: Int, marker: Boolean)
    }

    interface HintListener {
        fun hintAdded(x: Int, y: Int, hint: Hint)
    }

    private val atoms: Set<Pair<Int, Int>>
    private val markers: MutableSet<Pair<Int, Int>> = mutableSetOf()
    private val hints: MutableMap<Pair<Int, Int>, Hint> = mutableMapOf()

    private var nextHintReference = 0
    private var numUniqueHints = 0

    private val markerListeners: MutableList<MarkerListener> = mutableListOf()
    private val hintListeners: MutableList<HintListener> = mutableListOf()

    init {
        // create atoms
        // TODO: prevent ambiguous configurations
        val atoms: MutableSet<Pair<Int, Int>> = mutableSetOf()
        for (i in 1..numAtoms) {
            while (true) {
                val xy = Pair(Random.nextInt(numCols), Random.nextInt(numRows))

                if (!atoms.contains(xy)) {
                    Log.i(null, "Placed atom at $xy")
                    atoms.add(xy)
                    break
                }
            }
        }
        this.atoms = atoms
    }

    fun getAtoms(): Set<Pair<Int, Int>> {
        return atoms.toSet()
    }

    fun addMarkerListener(listener: MarkerListener) {
        markerListeners.add(listener)
    }

    fun removeMarkerListener(listener: MarkerListener) {
        markerListeners.remove(listener)
    }

    fun getMarkers(): Set<Pair<Int, Int>> {
        return markers.toSet()
    }

    fun toggleMarker(x: Int, y: Int) {
        val pair = Pair(x, y)
        val marker = markers.add(pair) || !markers.remove(pair)
        for (listener in markerListeners) {
            listener.markerChanged(x, y, marker)
        }
    }

    fun addHintListener(listener: HintListener) {
        hintListeners.add(listener)
    }

    fun removeHintListener(listener: HintListener) {
        hintListeners.remove(listener)
    }

    fun getHint(x: Int, y: Int): Hint? {
        return hints[Pair(x, y)]
    }

    fun getHints(): Map<Pair<Int, Int>, Hint> {
        return hints.toMap()
    }

    private fun setHint(x: Int, y: Int, hint: Hint, secondary: Boolean = false) {
        hints[Pair(x, y)] = hint
        if (!secondary) {
            numUniqueHints++
        }
        for (listener in hintListeners) {
            listener.hintAdded(x, y,  hint)
        }
    }

    fun getNumUniqueHints(): Int {
        return numUniqueHints
    }

    fun findHints(x: Int, y: Int) {
        var x2 = x
        var y2 = y
        var dx = when {
            x < 0 -> 1
            x >= numCols -> -1
            else -> 0
        }
        var dy = when {
            y < 0 -> 1
            y >= numRows -> -1
            else -> 0
        }
        assert((dx == 0) != (dy == 0))

        fun hit(): Boolean {
            return atoms.contains(Pair(x2 + dx, y2 + dy))
        }

        fun atomLeft(): Boolean {
            return atoms.contains(Pair(x2 + dx + dy, y2 + dy - dx))
        }

        fun atomRight(): Boolean {
            return atoms.contains(Pair(x2 + dx - dy, y2 + dy + dx))
        }

        // hit at beginning
        if (hit()) {
            Log.i(null, "Hit at start")
            setHint(x, y, Hint(hit=true))
            return
        }

        // reflected at beginning
        if (atomLeft() || atomRight()) {
            Log.i(null, "Reflected at start")
            setHint(x, y, Hint(reflect=true))
            return
        }

        while (true) {
            // Make a step
            x2 += dx
            y2 += dy

            Log.i(null, "position: $x2 $y2 direction: $dx $dy")

            // Did we leave the board?
            if (x2 !in 0 until numCols || y2 !in 0 until numRows) {
                // Found an exit
                Log.i(null, "Found an exit at $x2 $y2")
                if (x == x2 && y == y2) {
                    // reflected
                    Log.i(null, "Reflected")
                    setHint(x, y, Hint(reflect=true))
                } else {
                    Log.i(null, "Found way through")
                    val hint = Hint(reference=(nextHintReference++))
                    setHint(x, y, hint)
                    setHint(x2, y2, hint, secondary=true)
                }

                return
            }

            // Hit?
            if (hit()) {
                Log.i(null, "Hit $x2+$dx $y2+$dy")
                setHint(x, y, Hint(hit=true))
                return
            }

            val atomLeft = atomLeft()
            val atomRight = atomRight()

            if (atomLeft && atomRight) {
                // Reflected
                Log.i(null, "Reflected")
                setHint(x, y, Hint(reflect=true))
                return
            } else if (atomLeft) {
                // Atom on the left side -> turn right
                Log.i(null, "Turning right")
                dy = dx.also { dx = -dy }
            } else if (atomRight) {
                // Atom on the right side -> turn left
                Log.i(null, "Turning left")
                dy = -dx.also { dx = dy }
            }
        }
    }
}