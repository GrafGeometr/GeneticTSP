package org.example.strategies

import org.example.model.Problem
import org.example.model.Solution
import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex

interface Solver<S : Solution, P : Problem<S>> {
    suspend fun solve(p: P): S
}