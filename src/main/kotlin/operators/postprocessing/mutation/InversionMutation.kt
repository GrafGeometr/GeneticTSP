package org.example.operators.postprocessing.mutation

import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class InversionMutation<V : Vertex>(val mutationRate: Double) : Mutation<V> {
    override fun mutate(tour: Tour<V>): Tour<V> {
        val n = tour.list.size
        val result = tour.list.toMutableList()
        val mutations = (mutationRate * n / 2.0).toInt()

        repeat(mutations) {
            val i = Random.nextInt(n)
            val j = Random.nextInt(n)
            val start = minOf(i, j)
            val end = maxOf(i, j)
            result.subList(start, end + 1).reverse()
        }

        return Tour(result)
    }
}