package org.example.operators.population

import org.example.model.Population
import org.example.model.Problem
import org.example.model.Solution
import org.example.strategies.evolution.EvolutionCycle


class SimplePopulationManager<S : Solution, P : Problem<S>>(
    private val population: Population<S, P>
) : PopulationManager<S, P> {

    constructor(
        capacity: Int,
        problem: P,
        generator: (P) -> S
    ) : this(
        Population<S, P>(capacity).also { pop ->
            pop.initializeRandom(problem, generator)
        }
    )

    override fun getAll(): List<S> = population.getAll()

    override fun getBest(p: P): S = population.getBest(p)
        ?: throw IllegalStateException("Популяция пуста")

    override val size: Int get() = population.size

    override fun evolve(cycle: EvolutionCycle<S, P>, p: P) {
        cycle.execute(population, p)
    }
}
