package org.example.experiments

import org.example.model.Tour
import org.example.model.Vertex

object DiversityUtils {
    fun <V : Vertex> edgeDiversity(tours: List<Tour<V>>): Double {
        if (tours.isEmpty()) return 0.0
        val n = tours.first().list.size
        val totalEdges = tours.size * n
        val uniqueEdges = tours.flatMap { tour ->
            (0 until n).map { i ->
                val a = tour.list[i].id
                val b = tour.list[(i + 1) % n].id
                if (a < b) a to b else b to a
            }
        }.distinct().size
        return uniqueEdges.toDouble() / totalEdges
    }
}