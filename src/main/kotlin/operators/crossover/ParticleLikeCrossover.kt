package org.example.operators.crossover

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class ParticleLikeCrossover<V : Vertex> : Crossover<Tour<V>, TSP<V>> {
    fun getOneChild(a: Tour<V>, b: Tour<V>): Tour<V> {
        val n = a.list.size

        val sortedA = DoubleArray(n) { Random.nextDouble() }.apply { sort() }
        val sortedB = DoubleArray(n) { Random.nextDouble() }.apply { sort() }

        val posA = mutableMapOf<V, Double>()
        for (i in 0 until n) posA[a.list[i]] = sortedA[i]

        val posB = mutableMapOf<V, Double>()
        for (i in 0 until n) posB[b.list[i]] = sortedB[i]


        val vertices = a.list
        val meanValues = vertices.map { v ->
            v to (posA[v]!! + posB[v]!!) / 2.0
        }

        val sortedVertices = meanValues
            .sortedWith(compareBy({ it.second }, { it.first.id }))
            .map { it.first }

        return Tour(sortedVertices)
    }

    override fun merge(a: Tour<V>, b: Tour<V>, p: TSP<V>): List<Tour<V>> {
        return listOf(getOneChild(a, b), getOneChild(a, b))
    }
}