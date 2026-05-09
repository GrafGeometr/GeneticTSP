package org.example.operators.initial

import org.example.model.Problem
import org.example.model.Solution
import org.example.model.Vertex

interface SolutionInitializer<S : Solution, P : Problem<S>> {
    fun getInitial(p: P): S
}