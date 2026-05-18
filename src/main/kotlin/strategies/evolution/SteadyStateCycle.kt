package org.example.strategies.evolution

import kotlinx.coroutines.*
import org.example.model.Population
import org.example.model.Problem
import org.example.model.Solution
import org.example.operators.crossover.Crossover
import org.example.operators.postprocessing.PostProcessing
import org.example.operators.selection.Selection
import java.util.concurrent.atomic.AtomicInteger

class SteadyStateCycle<S : Solution, P : Problem<S>>(
    private val selection: Selection<S, P>,
    private val crossover: Crossover<S, P>,
    private val postProcessing: PostProcessing<S, P>? = null,
    private val offspringCount: Int = 2
) : EvolutionCycle<S, P> {
    override suspend fun execute(population: Population<S, P>, problem: P) {
        val popList = population.getAll()
        if (popList.size < 2) return
        val parentPairs = selection.select(popList, problem)
        val generated = AtomicInteger(0)

        coroutineScope {
            val jobs = parentPairs.map { (p1, p2) ->
                async(Dispatchers.Default) {
                    if (generated.get() >= offspringCount) return@async
                    val children = crossover.merge(p1, p2, problem)
                    for (child in children) {
                        if (generated.get() >= offspringCount) break
                        val finalChild = postProcessing?.doSomething(child, problem) ?: child
                        val count = generated.incrementAndGet()
                        if (count <= offspringCount) {
                            synchronized(population) {
                                population.replaceWorst(finalChild, problem)
                            }
                        } else {
                            break
                        }
                    }
                }
            }
            jobs.awaitAll()
        }
    }
}