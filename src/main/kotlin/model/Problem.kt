package org.example.model

interface Problem<S : Solution> {
    fun fitness(solution: S): Double
}