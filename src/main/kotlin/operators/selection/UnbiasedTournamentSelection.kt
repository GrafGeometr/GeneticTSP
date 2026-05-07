package org.example.operators.selection

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

class UnbiasedTournamentSelection(val populationSize: Int) : Selection {
    private val indices = mutableListOf<Int>()
    private var pos = 0

    init {
        resetIndices()
    }

    private fun resetIndices() {
        indices.clear()
        repeat(2) {
            for (i in 0 until populationSize) indices.add(i)
        }
        indices.shuffle()
        pos = 0
    }

    override fun <V : Vertex> select(population: List<Tour<V>>, graph: Graph<V>): Tour<V> {
        if (pos >= indices.size) resetIndices()
        val i1 = indices[pos]
        val i2 = indices[pos + 1]
        pos += 2
        val t1 = population[i1]
        val t2 = population[i2]
        return if (graph.fitness(t1) <= graph.fitness(t2)) t1 else t2
    }
}