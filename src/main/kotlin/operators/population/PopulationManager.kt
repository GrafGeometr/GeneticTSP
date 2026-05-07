package org.example.operators.population

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

interface PopulationManager<V : Vertex> {
    fun initialize(graph: Graph<V>)
    fun addOffspring(offspring: Tour<V>): Boolean
    fun getAll(): List<Tour<V>>
    fun getBest(): Tour<V>
    val size: Int

    fun afterGeneration() {}
}