package org.example.strategies

import org.example.model.Problem
import org.example.model.Solution
import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex

interface SolverData<S : Solution, P : Problem<S>> {
    fun getBest(): S
}

interface IterativeSolver<S : Solution, P : Problem<S>, D : SolverData<S, P>> : Solver<S, P> {
    fun oneIteration(data: D): D

    fun init(p: P): D

    fun stopCriteria(data: D): Boolean

    override fun solve(p: P): S {
        var data = init(p)

        while (!stopCriteria(data)) {
            data = oneIteration(data)
        }

        return data.getBest()
    }
}