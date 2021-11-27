package de.martingropp.blackbox

import kotlin.math.sign

class Util {
    companion object {
        /**
         * Returns the index of a button/hint,
         * i.e. maps the coordinates of an edge field (outside the
         * 0 until numCols / 0 until numRows board) to a 1D index
         * of a button/hint.
         * Coordinates:
         * T of TOP is at (0, -1)
         * T of RIGHT is at (numCols, numRows-1)
         *
         *     T  O  P
         *   L         R
         *   E         I
         *   F         G
         *   T         H
         *   |         T
         *     BOTTOM-
         *
         * Order: LEFT RIGHT TOP BOTTOM
         *
         * @param x
         *   x coordinate of the button/hint field
         * @param y
         *   y coordinate of the button/hint field
         * @param numCols
         *   number of columns in the board
         * @param numRows
         *   number of rows in the board
         */
        fun getEdgeFieldIndex(x: Int, y: Int, numCols: Int, numRows: Int): Int {
            assert(x !in 0 until numCols || y == -1 || y == numRows) { "Invalid edge coordinates: $x $y" }
            assert(y !in 0 until numRows || x == -1 || x == numCols) { "Invalid edge coordinates: $x $y" }
            assert(numCols > 0)
            assert(numRows > 0)

            return when {
                x == -1 -> y
                x == numCols -> numRows + y
                y == -1 -> 2 * numRows + x
                y == numRows -> 2 * numRows + numCols + x
                else -> throw AssertionError("Bad coordinates: $x $y")
            }

//            return (
//                dx*dx * ((dx+1).sign * numRows + y) +
//                dy*dy * (2*numRows + (dy+1).sign * numCols + x)
//            )
        }
    }
}