package org.example.operators.selection

import org.example.model.Problem
import org.example.model.Solution
import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex

class SimpleTournamentSelection<S : Solution, P : Problem<S>>(val tournamentSize: Int = 2) : Selection<S, P> {
    fun selectOne(population: List<S>, p: P): S {
        val candidates = (1..tournamentSize).map { population.random() }
        return candidates.minBy { p.fitness(it) }
    }

    override fun select(population: List<S>, p: P): List<Pair<S, S>> {
        val n = population.size
        return (1..n / 2).map { selectOne(population, p) to selectOne(population, p) }
    }
}