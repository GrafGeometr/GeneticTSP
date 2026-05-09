package org.example.operators.population

import kotlin.random.Random
import org.example.model.Population
import org.example.model.Problem
import org.example.model.Solution
import org.example.strategies.evolution.EvolutionCycle

class IslandPopulationManager<S : Solution, P : Problem<S>>(
    private val islands: List<Population<S, P>>,
    private val migrationRate: Double = 0.1,
    private val elitesPerMigration: Int = 2,
) : PopulationManager<S, P> {

    constructor(
        islandCount: Int,
        islandCapacity: Int,
        problem: P,
        generator: (P) -> S,
        migrationRate: Double = 0.1,
        elitesPerMigration: Int = 2
    ) : this(
        islands = (0 until islandCount).map {
            Population<S, P>(islandCapacity).also { pop ->
                pop.initializeRandom(problem, generator)
            }
        },
        migrationRate = migrationRate,
        elitesPerMigration = elitesPerMigration
    )

    override fun getAll(): List<S> = islands.flatMap { it.getAll() }

    override fun getBest(p: P): S = islands
        .mapNotNull { it.getBest(p) }
        .minByOrNull { p.fitness(it) }
        ?: throw IllegalStateException("Все острова пусты")

    override val size: Int get() = islands.sumOf { it.size }

    override fun evolve(cycle: EvolutionCycle<S, P>, p: P) {
        islands.forEach { cycle.execute(it, p) }

        if (Random.nextDouble() >= migrationRate) return

        for (i in islands.indices) {
            val from = islands[i]
            val to = islands[(i + 1) % islands.size]
            val migrants = from.getAll()
                .sortedBy { p.fitness(it) }
                .take(elitesPerMigration)
            migrants.forEach { elite -> to.replaceWorst(elite, p) }
        }
    }
}