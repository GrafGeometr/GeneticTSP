package org.example.operators.population

import org.example.model.Problem
import org.example.model.Solution
import org.example.operators.postprocessing.PostProcessing
import org.example.strategies.evolution.EvolutionCycle

interface PopulationManager<S : Solution, P : Problem<S>> {
    suspend fun initialize()
    fun getAll(): List<S>
    fun getBest(p: P): S
    val size: Int

    suspend fun evolve(cycle: EvolutionCycle<S, P>, p: P)

    fun rehope(p: P, mutation: PostProcessing<S, P>) {}
}