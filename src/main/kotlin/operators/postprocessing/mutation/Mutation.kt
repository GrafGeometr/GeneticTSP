package org.example.operators.postprocessing.mutation

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import org.example.operators.postprocessing.PostProcessing

interface Mutation<V : Vertex> : PostProcessing<Tour<V>, TSP<V>> {
    fun mutate(tour: Tour<V>): Tour<V>

    override fun doSomething(solution: Tour<V>, p: TSP<V>): Tour<V> = mutate(solution)
}