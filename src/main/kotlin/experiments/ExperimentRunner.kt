package org.example.experiments

import ExperimentConfig
import kotlinx.coroutines.*
import org.example.load.GraphLoader
import org.example.model.Point
import org.example.strategies.concrete.BasicSolver
import java.io.File


object ExperimentRunner {
    fun run(configs: List<ExperimentConfig>) = runBlocking {
        val resultsDir = File("results")
        if (!resultsDir.exists()) resultsDir.mkdirs()

        for (cfg in configs) {
            File(resultsDir, cfg.name).mkdirs()
        }

        val configJobs = configs.map { cfg ->
            async(Dispatchers.Default) {
                val runJobs = (0 until cfg.repeats).map { runIdx ->
                    async(Dispatchers.Default) {
                        withContext(Dispatchers.Default) {
                            val graph = GraphLoader.loadPointsFromResource(cfg.datasetPath)
                            // Создаём новый экземпляр менеджера для этого запуска
                            val pm = cfg.populationManagerFactory()
                            pm.initialize(graph)

                            val collector = MetricsCollector<Point>(
                                File(resultsDir, "${cfg.name}/run_$runIdx.csv").path
                            )
                            val solver = BasicSolver(
                                populationManager = pm,
                                selection = cfg.selection,
                                crossover = cfg.crossover,
                                mutation = cfg.mutation,
                                localSearch = cfg.localSearch,
                                mutationRate = cfg.mutationRate,
                                localSearchProbability = cfg.localSearchProbability,
                                maxGenerations = cfg.maxGenerations,
                                maxStagnation = cfg.maxStagnation,
                                callback = collector
                            )
                            solver.solve(graph)
                            collector.close()
                            println("${cfg.name} run $runIdx completed")
                        }
                    }
                }
                runJobs.awaitAll()
                println("${cfg.name} all runs completed")
            }
        }
        configJobs.awaitAll()
        println("All experiments finished.")
    }
}