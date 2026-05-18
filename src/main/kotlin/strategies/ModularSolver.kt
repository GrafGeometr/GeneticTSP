package org.example.strategies

import org.example.model.Problem
import org.example.model.Solution
import org.example.operators.population.PopulationManager
import org.example.operators.postprocessing.PostProcessing
import org.example.strategies.evolution.EvolutionCycle

class ModularSolver<S : Solution, P : Problem<S>>(
    private val populationManager: PopulationManager<S, P>,
    private val evolutionCycle: EvolutionCycle<S, P>,
    private val maxGenerations: Int,
    private val maxStagnation: Int,
    private val noHopeThreshold: Int = 100,
    private val rehopeMutation: PostProcessing<S, P>,
    private val callback: ((generation: Int, population: List<S>, bestFitness: Double) -> Unit)? = null
) : Solver<S, P> {

    override suspend fun solve(p: P): S {
        var best = populationManager.getBest(p)
        var bestFitness = p.fitness(best)
        var stagnation = 0
        var prevFitness = p.fitness(best)

        for (gen in 0 until maxGenerations) {
            populationManager.evolve(evolutionCycle, p)

            val currentBest = populationManager.getBest(p)
            val currentFitness = p.fitness(currentBest)

            if (currentFitness < bestFitness) {
                best = currentBest
                bestFitness = currentFitness
            }
            if (currentFitness < prevFitness) {
                stagnation = 0
            } else {
                stagnation++
                if (noHopeThreshold > 0 && stagnation >= noHopeThreshold) {
                    println("Rehope")
                    populationManager.rehope(p, rehopeMutation)
                    stagnation = 0
                }
            }

            prevFitness = currentFitness

            callback?.invoke(gen, populationManager.getAll(), p.fitness(populationManager.getBest(p)))

            if (stagnation >= maxStagnation) break
        }
        return best
    }
}