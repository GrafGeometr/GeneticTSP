package org.example.operators.selection

import org.example.model.Problem
import org.example.model.Solution
import kotlin.random.Random


class ParameterizedTournamentSelector<S : Solution, P : Problem<S>>(
    val sv: Double
) : Selection<S, P> {
    init {
        require(sv in 0.5..1.0) { "sv must be in [0.5, 1.0]" }
    }

    private fun selectOne(population: List<S>, p: P): S {
        val a = population.random()
        val b = population.random()
        val (better, worse) = if (p.fitness(a) <= p.fitness(b)) a to b else b to a
        return if (Random.nextDouble() < sv) better else worse
    }

    override fun select(population: List<S>, p: P): List<Pair<S, S>> {
        val n = population.size
        val pairsCount = n / 2
        return (1..pairsCount).map {
            val parent1 = selectOne(population, p)
            val parent2 = selectOne(population, p)
            parent1 to parent2
        }
    }
}