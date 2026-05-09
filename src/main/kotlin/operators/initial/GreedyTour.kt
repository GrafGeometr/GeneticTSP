package org.example.operators.initial

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex

class GreedyTour<V : Vertex> : SolutionInitializer<Tour<V>, TSP<V>> {
    override fun getInitial(p: TSP<V>): Tour<V> {
        val startIndex: Int = kotlin.random.Random.nextInt(p.vertexes.size)
        val n = p.vertexes.size
        val visited = BooleanArray(n)
        val tour = mutableListOf<V>()
        var current = p.vertexes[startIndex]
        visited[current.id] = true
        tour.add(current)
        repeat(n - 1) {
            var nearest: V? = null
            var nearestDist = Double.MAX_VALUE
            for (v in p.vertexes) {
                if (!visited[v.id]) {
                    val d = p.distance(current, v)
                    if (d < nearestDist) {
                        nearestDist = d
                        nearest = v
                    }
                }
            }
            current = nearest!!
            visited[current.id] = true
            tour.add(current)
        }
        return Tour(tour)
    }
}