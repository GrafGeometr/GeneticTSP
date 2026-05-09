package org.example.model

data class Tour<V : Vertex>(val list: List<V>) : Solution {
    var fitness: Double? = null

    fun at(idx: Int) = list[idx % list.size]

    override fun toString() = list.map { it.id }.joinToString(" ")
}
