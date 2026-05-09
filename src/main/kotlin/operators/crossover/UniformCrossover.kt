package org.example.operators.crossover

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class UniformCrossover<V : Vertex> : Crossover<Tour<V>, TSP<V>> {
    override fun merge(a: Tour<V>, b: Tour<V>, p: TSP<V>): List<Tour<V>> {
        val n = a.list.size

        val mask = BooleanArray(n) { Random.nextBoolean() }

        val child1 = buildChild(a, b, mask)

        val child2 = buildChild(a, b, mask.map { !it }.toBooleanArray())

        return listOf(child1, child2)
    }

    fun <V : Vertex> buildChild(a: Tour<V>, b: Tour<V>, mask: BooleanArray): Tour<V> {
        data class Source(val fromFirst: Boolean, val idx: Int)

        val n = mask.size
        val childIndexes = arrayOfNulls<Source>(n)

        val used = mutableSetOf<Int>()

        for (i in 0 until n) {
            val preferred = if (mask[i]) a.list[i] else b.list[i]
            if (preferred.id !in used) {
                childIndexes[i] = Source(mask[i], i)
                used.add(preferred.id)
            } else {
                val alternative = if (mask[i]) b.list[i] else a.list[i]
                if (alternative.id !in used) {
                    childIndexes[i] = Source(!mask[i], i)
                    used.add(alternative.id)
                }
            }
        }

        val remaining = (a.list.mapIndexed { idx, v -> Triple(idx, true, v) } + b.list.mapIndexed { idx, v ->
            Triple(
                idx,
                false,
                v
            )
        }).distinctBy { it.third.id }.filter { it.third.id !in used }.toMutableList()
        for (i in 0 until n) {
            if (childIndexes[i] == null) {
                val elem = remaining.removeAt(0)
                childIndexes[i] = Source(elem.second, elem.first)
                used.add(elem.third.id)
            }
        }

        return Tour(childIndexes.map { if (it!!.fromFirst) a.list[it.idx] else b.list[it.idx] })
    }
}