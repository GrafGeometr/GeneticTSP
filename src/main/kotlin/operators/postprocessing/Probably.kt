package org.example.operators.postprocessing

import org.example.model.Problem
import org.example.model.Solution
import kotlin.random.Random

class Probably<S : Solution, P : Problem<S>>(
    val op: PostProcessing<S, P>, val probability: Double
) : PostProcessing<S, P> {
    override fun doSomething(solution: S, p: P): S =
        if (Random.Default.nextDouble() < probability) op.doSomething(solution, p) else solution
}