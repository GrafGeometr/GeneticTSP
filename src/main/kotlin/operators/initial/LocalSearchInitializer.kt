package org.example.operators.initial

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import org.example.operators.postprocessing.localsearch.LocalSearch

class LocalSearchInitializer<V : Vertex>(
    private val baseInitializer: SolutionInitializer<Tour<V>, TSP<V>>,
    private val localSearch: LocalSearch<V>
) : SolutionInitializer<Tour<V>, TSP<V>> {

    override fun getInitial(p: TSP<V>): Tour<V> {
        val tour = baseInitializer.getInitial(p)
        return localSearch.improve(tour, p)
    }
}