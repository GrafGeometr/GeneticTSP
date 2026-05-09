package org.example.operators.population

import org.example.model.Problem
import org.example.model.Solution
import org.example.strategies.evolution.EvolutionCycle

interface PopulationManager<S : Solution, P : Problem<S>> {
    fun getAll(): List<S>
    fun getBest(p: P): S
    val size: Int

    fun evolve(cycle: EvolutionCycle<S, P>, p: P)
}