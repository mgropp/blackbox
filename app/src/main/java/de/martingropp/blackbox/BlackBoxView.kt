package de.martingropp.blackbox

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Space
import android.widget.TextView

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


class BlackBoxView :
    ConstraintLayout,
    BlackBoxBoard.MarkerListener,
    BlackBoxBoard.HintListener
{
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var tileControls: Array<Array<TextView>>? = null
    private var buttonControls: List<Button>? = null

    var board: BlackBoxBoard? = null
        set(value) {
            val old = field
            field = value

            if (old !== null) {
                old.removeMarkerListener(this)
                old.removeHintListener(this)
            }

            removeAllViews()

            if (value !== null) {
                createGrid(this, value.numCols, value.numCols, value)
                value.addMarkerListener(this)
                value.addHintListener(this)

                // debug
                /*
                val fieldControls = this.fieldControls
                if (fieldControls !== null) {
                    for (y in 0 until value.numRows) {
                        for (x in 0 until value.numCols) {
                            if (value.getAtom(x, y)) {
                                fieldControls[y][x].setBackgroundColor(0x00ff00)
                                fieldControls[y][x].text = "⦿"
                            }
                        }
                    }
                }
                */
            }
        }

    var showSolution: Boolean = false
        set(value) {
            field = value
            updateMarkers(value)
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private fun createItems(
        parent: ConstraintLayout,
        numCols: Int,
        numRows: Int,
        board: BlackBoxBoard
    ): List<List<View>> {
        val context = this.context

        fun createButtons(n: Int, fixedX: Int?, fixedY: Int?): List<Button> {
            assert((fixedX === null) != (fixedY === null))
            return (0 until n).map {
                val x = fixedX ?: it
                val y = fixedY ?: it
                val layout = if (fixedX === null) R.layout.laser_button_v else R.layout.laser_button_h
                (inflater.inflate(layout, null) as Button)
                .apply {
                    id = View.generateViewId()
                    parent.addView(
                        this,
                        LayoutParams(
                            ConstraintSet.MATCH_CONSTRAINT,
                            ConstraintSet.MATCH_CONSTRAINT
                        )
                    )
                    setOnClickListener {
                        if (!showSolution && board.getHint(x, y) === null) {
                            board.findHints(x, y)
                        }
                    }
                }
            }
        }
        fun createField(x: Int, y: Int): TextView {
            return (
                inflater.inflate(
                    if ((x + y) % 2 == 0) R.layout.board_tile_1 else R.layout.board_tile_2,
                    null
                ) as TextView
            )
            .apply {
                id = View.generateViewId()
                setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
                gravity = Gravity.CENTER
                setOnClickListener {
                    if (!showSolution) {
                        board.toggleMarker(x, y)
                    }
                }
                parent.addView(
                    this,
                    LayoutParams(
                        ConstraintSet.MATCH_CONSTRAINT,
                        ConstraintSet.MATCH_CONSTRAINT
                    )
                )
            }
        }

        fun createSpace(): Space {
            return Space(context)
                .apply {
                    id = View.generateViewId()
                    parent.addView(
                        this,
                        LayoutParams(
                            ConstraintSet.MATCH_CONSTRAINT,
                            ConstraintSet.MATCH_CONSTRAINT
                        )
                    )
                }
        }

        val topButtons = createButtons(numRows, null, -1)
        val bottomButtons = createButtons(numRows, null, numCols)
        val leftButtons = createButtons(numCols, -1, null)
        val rightButtons = createButtons(numCols, numRows, null)

        this.buttonControls = leftButtons + rightButtons + topButtons + bottomButtons

        val fields = (0 until numRows).map { y ->
            (0 until numCols).map { x ->
                createField(x, y)
            }.toTypedArray()
        }.toTypedArray()

        this.tileControls = fields

        return (
                listOf(
                    listOf(createSpace()) + topButtons + listOf(createSpace())
                ) +
                fields.zip(leftButtons zip rightButtons).map {
                        (fieldRow, buttons) ->
                    val (leftButton, rightButton) = buttons
                    listOf(leftButton) + fieldRow + listOf(rightButton)
                } +
                listOf(
                    listOf(createSpace()) + bottomButtons + listOf(createSpace())
                )
        )
    }

    private fun createGrid(
        parent: ConstraintLayout,
        numCols: Int,
        numRows: Int,
        board: BlackBoxBoard
    ) {
        val items = createItems(parent, numCols, numRows, board)

        val cs = ConstraintSet()
        cs.clone(parent)
        cs.setDimensionRatio(parent.id, "${numCols}:${numRows}")

        var prevRow: List<View>? = null
        for (row in items) {
            row.forEachIndexed {
                i, item ->
                cs.setDimensionRatio(item.id, "1:1")
                val p = prevRow
                if (p === null) {
                    cs.connect(
                        item.id,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP
                    )
                } else {
                    cs.connect(
                        item.id,
                        ConstraintSet.TOP,
                        p[i].id,
                        ConstraintSet.BOTTOM
                    )
                }
            }

            cs.createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                row.map { it.id }.toIntArray(),
                null,
                ConstraintSet.CHAIN_PACKED
            )

            prevRow = row
        }

        cs.applyTo(parent)
    }

    override fun markerChanged(x: Int, y: Int, marker: Boolean) {
        val controls = tileControls
        if (controls !== null) {
            controls[y][x].text = if (marker) "\uD83D\uDEA9" else ""
        }
    }

    override fun hintAdded(x: Int, y: Int, hint: Hint) {
        Log.i(null, "Hint added at $x $y: $hint")
        val buttonControls = this.buttonControls
        val board = this.board
        if (buttonControls !== null && board !== null) {
            val button = buttonControls[
                Util.getEdgeFieldIndex(x, y, board.numCols, board.numRows)
            ]

            button.isEnabled = false
            button.text = when {
                hint.reflect -> "R"
                hint.hit -> "H"
                else -> hint.reference.toString()
            }

            /*
            //context.theme.
            val attrs = context.obtainStyledAttributes(
                R.style.ButtonOn,
                intArrayOf(android.R.attr.backgroundTint, android.R.attr.strokeColor)
            )

            button.backgroundTintList = ColorStateList.valueOf(attrs.getColor(0, Color.GREEN))

            val background = button.background
            if (background is RippleDrawable) {
                background.
            }
            Log.i(null, "background type ${button.background::class}")

            attrs.recycle()
            */
        }
    }

    private fun updateMarkers(showSolution: Boolean = false) {
        val board = this.board
        val tileControls = this.tileControls
        if (board === null || tileControls === null) {
            return
        }

        val atoms = board.getAtoms()
        val markers = board.getMarkers()

        for (y in 0 until board.numRows) {
            for (x in 0 until board.numCols) {
                val xy = Pair(x, y)
                tileControls[y][x].text = when {
                    showSolution && atoms.contains(xy) && markers.contains(xy) -> "✅"
                    showSolution && markers.contains(xy) -> "❌"
                    showSolution && atoms.contains(xy) -> "⚛"
                    markers.contains(xy) -> "\uD83D\uDEA9"
                    else -> ""
                }
            }
        }
    }
}
