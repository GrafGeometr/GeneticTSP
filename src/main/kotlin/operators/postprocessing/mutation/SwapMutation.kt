package org.example.operators.postprocessing.mutation

import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class SwapMutation<V : Vertex>(val mutationRate: Double) : Mutation<V> {
    override fun mutate(tour: Tour<V>): Tour<V> {
        val n = tour.list.size
        val mutations = (mutationRate * n / 2.0).toInt()
        val result = tour.list.toMutableList()

        repeat(mutations) {
            val i = Random.nextInt(n)
            val j = Random.nextInt(n)
            val tmp = result[i]
            result[i] = result[j]
            result[j] = tmp
        }

        return Tour(result)
    }
}