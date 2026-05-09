package org.example.experiments

import ExperimentConfig
import kotlinx.coroutines.*
import org.example.load.GraphLoader
import org.example.model.Point
import org.example.model.Tour
import org.example.strategies.ModularSolver
import java.io.File

object ExperimentRunner {
    fun run(configs: List<ExperimentConfig>) = runBlocking {
        val resultsDir = File("results")
        if (!resultsDir.exists()) resultsDir.mkdirs()
        configs.forEach { File(resultsDir, it.name).mkdirs() }

        val jobs = configs.map { cfg ->
            async(Dispatchers.Default) {
                // ExperimentRunner.kt (фрагмент)

                val runJobs = (0 until cfg.repeats).map { runIdx ->
                    async(Dispatchers.Default) {
                        val problem = GraphLoader.loadPointsFromResource(cfg.datasetPath)
                        val pm = cfg.populationManagerFactory(problem)
                        val collector = MetricsCollector<Point>(
                            File(resultsDir, "${cfg.name}/run_$runIdx.csv").path,
                            loggingRate = cfg.loggingRate
                        )

                        var lastBest = Double.MAX_VALUE
                        val logInterval = 100

                        val compositeCallback: (Int, List<Tour<Point>>, Double) -> Unit = { gen, pop, bestFit ->
                            val tours = pop
                            val fits = tours.map { problem.fitness(it) }
                            collector.onGeneration(gen, tours, fits, bestFit, fits.average())

                            if (bestFit < lastBest) {
                                println("${cfg.name} run $runIdx | gen $gen : new best = $bestFit")
                                lastBest = bestFit
                            } else if (gen % logInterval == 0) {
                                println("${cfg.name} run $runIdx | gen $gen : best = $bestFit (no improvement)")
                            }
                        }

                        val solver = ModularSolver(
                            populationManager = pm,
                            evolutionCycle = cfg.evolutionCycleFactory(),
                            maxGenerations = cfg.maxGenerations,
                            maxStagnation = cfg.maxStagnation,
                            callback = compositeCallback
                        )
                        solver.solve(problem)
                        collector.close()
                        println("${cfg.name} run $runIdx completed")
                    }
                }
                runJobs.awaitAll()
                println("${cfg.name} all runs completed")
            }
        }
        jobs.awaitAll()
        println("All experiments finished.")
    }
}