package org.example.model

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.operators.postprocessing.PostProcessing


class Population<S : Solution, P : Problem<S>>(
    val capacity: Int,
    initialIndividuals: List<S> = emptyList()
) {
    private val individuals: MutableList<S> = initialIndividuals.toMutableList()
    private val personalBests: MutableList<S> = mutableListOf()

    val size: Int get() = individuals.size

    fun getAll(): List<S> = individuals.toList()

    fun getBest(problem: P): S? = individuals.minByOrNull { problem.fitness(it) }

    fun add(individual: S) {
        require(individuals.size < capacity) { "Population is full" }
        individuals.add(individual)
    }

    fun replaceWorst(offspring: S, problem: P): Boolean {
        if (individuals.isEmpty()) return false
        val worstIdx = individuals.indices.maxByOrNull { problem.fitness(individuals[it]) } ?: return false
        if (problem.fitness(offspring) < problem.fitness(individuals[worstIdx])) {
            individuals[worstIdx] = offspring
            return true
        }
        return false
    }

    fun replaceAll(newIndividuals: List<S>) {
        individuals.clear()
        individuals.addAll(newIndividuals.take(capacity))
    }

    suspend fun initializeRandom(problem: P, solutionGenerator: suspend (P) -> S) {
        individuals.clear()
        coroutineScope {
            val jobs = List(capacity) {
                async { solutionGenerator(problem) }
            }
            individuals.addAll(jobs.awaitAll())
        }
    }

    fun initPersonalBests() {
        personalBests.clear()
        for (ind in individuals) {
            personalBests.add(copySolution(ind))
        }
    }

    fun rehope(problem: P, mutation: PostProcessing<S, P>) {
        if (personalBests.isEmpty()) {
            initPersonalBests()
        }
        for (i in individuals.indices) {
            val current = individuals[i]
            val pbest = personalBests[i]
            if (problem.fitness(current) < problem.fitness(pbest)) {
                personalBests[i] = copySolution(current)
            }
        }
        val newIndividuals = personalBests.map { mutation.doSomething(it, problem) }
        replaceAll(newIndividuals)
    }

    private fun copySolution(solution: S): S {
        return when (solution) {
            is Tour<*> -> {
                @Suppress("UNCHECKED_CAST")
                Tour(solution.list.toList()) as S
            }
            else -> solution
        }
    }
}