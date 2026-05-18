package org.example.experiments

import org.example.model.Tour
import org.example.model.Vertex

object DiversityUtils {
    fun <V : Vertex> edgeDiversity(tours: List<Tour<V>>): Double {
        if (tours.size < 2) return 0.0
        val n = tours.first().list.size
        val edgeSets = tours.map { tour ->
            (0 until n).map { i ->
                val a = tour.list[i].id
                val b = tour.list[(i + 1) % n].id
                if (a < b) a to b else b to a
            }.toSet()
        }
        var sumDissimilarity = 0.0
        var pairCount = 0
        for (i in edgeSets.indices) {
            for (j in i + 1 until edgeSets.size) {
                val intersection = edgeSets[i].intersect(edgeSets[j]).size
                val dissimilarity = 1.0 - intersection.toDouble() / n
                sumDissimilarity += dissimilarity
                pairCount++
            }
        }
        return sumDissimilarity / pairCount
    }
}