package org.example.operators.population

import org.example.model.Graph
import org.example.model.Tour
import org.example.model.Vertex

class SimplePopulationManager<V : Vertex>(
    val populationSize: Int,
    private val elitism: Boolean = true
) : PopulationManager<V> {

    private val population = mutableListOf<Tour<V>>()
    private lateinit var graph: Graph<V>

    override fun initialize(graph: Graph<V>) {
        this.graph = graph
        population.clear()
        repeat(populationSize) {
            population.add(Tour.random(graph))
        }
    }

    override fun addOffspring(offspring: Tour<V>): Boolean {
        val fitnessOffspring = graph.fitness(offspring)

        if (population.size < populationSize) {
            population.add(offspring)
            return true
        }

        val worstIdx = population.indices.maxByOrNull { graph.fitness(population[it]) } ?: return false
        val worstFitness = graph.fitness(population[worstIdx])

        if (fitnessOffspring >= worstFitness) return false

        population[worstIdx] = offspring
        return true
    }

    override fun getAll(): List<Tour<V>> = population

    override fun getBest(): Tour<V> {
        return population.minByOrNull { graph.fitness(it) }!!
    }

    override val size: Int get() = population.size
}