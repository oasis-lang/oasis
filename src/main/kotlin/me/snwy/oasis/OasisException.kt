package me.snwy.oasis

data class OasisException(val value: Any?) : Exception() {
    override fun toString(): String {
        return value.toString()
    }
}
