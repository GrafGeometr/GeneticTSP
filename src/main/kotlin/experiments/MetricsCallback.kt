package org.example.experiments

import org.example.model.Tour
import org.example.model.Vertex

interface MetricsCallback<V : Vertex> {
    fun onGeneration(
        generation: Int,
        population: List<Tour<V>>,
        fitnesses: List<Double>,
        bestFitness: Double,
        averageFitness: Double
    )
}