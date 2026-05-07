package org.example.strategies.concrete

import org.example.experiments.MetricsCallback
import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex
import org.example.operators.crossover.Crossover
import org.example.operators.localsearch.HillClimbing2Opt
import org.example.operators.mutation.Mutation
import org.example.operators.population.PopulationManager
import org.example.operators.selection.Selection
import org.example.strategies.common.Solver
import kotlin.random.Random

class BasicSolver<V : Vertex>(
    private val populationManager: PopulationManager<V>,
    private val selection: Selection,
    private val crossover: Crossover,
    private val mutation: Mutation?,
    private val localSearch: HillClimbing2Opt?,
    private val mutationRate: Double,
    private val localSearchProbability: Double,
    private val maxGenerations: Int,
    private val maxStagnation: Int = 50,
    private val callback: MetricsCallback<V>? = null
) : Solver<V> {

    override fun solve(g: Graph<V>): Tour<V> {
        populationManager.initialize(g)

        var best = populationManager.getBest()
        var bestFitness = g.fitness(best)
        var generationsWithoutImprovement = 0

        for (gen in 0 until maxGenerations) {
            val parent1 = selection.select(populationManager.getAll(), g)
            val parent2 = selection.select(populationManager.getAll(), g)

            val children = crossover.merge(parent1, parent2)

            for (child in children) {
                var offspring = child

                if (mutation != null && Random.nextDouble() < mutationRate) {
                    offspring = mutation.mutate(offspring, mutationRate)
                }

                if (localSearch != null && Random.nextDouble() < localSearchProbability) {
                    offspring = localSearch.improve(offspring, g)
                }

                populationManager.addOffspring(offspring)

                val offspringFitness = g.fitness(offspring)
                if (offspringFitness < bestFitness) {
                    best = offspring
                    bestFitness = offspringFitness
                    generationsWithoutImprovement = 0
                    println("Generation $gen: improved to $bestFitness")
                }
            }

            callback?.onGeneration(
                generation = gen,
                population = populationManager.getAll(),
                fitnesses = populationManager.getAll().map { g.fitness(it) },
                bestFitness = bestFitness,
                averageFitness = populationManager.getAll().map { g.fitness(it) }.average()
            )

            populationManager.afterGeneration()

            generationsWithoutImprovement++
            if (generationsWithoutImprovement >= maxStagnation) {
                println("Stopped at generation $gen due to stagnation.")
                break
            }
        }

        return best
    }
}