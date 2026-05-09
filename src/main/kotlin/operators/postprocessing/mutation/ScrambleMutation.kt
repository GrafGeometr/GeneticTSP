package org.example.operators.postprocessing.mutation

import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random


class ScrambleMutation<V : Vertex> : Mutation<V> {
    override fun mutate(tour: Tour<V>): Tour<V> {
        val n = tour.list.size
        if (n < 2) return tour

        val i = Random.nextInt(n)
        val j = Random.nextInt(n)
        val start = minOf(i, j)
        val end = maxOf(i, j)

        val result = tour.list.toMutableList()
        val sub = result.subList(start, end + 1)
        val shuffled = sub.shuffled(Random)

        for (idx in sub.indices) {
            sub[idx] = shuffled[idx]
        }

        return Tour(result)
    }
}