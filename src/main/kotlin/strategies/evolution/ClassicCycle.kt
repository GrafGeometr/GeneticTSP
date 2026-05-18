package org.example.strategies.evolution

import kotlinx.coroutines.*
import org.example.model.Population
import org.example.model.Problem
import org.example.model.Solution
import org.example.operators.crossover.Crossover
import org.example.operators.postprocessing.PostProcessing
import org.example.operators.selection.Selection

class ClassicCycle<S : Solution, P : Problem<S>>(
    private val selection: Selection<S, P>,
    private val crossover: Crossover<S, P>,
    private val postProcessing: PostProcessing<S, P>? = null,
    private val elitismCount: Int = 2
) : EvolutionCycle<S, P> {
    override suspend fun execute(population: Population<S, P>, problem: P) {
        val oldPop = population.getAll()
        val capacity = population.capacity
        val newPop = mutableListOf<S>()
        newPop.addAll(oldPop.sortedBy { problem.fitness(it) }.take(elitismCount))

        val parentPairs = selection.select(oldPop, problem)

        coroutineScope {
            val jobs = parentPairs.map { (p1, p2) ->
                async(Dispatchers.Default) {
                    val children = crossover.merge(p1, p2, problem)
                    children.map { child ->
                        postProcessing?.doSomething(child, problem) ?: child
                    }
                }
            }
            val allChildren = jobs.awaitAll().flatten()
            for (child in allChildren) {
                if (newPop.size >= capacity) break
                newPop.add(child)
            }
        }
        population.replaceAll(newPop)
    }
}