package org.example

import ExperimentConfig
import org.example.experiments.ExperimentRunner
import org.example.model.*
import org.example.operators.crossover.OXCrossover
import org.example.operators.crossover.UniformCrossover
import org.example.operators.initial.GreedyTour
import org.example.operators.initial.ProbabilisticGreedyTour
import org.example.operators.initial.RandomTour
import org.example.operators.population.IslandPopulationManager
import org.example.operators.postprocessing.localsearch.HillClimbing2Opt
import org.example.operators.postprocessing.mutation.SwapMutation
import org.example.operators.selection.SimpleTournamentSelection
import org.example.strategies.evolution.ClassicCycle
import org.example.strategies.evolution.SteadyStateCycle
import kotlin.random.Random

fun main() {
    val GA_Steady_OX_Swap_Local_Island51 = ExperimentConfig(
        name = "GA_Steady_OX_Swap_Local_Island51",
        datasetPath = "data/tsp_51_1",
        populationManagerFactory = { problem ->
            IslandPopulationManager(
                islandCount = 5,
                islandCapacity = 60,
                problem = problem,
                generator = { GreedyTour<Point>().getInitial(problem) },
                migrationRate = 0.1,
                elitesPerMigration = 2
            )
        },
        evolutionCycleFactory = {
            SteadyStateCycle(
                selection = SimpleTournamentSelection(2),
                crossover = OXCrossover(),
                postProcessing = SwapMutation<Point>(0.1).then(
                    HillClimbing2Opt<Point>(HillClimbing2Opt.Mode.First).withProbability(0.01)
                ),
                offspringCount = 10
            )
        },
        maxGenerations = 1000,
        maxStagnation = Int.MAX_VALUE,
        repeats = 5
    )

    val GA_Classic_OX_Swap_Local_Island51 = ExperimentConfig(
        name = "GA_Classic_OX_Swap_Local_Island51",
        datasetPath = "data/tsp_51_1",
        populationManagerFactory = { problem ->
            IslandPopulationManager(
                islandCount = 5,
                islandCapacity = 60,
                problem = problem,
                generator = { GreedyTour<Point>().getInitial(problem) },
                migrationRate = 0.1,
                elitesPerMigration = 2
            )
        },
        evolutionCycleFactory = {
            SteadyStateCycle(
                selection = SimpleTournamentSelection(2),
                crossover = OXCrossover(),
                postProcessing = SwapMutation<Point>(0.1).then(
                    HillClimbing2Opt<Point>(HillClimbing2Opt.Mode.First).withProbability(0.01)
                ),
                offspringCount = 10
            )
        },
        maxGenerations = 1000,
        maxStagnation = Int.MAX_VALUE,
        repeats = 5
    )

    val GA_Steady_OX_Swap_Local_Island574 = ExperimentConfig(
        name = "GA_Steady_OX_Swap_Local_Island574",
        datasetPath = "data/tsp_574_1",
        populationManagerFactory = { problem ->
            IslandPopulationManager(
                islandCount = 5,
                islandCapacity = 60,
                problem = problem,
                generator = { RandomTour<Point>().getInitial(problem) },
                migrationRate = 0.1,
                elitesPerMigration = 2
            )
        },
        evolutionCycleFactory = {
            SteadyStateCycle(
                selection = SimpleTournamentSelection(2),
                crossover = OXCrossover(),
                postProcessing = SwapMutation<Point>(0.1).then(
                    HillClimbing2Opt<Point>(HillClimbing2Opt.Mode.First).withProbability(0.01)
                ),
                offspringCount = 10
            )
        },
        maxGenerations = 1000,
        maxStagnation = Int.MAX_VALUE,
        repeats = 5
    )

    val GA_Classic_OX_Swap_Local_Island574 = ExperimentConfig(
        name = "GA_Classic_OX_Swap_Local_Island574",
        datasetPath = "data/tsp_574_1",
        populationManagerFactory = { problem ->
            IslandPopulationManager(
                islandCount = 5,
                islandCapacity = 60,
                problem = problem,
                generator = { RandomTour<Point>().getInitial(problem) },
                migrationRate = 0.1,
                elitesPerMigration = 2
            )
        },
        evolutionCycleFactory = {
            SteadyStateCycle(
                selection = SimpleTournamentSelection(2),
                crossover = OXCrossover(),
                postProcessing = SwapMutation<Point>(0.1).then(
                    HillClimbing2Opt<Point>(HillClimbing2Opt.Mode.First).withProbability(0.01)
                ),
                offspringCount = 10
            )
        },
        maxGenerations = 1000,
        maxStagnation = Int.MAX_VALUE,
        repeats = 5
    )

    ExperimentRunner.run(
        listOf(
            GA_Steady_OX_Swap_Local_Island574,
            GA_Classic_OX_Swap_Local_Island574
        )
    )
}