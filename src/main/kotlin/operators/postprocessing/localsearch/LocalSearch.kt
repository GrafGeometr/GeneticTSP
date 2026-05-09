package org.example.operators.postprocessing.localsearch

import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Vertex
import org.example.operators.postprocessing.PostProcessing

interface LocalSearch<V : Vertex> : PostProcessing<Tour<V>, TSP<V>> {
    fun improve(tour: Tour<V>, p: TSP<V>): Tour<V>

    override fun doSomething(solution: Tour<V>, p: TSP<V>): Tour<V> = improve(solution, p)
}