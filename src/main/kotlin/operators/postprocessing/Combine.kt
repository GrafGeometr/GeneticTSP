package org.example.operators.postprocessing

import org.example.model.Problem
import org.example.model.Solution
import org.example.model.Tour

class Combine<S : Solution, P : Problem<S>>(
    private val op1: PostProcessing<S, P>,
    private val op2: PostProcessing<S, P>
) : PostProcessing<S, P> {
    override fun doSomething(solution: S, p: P): S = op2.doSomething(op1.doSomething(solution, p), p)
}