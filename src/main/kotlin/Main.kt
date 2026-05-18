package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.experiments.AlgorithmConfig
import org.example.experiments.DatasetInfo
import org.example.experiments.ExperimentRunner
import org.example.model.Point
import org.example.model.TSP
import org.example.model.Tour
import org.example.operators.crossover.OXCrossover
import org.example.operators.crossover.ParticleLikeCrossover
import org.example.operators.crossover.UniformCrossover
import org.example.operators.initial.GreedyTour
import org.example.operators.initial.LocalSearchInitializer
import org.example.operators.initial.RandomTour
import org.example.operators.population.IslandPopulationManager
import org.example.operators.population.PopulationManager
import org.example.operators.population.SimplePopulationManager
import org.example.operators.postprocessing.PostProcessing
import org.example.operators.postprocessing.localsearch.HillClimbing2Opt
import org.example.operators.postprocessing.mutation.InversionMutation
import org.example.operators.postprocessing.mutation.ScrambleMutation
import org.example.operators.postprocessing.mutation.SwapMutation
import org.example.operators.selection.ParameterizedTournamentSelector
import org.example.operators.selection.SimpleTournamentSelection
import org.example.operators.selection.UnbiasedTournamentSelection
import org.example.strategies.evolution.ClassicCycle
import org.example.strategies.evolution.EvolutionCycle
import org.example.strategies.evolution.SteadyStateCycle
import java.io.File


@Serializable
data class ExperimentJson(
    val common: CommonConfig? = null,
    val algorithms: List<AlgorithmJson>,
    val datasets: List<DatasetJson>
)

@Serializable
data class CommonConfig(
    val maxGenerations: Int = 1000,
    val repeats: Int = 5,
    val csvLogRate: Int = 10,
    val consoleLogRate: Int = 100,
    val logImprovements: Boolean = true,
    val noHopeThreshold: Int = 0,
    val maxStagnation: Int = -1  // -1 means Int.MAX_VALUE
)

@Serializable
data class AlgorithmJson(
    val name: String,
    val maxGenerations: Int? = null,
    val repeats: Int? = null,
    val csvLogRate: Int? = null,
    val consoleLogRate: Int? = null,
    val logImprovements: Boolean? = null,
    val noHopeThreshold: Int? = null,
    val maxStagnation: Int? = null,
    val population: PopulationJson,
    val cycle: CycleJson,
    val selection: SelectionJson,
    val crossover: CrossoverJson,
    val mutation: MutationJson? = null,
    val localSearch: LocalSearchJson? = null,
    val rehope: RehopeJson? = null
)

@Serializable
data class PopulationJson(
    val type: String,
    val islandCount: Int? = 5,
    val islandCapacity: Int? = 60,
    val migrationRate: Double? = 0.1,
    val elitesPerMigration: Int? = 2,
    val capacity: Int? = 300,
    val initialGenerator: String = "random"
)

@Serializable
data class CycleJson(
    val type: String,
    val elitismCount: Int? = 2,
    val offspringCount: Int? = 10
)

@Serializable
data class SelectionJson(
    val type: String,
    val sv: Double? = null
)

@Serializable
data class CrossoverJson(
    val type: String
)

@Serializable
data class MutationJson(
    val type: String,
    val rate: Double? = null
)

@Serializable
data class LocalSearchJson(
    val enabled: Boolean,
    val type: String? = "HillClimbing2Opt",
    val mode: String? = "First",
    val probability: Double = 0.0
)

@Serializable
data class RehopeJson(
    val type: String = "Scramble"
)

@Serializable
data class DatasetJson(
    val name: String,
    val path: String
)

fun AlgorithmJson.toAlgorithmConfig(common: CommonConfig): AlgorithmConfig {
    val maxGen = maxGenerations ?: common.maxGenerations
    val rep = repeats ?: common.repeats
    val csv = csvLogRate ?: common.csvLogRate
    val con = consoleLogRate ?: common.consoleLogRate
    val logImp = logImprovements ?: common.logImprovements
    val noHope = noHopeThreshold ?: common.noHopeThreshold
    val stag = if (maxStagnation != null && maxStagnation >= 0) maxStagnation
    else if (common.maxStagnation >= 0) common.maxStagnation
    else Int.MAX_VALUE

    val popFactory: suspend (TSP<Point>) -> PopulationManager<Tour<Point>, TSP<Point>> = { problem ->
        val generator: suspend (TSP<Point>) -> Tour<Point> = when (population.initialGenerator) {
            "random_ls" -> { p: TSP<Point> ->
                LocalSearchInitializer<Point>(
                    baseInitializer = RandomTour(),
                    localSearch = HillClimbing2Opt(HillClimbing2Opt.Mode.First)
                ).getInitial(p)
            }

            "greedy" -> { p: TSP<Point> -> GreedyTour<Point>().getInitial(p) }
            else -> { p: TSP<Point> -> RandomTour<Point>().getInitial(p) }
        }
        when (population.type) {
            "island" -> IslandPopulationManager(
                islandCount = population.islandCount ?: 5,
                islandCapacity = population.islandCapacity ?: 60,
                problem = problem,
                generator = generator,
                migrationRate = population.migrationRate ?: 0.1,
                elitesPerMigration = population.elitesPerMigration ?: 2
            )

            "simple" -> SimplePopulationManager(
                capacity = population.capacity ?: 300,
                problem = problem,
                generator = generator
            )

            else -> throw IllegalArgumentException("Unknown population type: ${population.type}")
        }
    }

    val selectionOp = when (selection.type) {
        "SimpleTournament2" -> SimpleTournamentSelection<Tour<Point>, TSP<Point>>(2)
        "UnbiasedTournament" -> UnbiasedTournamentSelection()
        "ParameterizedTournament" -> {
            val sv = selection.sv ?: 0.75
            ParameterizedTournamentSelector(sv)
        }

        else -> throw IllegalArgumentException("Unknown selection: ${selection.type}")
    }

    val crossoverOp = when (crossover.type) {
        "OX" -> OXCrossover()
        "Uniform" -> UniformCrossover()
        "ParticleLike" -> ParticleLikeCrossover<Point>()
        else -> throw IllegalArgumentException("Unknown crossover: ${crossover.type}")
    }

    val mutationOp = when (mutation?.type) {
        "Swap" -> SwapMutation(mutation.rate ?: 0.1)
        "Inversion" -> InversionMutation(mutation.rate ?: 0.1)
        "Scramble" -> ScrambleMutation<Point>()
        null -> throw IllegalArgumentException("Mutation must be specified")
        else -> throw IllegalArgumentException("Unknown mutation: ${mutation.type}")
    }

    val postProc = if (localSearch?.enabled == true) {
        val ls = when (localSearch.type) {
            "HillClimbing2Opt" -> {
                val mode = if (localSearch.mode == "Best") HillClimbing2Opt.Mode.Best
                else HillClimbing2Opt.Mode.First
                HillClimbing2Opt<Point>(mode)
            }

            else -> throw IllegalArgumentException("Unknown local search: ${localSearch.type}")
        }
        mutationOp.then(ls.withProbability(localSearch.probability))
    } else {
        mutationOp
    }

    val cycleFactory: () -> EvolutionCycle<Tour<Point>, TSP<Point>> = {
        when (cycle.type) {
            "classic" -> ClassicCycle(selectionOp, crossoverOp, postProc, cycle.elitismCount ?: 2)
            "steady" -> SteadyStateCycle(selectionOp, crossoverOp, postProc, cycle.offspringCount ?: 10)
            else -> throw IllegalArgumentException("Unknown cycle: ${cycle.type}")
        }
    }

    val rehopeFactory: () -> PostProcessing<Tour<Point>, TSP<Point>> = {
        when (rehope?.type ?: "Scramble") {
            "Scramble" -> ScrambleMutation()
            else -> throw IllegalArgumentException("Unknown rehope mutation: ${rehope?.type}")
        }
    }

    return AlgorithmConfig(
        name = name,
        populationManagerFactory = popFactory,
        evolutionCycleFactory = cycleFactory,
        maxGenerations = maxGen,
        maxStagnation = stag,
        repeats = rep,
        csvLogRate = csv,
        consoleLogRate = con,
        logImprovements = logImp,
        noHopeThreshold = noHope,
        rehopeMutationFactory = rehopeFactory
    )
}


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -jar solver.jar <config.json>")
        return
    }
    val configFile = File(args[0])
    if (!configFile.exists()) {
        println("Config file not found: ${args[0]}")
        return
    }
    val json = Json { ignoreUnknownKeys = true }
    val experiment = json.decodeFromString<ExperimentJson>(configFile.readText())
    val common = experiment.common ?: CommonConfig()
    val algorithms = experiment.algorithms.map { it.toAlgorithmConfig(common) }
    // Здесь при желании можно добавить вручную более детальные конфиги для алгоритмов
    val datasets = experiment.datasets.map { DatasetInfo(it.name, it.path) }
    ExperimentRunner.run(algorithms, datasets)
}