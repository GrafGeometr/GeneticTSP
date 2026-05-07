package org.example.operators.selection

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class ParameterizedTournamentSelector(val sv: Double) : Selection {
    init {
        require(sv in 0.5..1.0) { "sv must be in [0.5, 1.0]" }
    }

    override fun <V : Vertex> select(population: List<Tour<V>>, graph: Graph<V>): Tour<V> {
        val a = population.random()
        val b = population.random()
        val (better, worse) = if (graph.fitness(a) <= graph.fitness(b)) a to b else b to a
        return if (Random.nextDouble() < sv) better else worse
    }
}