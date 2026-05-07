package org.example.model

data class Tour<V : Vertex>(val list: List<V>) {
    fun at(idx: Int) = list[idx % list.size]

    override fun toString() = list.map { it.id }.joinToString(" ")

    companion object {
        fun <V : Vertex> random(graph: Graph<V>) = Tour(graph.vertexes.shuffled())
    }
}
