package org.example

import org.example.experiments.AlgorithmConfig
import org.example.experiments.DatasetInfo
import org.example.experiments.ExperimentRunner
import org.example.experiments.base
import org.example.experiments.datasets
import org.example.experiments.presets
import org.example.model.Point
import org.example.operators.crossover.OXCrossover
import org.example.operators.initial.GreedyTour
import org.example.operators.population.IslandPopulationManager
import org.example.operators.postprocessing.localsearch.HillClimbing2Opt
import org.example.operators.postprocessing.mutation.ScrambleMutation
import org.example.operators.selection.UnbiasedTournamentSelection
import org.example.strategies.evolution.SteadyStateCycle

fun main() {
//    ExperimentRunner.run(presets, datasets)

    ExperimentRunner.run(
        listOf(
            AlgorithmConfig(
                name = "Example",
                populationManagerFactory = { p ->
                    IslandPopulationManager(
                        islandCount = 5,
                        islandCapacity = 20,
                        problem = p,
                        generator = { GreedyTour<Point>().getInitial(p) },
                        migrationRate = 0.1,
                        elitesPerMigration = 2,
                    )
                },
                evolutionCycleFactory = {
                    SteadyStateCycle(
                        selection = UnbiasedTournamentSelection(),
                        crossover = OXCrossover(),
                        postProcessing = ScrambleMutation<Point>().then(
                            HillClimbing2Opt<Point>(HillClimbing2Opt.Mode.First).withProbability(
                                0.01
                            )
                        ),
                        offspringCount = 5
                    )
                },
                maxGenerations = 1000,
                maxStagnation = Int.MAX_VALUE,
                repeats = 1,
                csvLogRate = 100,
                consoleLogRate = 100,
                logImprovements = true
            )
        ), listOf(
            DatasetInfo(
                name = "tsp_574_1", path = "data/tsp_574_1"
            )
        )
    )
}