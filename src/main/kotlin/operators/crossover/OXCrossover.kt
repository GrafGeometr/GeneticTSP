package org.example.operators.crossover

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class OXCrossover<V : Vertex> : Crossover<Tour<V>, TSP<V>> {
    override fun merge(a: Tour<V>, b: Tour<V>, p: TSP<V>): List<Tour<V>> {
        val n = a.list.size
        val cut1 = Random.nextInt(n)
        val cut2 = Random.nextInt(n)
        val start = minOf(cut1, cut2)
        val end = maxOf(cut1, cut2)

        val child1 = buildChild(a, b, start, end)
        val child2 = buildChild(b, a, start, end)

        return listOf(child1, child2)
    }

    private fun <V : Vertex> buildChild(segParent: Tour<V>, fillParent: Tour<V>, start: Int, end: Int): Tour<V> {
        val n = segParent.list.size
        data class Source(val fromFirst: Boolean, val idx: Int)

        val childMap = arrayOfNulls<Source>(n)
        val used = mutableSetOf<Int>()

        for (i in start..end) {
            childMap[i] = Source(true, i)
            used.add(segParent.list[i].id)
        }

        var idx = (end + 1) % n
        var p2 = (end + 1) % n
        while (idx != start) {
            val gene = fillParent.list[p2]
            if (gene.id !in used) {
                childMap[idx] = Source(false, p2)
                used.add(gene.id)
                idx = (idx + 1) % n
            }
            p2 = (p2 + 1) % n
        }

        val childList = childMap.map { source ->
            if (source!!.fromFirst) segParent.list[source.idx] else fillParent.list[source.idx]
        }
        return Tour(childList)
    }
}