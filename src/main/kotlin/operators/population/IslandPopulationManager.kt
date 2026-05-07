package org.example.operators.population

import kotlin.random.Random
import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

class IslandPopulationManager<V : Vertex>(
    val totalPopulationSize: Int,
    val numberOfIslands: Int,
    val migrationRate: Double = 0.1,
    val elitesPerMigration: Int = 2
) : PopulationManager<V> {

    private val islands: List<SimplePopulationManager<V>>
    private val islandSize: Int = totalPopulationSize / numberOfIslands
    private lateinit var graph: Graph<V>

    init {
        require(numberOfIslands > 1) { "Островная модель требует минимум 2 острова" }
        islands = List(numberOfIslands) {
            SimplePopulationManager<V>(populationSize = islandSize, elitism = true)
        }
    }

    override fun initialize(graph: Graph<V>) {
        this.graph = graph
        islands.forEach { it.initialize(graph) }
    }

    override fun addOffspring(offspring: Tour<V>): Boolean {
        val targetIsland = islands.random()
        return targetIsland.addOffspring(offspring)
    }

    override fun getAll(): List<Tour<V>> {
        return islands.flatMap { it.getAll() }
    }

    override fun getBest(): Tour<V> {
        return islands.map { it.getBest() }.minByOrNull { graph.fitness(it) }!!
    }

    override val size: Int get() = totalPopulationSize


    override fun afterGeneration() {
        if (Random.nextDouble() >= migrationRate) return

        for (i in 0 until numberOfIslands) {
            val fromIsland = islands[i]
            val toIsland = islands[(i + 1) % numberOfIslands]

            val topTours = fromIsland.getAll()
                .sortedBy { graph.fitness(it) }
                .take(elitesPerMigration)

            topTours.forEach { elite ->
                val population = toIsland.getAll()
                if (population.size >= toIsland.size) {
                    population.indices.maxByOrNull { graph.fitness(population[it]) } ?: return@forEach
                    toIsland.addOffspring(elite)
                }
            }
        }
    }
}