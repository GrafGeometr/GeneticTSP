package org.example.operators.postprocessing

import org.example.model.Problem
import org.example.model.Solution
import org.example.model.Tour
import org.example.model.Vertex

interface PostProcessing<S : Solution, P : Problem<S>> {
    fun doSomething(solution: S, p: P): S

    fun then(other: PostProcessing<S, P>): PostProcessing<S, P> = Combine(this, other)

    fun withProbability(probability: Double): PostProcessing<S, P> = Probably(this, probability)
}