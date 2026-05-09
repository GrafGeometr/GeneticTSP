package org.example.strategies.evolution

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
    override fun execute(population: Population<S, P>, problem: P) {
        val oldPop = population.getAll()
        val capacity = population.capacity
        val newPop = mutableListOf<S>()

        newPop.addAll(oldPop.sortedBy { problem.fitness(it) }.take(elitismCount))

        val parentPairs = selection.select(oldPop, problem)
        var pairIdx = 0
        while (newPop.size < capacity && pairIdx < parentPairs.size) {
            val (p1, p2) = parentPairs[pairIdx++]
            val children = crossover.merge(p1, p2, problem)
            for (child in children) {
                if (newPop.size >= capacity) break
                newPop.add(postProcessing?.doSomething(child, problem) ?: child)
            }
        }

        population.replaceAll(newPop)
    }
}