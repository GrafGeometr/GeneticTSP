package org.example.operators.postprocessing.localsearch

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class TryHillClimbing2Opt<V : Vertex>(val iters: Int = 1) : LocalSearch<V> {

    override fun improve(tour: Tour<V>, p: TSP<V>): Tour<V> {
        var current = tour
        for (attempt in 0 until iters) {
            val next = tryOneRandom2Opt(current, p)
            if (next != null) {
                current = next
            } else {
                break
            }
        }
        return current
    }

    private fun tryOneRandom2Opt(tour: Tour<V>, p: TSP<V>): Tour<V>? {
        val n = tour.list.size
        if (n < 2) return null

        val i = Random.nextInt(n)
        val j = Random.nextInt(n)

        if (i == j || j == (i + 1) % n || i == (j + 1) % n) return null

        val a = tour.at(i)
        val b = tour.at(i + 1)
        val c = tour.at(j)
        val d = tour.at(j + 1)

        val oldDist = p.distance(a, b) + p.distance(c, d)
        val newDist = p.distance(a, c) + p.distance(b, d)
        val improvement = oldDist - newDist

        if (improvement <= 0.0) return null

        val start = minOf(i, j)
        val end = maxOf(i, j)

        val newList = mutableListOf<V>()
        for (k in 0..start) {
            newList.add(tour.at(k))
        }
        for (k in end downTo start + 1) {
            newList.add(tour.at(k))
        }
        for (k in end + 1 until n) {
            newList.add(tour.at(k))
        }

        return Tour(newList)
    }
}