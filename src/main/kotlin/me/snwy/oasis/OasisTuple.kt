package me.snwy.oasis

class OasisTuple<T>(override val size: Int, private val values: ArrayList<T>) : List<T> {
    override fun contains(element: T): Boolean {
        return values.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return values.containsAll(elements)
    }

    override fun get(index: Int): T {
        return values[index]
    }

    override fun isEmpty(): Boolean {
        return values.isEmpty()
    }

    override fun iterator(): Iterator<T> {
        return values.iterator()
    }

    override fun listIterator(): ListIterator<T> {
        return values.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<T> {
        return values.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        return values.subList(fromIndex, toIndex)
    }

    override fun lastIndexOf(element: T): Int {
        return values.lastIndexOf(element)
    }

    override fun indexOf(element: T): Int {
        return values.indexOf(element)
    }

    override fun toString(): String {
        return "(${values.joinToString(", ")})"
    }
}