package org.example.operators.mutation

import org.example.model.Tour
import org.example.model.Vertex

interface Mutation {
    fun <V : Vertex> mutate(tour: Tour<V>, mutationRate: Double): Tour<V>
}