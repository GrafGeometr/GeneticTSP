package org.example.operators.crossover

import org.example.model.Problem
import org.example.model.Solution

interface Crossover<S : Solution, P : Problem<S>> {
    fun merge(a: S, b: S, p: P): List<S>
}