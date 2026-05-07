package org.example.model

interface Graph<V : Vertex> {
    val vertexes: List<V>

    fun distance(v: V, u: V): Double

    fun fitness(path: Tour<V>) = path.list.indices.sumOf { idx ->
        distance(path.list[idx], path.list[if (idx == path.list.size - 1) 0 else idx + 1])
    }
}