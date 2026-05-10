package org.example.experiments

import kotlinx.coroutines.*
import org.example.load.GraphLoader
import org.example.model.Point
import org.example.model.Tour
import org.example.strategies.ModularSolver
import java.io.File

object ExperimentRunner {

    fun run(algorithms: List<AlgorithmConfig>, datasets: List<DatasetInfo>) = runBlocking {
        val resultsDir = File("results")
        if (!resultsDir.exists()) resultsDir.mkdirs()

        val configs = algorithms.flatMap { algo ->
            datasets.map { ds -> algo to ds }
        }

        val jobs = configs.map { (algo, ds) ->
            async(Dispatchers.Default) {
                val algoDir = File(resultsDir, "${ds.name}/${algo.name}")
                if (!algoDir.exists()) algoDir.mkdirs()

                val runJobs = (0 until algo.repeats).map { runIdx ->
                    async(Dispatchers.Default) {
                        val problem = GraphLoader.loadPointsFromResource(ds.path)
                        val pm = algo.populationManagerFactory(problem)

                        val collector = MetricsCollector<Point>(
                            File(algoDir, "run_$runIdx.csv").path,
                            loggingRate = algo.csvLogRate
                        )

                        var lastBest = Double.MAX_VALUE

                        val compositeCallback: (Int, List<Tour<Point>>, Double) -> Unit = { gen, pop, bestFit ->
                            val tours = pop
                            val fits = tours.map { problem.fitness(it) }
                            collector.onGeneration(gen, tours, fits, bestFit, fits.average())

                            if (algo.logImprovements && bestFit < lastBest) {
                                println("${algo.name} [${ds.name}] run $runIdx | gen $gen : new best = $bestFit")
                                lastBest = bestFit
                            } else if (!algo.logImprovements || gen % algo.consoleLogRate == 0) {
                                println("${algo.name} [${ds.name}] run $runIdx | gen $gen : best = $bestFit (no improvement)")
                            }
                        }

                        val solver = ModularSolver(
                            populationManager = pm,
                            evolutionCycle = algo.evolutionCycleFactory(),
                            maxGenerations = algo.maxGenerations,
                            maxStagnation = algo.maxStagnation,
                            callback = compositeCallback
                        )

                        val bestTour = solver.solve(problem)
                        collector.close()

                        val tourFile = File(algoDir, "best_tour_$runIdx.txt")
                        tourFile.writeText(bestTour.toString())

                        println("${algo.name} [${ds.name}] run $runIdx completed, best = ${problem.fitness(bestTour)}")
                    }
                }
                runJobs.awaitAll()
                println("${algo.name} [${ds.name}] all runs completed")
            }
        }
        jobs.awaitAll()
        println("All experiments finished.")
    }
}