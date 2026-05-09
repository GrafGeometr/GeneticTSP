package org.example.operators.selection

import org.example.model.Problem
import org.example.model.Solution
import kotlin.random.Random

class UnbiasedTournamentSelection<S : Solution, P : Problem<S>> : Selection<S, P> {
    override fun select(population: List<S>, p: P): List<Pair<S, S>> {
        val n = population.size
        if (n < 2) return emptyList()

        val indices = (0 until n).flatMap { listOf(it, it) }.shuffled(Random)

        val winners = (0 until 2 * n step 2).map { i ->
            val t1 = population[indices[i]]
            val t2 = population[indices[i + 1]]
            if (p.fitness(t1) <= p.fitness(t2)) t1 else t2
        }

        val shuffledWinners = winners.shuffled(Random)

        val pairs = (0 until n step 2).map { i ->
            shuffledWinners[i] to shuffledWinners[i + 1]
        }
        return pairs
    }
}