package org.example.strategies

import org.example.model.Problem
import org.example.model.Solution
import org.example.operators.population.PopulationManager
import org.example.strategies.evolution.EvolutionCycle

class ModularSolver<S : Solution, P : Problem<S>>(
    private val populationManager: PopulationManager<S, P>,
    private val evolutionCycle: EvolutionCycle<S, P>,
    private val maxGenerations: Int,
    private val maxStagnation: Int,
    private val callback: ((generation: Int, population: List<S>, bestFitness: Double) -> Unit)? = null
) : Solver<S, P> {

    override fun solve(p: P): S {
        var best = populationManager.getBest(p)
        var bestFitness = p.fitness(best)
        var stagnation = 0

        for (gen in 0 until maxGenerations) {
            populationManager.evolve(evolutionCycle, p)

            val currentBest = populationManager.getBest(p)
            val currentFitness = p.fitness(currentBest)
            if (currentFitness < bestFitness) {
                best = currentBest
                bestFitness = currentFitness
                stagnation = 0
            } else {
                stagnation++
            }

            callback?.invoke(gen, populationManager.getAll(), p.fitness(populationManager.getBest(p)))

            if (stagnation >= maxStagnation) break
        }
        return best
    }
}