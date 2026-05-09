package org.example.strategies.evolution


import org.example.model.Population
import org.example.model.Problem
import org.example.model.Solution

interface EvolutionCycle<S : Solution, P : Problem<S>> {
    fun execute(population: Population<S, P>, problem: P)
}




