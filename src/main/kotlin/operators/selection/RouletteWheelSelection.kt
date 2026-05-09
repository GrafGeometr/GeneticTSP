package org.example.operators.selection

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import kotlin.random.Random

class RouletteWheelSelection<V : Vertex> : Selection<Tour<V>, TSP<V>> {
    override fun select(population: List<Tour<V>>, p: TSP<V>): List<Pair<Tour<V>, Tour<V>>> {
        if (population.size < 2) return emptyList()


        val fitnesses = population.map { 1.0 / p.fitness(it) }
        val totalFitness = fitnesses.sum()

        val cumulative = DoubleArray(population.size)
        var acc = 0.0
        for (i in population.indices) {
            acc += fitnesses[i] / totalFitness
            cumulative[i] = acc
        }

        fun selectOne(): Tour<V> {
            val r = Random.nextDouble()
            var idx = cumulative.indexOfFirst { it >= r }
            if (idx == -1) idx = population.size - 1
            return population[idx]
        }

        val pairsCount = population.size / 2
        return (1..pairsCount).map {
            val parent1 = selectOne()
            val parent2 = selectOne()
            parent1 to parent2
        }
    }
}