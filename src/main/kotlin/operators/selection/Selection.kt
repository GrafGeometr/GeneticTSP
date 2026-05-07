package org.example.operators.selection

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

interface Selection {
    fun <V : Vertex> select(population: List<Tour<V>>, graph: Graph<V>): Tour<V>
}