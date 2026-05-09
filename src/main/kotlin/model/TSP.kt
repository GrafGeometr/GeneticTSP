package org.example.model

interface TSP<V : Vertex> : Problem<Tour<V>> {
    val vertexes: List<V>

    fun distance(v: V, u: V): Double

    override fun fitness(solution: Tour<V>) = solution.fitness ?: solution.list.indices.sumOf { idx ->
        distance(solution.list[idx], solution.list[if (idx == solution.list.size - 1) 0 else idx + 1])
    }.also { solution.fitness = it }
}