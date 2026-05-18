package org.example.operators.population

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import org.example.model.Population
import org.example.model.Problem
import org.example.model.Solution
import org.example.operators.postprocessing.PostProcessing
import org.example.strategies.evolution.EvolutionCycle

class IslandPopulationManager<S : Solution, P : Problem<S>>(
    private val islands: List<Population<S, P>>,
    private val migrationRate: Double = 0.1,
    private val elitesPerMigration: Int = 2,
    private val problem: P,
    private val generator: suspend (P) -> S
) : PopulationManager<S, P> {

    constructor(
        islandCount: Int,
        islandCapacity: Int,
        problem: P,
        generator: suspend (P) -> S,
        migrationRate: Double = 0.1,
        elitesPerMigration: Int = 2
    ) : this(
        islands = List(islandCount) { Population<S, P>(islandCapacity) },
        migrationRate = migrationRate,
        elitesPerMigration = elitesPerMigration,
        problem = problem,
        generator = generator
    )

    override suspend fun initialize() {
        coroutineScope {
            islands.map { island ->
                async {
                    island.initializeRandom(problem, generator)
                }
            }.awaitAll()
        }
    }

    override fun getAll(): List<S> = islands.flatMap { it.getAll() }

    override fun getBest(p: P): S = islands
        .mapNotNull { it.getBest(p) }
        .minByOrNull { p.fitness(it) }
        ?: throw IllegalStateException("Все острова пусты")

    override val size: Int get() = islands.sumOf { it.size }

    override suspend fun evolve(cycle: EvolutionCycle<S, P>, p: P) {
        coroutineScope {
            islands.map { island ->
                async(Dispatchers.Default) {
                    cycle.execute(island, p)
                }
            }.awaitAll()
        }

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

    override fun rehope(p: P, mutation: PostProcessing<S, P>) {
        islands.forEach { it.rehope(p, mutation) }
    }
}