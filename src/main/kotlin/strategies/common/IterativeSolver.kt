package org.example.strategies.common

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

interface IterativeSolver<V : Vertex> : Solver<V> {
    interface Data<V : Vertex> {
        fun getSolution(): Tour<V>
    }

    fun oneIteration(data: Data<V>): Data<V>

    fun init(g: Graph<V>): Data<V>

    fun stopCriteria(data: Data<V>): Boolean

    override fun solve(g: Graph<V>): Tour<V> {
        var data = init(g)

        while (!stopCriteria(data)) {
            data = oneIteration(data)
        }

        return data.getSolution()
    }
}