package de.martingropp.blackbox

data class Hint(
    val reference: Int = -1,
    val hit: Boolean = false,
    val reflect: Boolean = false
) {
}