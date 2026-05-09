package org.example.operators.postprocessing.localsearch

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex

class HillClimbing2Opt<V : Vertex>(val mode: Mode) : LocalSearch<V> {
    enum class Mode {
        First,
        Best
    }

    override fun improve(tour: Tour<V>, p: TSP<V>): Tour<V> {
        var current = tour
        while (true) {
            val next = better(current, p) ?: break
            current = next
        }
        return current
    }

    private fun <V : Vertex> better(tour: Tour<V>, p: TSP<V>): Tour<V>? {
        val n = tour.list.size
        var bestI = -1
        var bestJ = -1
        var bestImprovement = 0.0

        outer@ for (i in 0 until n) {
            val a = tour.at(i)
            val b = tour.at(i + 1)
            for (j in i + 2 until n + i - 1) {
                if (j % n == i) continue
                val c = tour.at(j)
                val d = tour.at(j + 1)
                val oldDist = p.distance(a, b) + p.distance(c, d)
                val newDist = p.distance(a, c) + p.distance(b, d)
                val improvement = oldDist - newDist
                if (improvement > bestImprovement) {
                    bestImprovement = improvement
                    bestI = i
                    bestJ = j % n

                    if (mode == Mode.First) {
                        break@outer
                    }
                }
            }
        }
        if (bestImprovement <= 0.0) return null

        val newList = mutableListOf<V>()
        for (k in 0..bestI) newList.add(tour.at(k))
        for (k in bestJ downTo bestI + 1) newList.add(tour.at(k))
        for (k in bestJ + 1 until n) newList.add(tour.at(k))

        return Tour(newList)
    }
}