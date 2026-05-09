package org.example.operators.initial

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import kotlin.math.pow
import kotlin.random.Random

class RandomTour<V : Vertex> : SolutionInitializer<Tour<V>, TSP<V>> {
    override fun getInitial(p: TSP<V>) = Tour(p.vertexes.shuffled())
}

class ProbabilisticGreedyTour<V : Vertex>(
    val alpha: Double = 1.0,
    val epsilon: Double = 1e-6
) : SolutionInitializer<Tour<V>, TSP<V>> {

    override fun getInitial(p: TSP<V>): Tour<V> {
        val n = p.vertexes.size
        val startIndex = Random.Default.nextInt(n)
        val visited = BooleanArray(n)
        val tour = mutableListOf<V>()

        var current = p.vertexes[startIndex]
        visited[current.id] = true
        tour.add(current)

        repeat(n - 1) {
            val candidates = p.vertexes.filter { !visited[it.id] }
            val weights = DoubleArray(candidates.size) { idx ->
                1.0 / (p.distance(current, candidates[idx]).pow(alpha) + epsilon)
            }
            val totalWeight = weights.sum()
            var r = Random.Default.nextDouble() * totalWeight
            var selected = candidates.last()
            for (i in candidates.indices) {
                r -= weights[i]
                if (r <= 0.0) {
                    selected = candidates[i]
                    break
                }
            }
            current = selected
            visited[current.id] = true
            tour.add(current)
        }
        return Tour(tour)
    }
}