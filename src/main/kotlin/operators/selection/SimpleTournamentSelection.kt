package org.example.operators.selection

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

class SimpleTournamentSelection(val tournamentSize: Int = 2) : Selection {
    override fun <V : Vertex> select(population: List<Tour<V>>, graph: Graph<V>): Tour<V> {
        val candidates = (1..tournamentSize).map { population.random() }
        return candidates.minBy { graph.fitness(it) }
    }
}