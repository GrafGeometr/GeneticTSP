package org.example.operators.crossover

import org.example.model.Tour
import org.example.model.Vertex

interface Crossover {
    fun <V : Vertex> merge(a: Tour<V>, b: Tour<V>): List<Tour<V>>
}