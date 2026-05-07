package org.example.strategies.common

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

interface  Solver<V : Vertex> {
    fun solve(g: Graph<V>): Tour<V>
}