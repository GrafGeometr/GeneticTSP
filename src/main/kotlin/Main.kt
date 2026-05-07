package org.example

import ExperimentConfig
import org.example.experiments.ExperimentRunner
import org.example.model.Point
import org.example.operators.crossover.OXCrossover
import org.example.operators.crossover.UniformCrossover
import org.example.operators.localsearch.HillClimbing2Opt
import org.example.operators.mutation.InversionMutation
import org.example.operators.mutation.SwapMutation
import org.example.operators.population.IslandPopulationManager
import org.example.operators.population.SimplePopulationManager
import org.example.operators.selection.SimpleTournamentSelection


fun main() {
    val configs = listOf(
//        ExperimentConfig(
//            name = "GA_OX_Swap_Local",
//            datasetPath = "data/tsp_574_1",
//            populationManagerFactory = { SimplePopulationManager<Point>(populationSize = 100, elitism = true) },
//            selection = SimpleTournamentSelection(2),
//            crossover = OXCrossover(),
//            mutation = SwapMutation(),
//            localSearch = HillClimbing2Opt(HillClimbing2Opt.Mode.First),
//            mutationRate = 0.1,
//            localSearchProbability = 0.1,
//            maxGenerations = 1000,
//            maxStagnation = Int.MAX_VALUE,
//            repeats = 5
//        ),
//        ExperimentConfig(
//            name = "GA_OX_Swap_Local_Island",
//            datasetPath = "data/tsp_574_1",
//            populationManagerFactory = { IslandPopulationManager<Point>(totalPopulationSize = 300, numberOfIslands = 5, migrationRate = 0.1, elitesPerMigration = 2) },
//            selection = SimpleTournamentSelection(2),
//            crossover = OXCrossover(),
//            mutation = SwapMutation(),
//            localSearch = HillClimbing2Opt(HillClimbing2Opt.Mode.First),
//            mutationRate = 0.1,
//            localSearchProbability = 0.1,
//            maxGenerations = 1000,
//            maxStagnation = Int.MAX_VALUE,
//            repeats = 5
//        ),
//        ExperimentConfig(
//            name = "GA_Uniform_Inv_Local_Island",
//            datasetPath = "data/tsp_574_1",
//            populationManagerFactory = { IslandPopulationManager<Point>(totalPopulationSize = 300, numberOfIslands = 5, migrationRate = 0.1, elitesPerMigration = 2) },
//            selection = SimpleTournamentSelection(2),
//            crossover = UniformCrossover(),
//            mutation = InversionMutation(),
//            localSearch = HillClimbing2Opt(HillClimbing2Opt.Mode.First),
//            mutationRate = 0.1,
//            localSearchProbability = 0.1,
//            maxGenerations = 1000,
//            maxStagnation = Int.MAX_VALUE,
//            repeats = 5
//        )
        ExperimentConfig(
            name = "GA_OX_Swap_LowLocal_Island",
            datasetPath = "data/tsp_574_1",
            populationManagerFactory = { IslandPopulationManager<Point>(totalPopulationSize = 300, numberOfIslands = 5, migrationRate = 0.1, elitesPerMigration = 2) },
            selection = SimpleTournamentSelection(2),
            crossover = OXCrossover(),
            mutation = SwapMutation(),
            localSearch = HillClimbing2Opt(HillClimbing2Opt.Mode.First),
            mutationRate = 0.1,
            localSearchProbability = 0.01,
            maxGenerations = 1000,
            maxStagnation = Int.MAX_VALUE,
            repeats = 5
        ),
    )

    ExperimentRunner.run(configs)
}