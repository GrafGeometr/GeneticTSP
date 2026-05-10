package org.example.experiments

import org.example.model.Point
import org.example.model.TSP
import org.example.model.Tour
import org.example.operators.crossover.OXCrossover
import org.example.operators.crossover.UniformCrossover
import org.example.operators.initial.RandomTour
import org.example.operators.population.IslandPopulationManager
import org.example.operators.population.PopulationManager
import org.example.operators.population.SimplePopulationManager
import org.example.operators.postprocessing.PostProcessing
import org.example.operators.postprocessing.localsearch.HillClimbing2Opt
import org.example.operators.postprocessing.mutation.*
import org.example.operators.selection.*
import org.example.strategies.evolution.ClassicCycle
import org.example.strategies.evolution.EvolutionCycle
import org.example.strategies.evolution.SteadyStateCycle

fun createAlgorithmConfig(
    name: String,
    cycleType: String,
    crossoverType: String,
    mutationType: String,
    mutationRate: Double,
    useLocalSearch: Boolean,
    localSearchMode: HillClimbing2Opt.Mode = HillClimbing2Opt.Mode.First,
    localSearchProbability: Double = 0.01,
    selectionType: String = "SimpleTournament2",
    populationType: String = "island",
    islandCount: Int = 5,
    islandCapacity: Int = 60,
    simpleCapacity: Int = 300,
    migrationRate: Double = 0.1,
    elitesPerMigration: Int = 2,
    offspringCount: Int = 10,
    elitismCount: Int = 2,
    repeats: Int = 5,
    maxGenerations: Int = 1000
): AlgorithmConfig {
    val evolutionFactory: () -> EvolutionCycle<Tour<Point>, TSP<Point>> = {
        val selection = when (selectionType) {
            "SimpleTournament2" -> SimpleTournamentSelection<Tour<Point>, TSP<Point>>(2)
            "UnbiasedTournament" -> UnbiasedTournamentSelection<Tour<Point>, TSP<Point>>()
            "ParameterizedTournament75" -> ParameterizedTournamentSelector<Tour<Point>, TSP<Point>>(0.75)
            "ParameterizedTournament90" -> ParameterizedTournamentSelector<Tour<Point>, TSP<Point>>(0.9)
            else -> throw IllegalArgumentException("Unknown selection: $selectionType")
        }
        val crossover = when (crossoverType) {
            "OX" -> OXCrossover<Point>()
            "Uniform" -> UniformCrossover<Point>()
            else -> throw IllegalArgumentException("Unknown crossover")
        }
        val mutation: PostProcessing<Tour<Point>, TSP<Point>> = when (mutationType) {
            "Swap" -> SwapMutation<Point>(mutationRate)
            "Inversion" -> InversionMutation<Point>(mutationRate)
            "Scramble" -> ScrambleMutation<Point>()
            else -> throw IllegalArgumentException("Unknown mutation")
        }
        val postProcessing = if (useLocalSearch) {
            mutation.then(
                HillClimbing2Opt<Point>(localSearchMode).withProbability(localSearchProbability)
            )
        } else mutation

        when (cycleType) {
            "classic" -> ClassicCycle(selection, crossover, postProcessing, elitismCount)
            "steady" -> SteadyStateCycle(selection, crossover, postProcessing, offspringCount)
            else -> throw IllegalArgumentException("Unknown cycle")
        }
    }

    val populationFactory: (TSP<Point>) -> PopulationManager<Tour<Point>, TSP<Point>> = { problem ->
        when (populationType) {
            "island" -> IslandPopulationManager(
                islandCount = islandCount,
                islandCapacity = islandCapacity,
                problem = problem,
                generator = { RandomTour<Point>().getInitial(problem) },
                migrationRate = migrationRate,
                elitesPerMigration = elitesPerMigration
            )
            "simple" -> SimplePopulationManager(
                capacity = simpleCapacity,
                problem = problem,
                generator = { RandomTour<Point>().getInitial(problem) }
            )
            else -> throw IllegalArgumentException("Unknown population type")
        }
    }

    return AlgorithmConfig(
        name = name,
        populationManagerFactory = populationFactory,
        evolutionCycleFactory = evolutionFactory,
        maxGenerations = maxGenerations,
        maxStagnation = Int.MAX_VALUE,
        repeats = repeats,
        csvLogRate = 10,
        consoleLogRate = 100,
        logImprovements = true
    )
}

val datasets = listOf(
    DatasetInfo("tsp_51_1", "data/tsp_51_1"),
    DatasetInfo("tsp_100_3", "data/tsp_100_3"),
    DatasetInfo("tsp_200_2", "data/tsp_200_2"),
    DatasetInfo("tsp_574_1", "data/tsp_574_1")
)

fun base(name: String, cycleType: String, popType: String = "island"): AlgorithmConfig =
    createAlgorithmConfig(name, cycleType, "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = popType)

val presets = listOf(
    base("Classic_OX_Swap_LS001", "classic"),
    base("Steady_OX_Swap_LS001", "steady"),

    base("Simple_Classic_OX_Swap_LS001", "classic", "simple"),
    base("Simple_Steady_OX_Swap_LS001", "steady", "simple"),

    createAlgorithmConfig("Classic_OX_Inversion_LS001", "classic", "OX", "Inversion", 0.1, true, localSearchProbability = 0.01),
    createAlgorithmConfig("Steady_OX_Inversion_LS001", "steady", "OX", "Inversion", 0.1, true, localSearchProbability = 0.01),
    createAlgorithmConfig("Classic_OX_Scramble_LS001", "classic", "OX", "Scramble", 0.0, true, localSearchProbability = 0.01),
    createAlgorithmConfig("Steady_OX_Scramble_LS001", "steady", "OX", "Scramble", 0.0, true, localSearchProbability = 0.01),

    createAlgorithmConfig("Classic_Uniform_Swap_LS001", "classic", "Uniform", "Swap", 0.1, true, localSearchProbability = 0.01),
    createAlgorithmConfig("Steady_Uniform_Swap_LS001", "steady", "Uniform", "Swap", 0.1, true, localSearchProbability = 0.01),

    createAlgorithmConfig("Classic_OX_Swap_LS001_parSel75", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, selectionType = "ParameterizedTournament75"),
    createAlgorithmConfig("Steady_OX_Swap_LS001_parSel75", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, selectionType = "ParameterizedTournament75"),
    createAlgorithmConfig("Classic_OX_Swap_LS001_parSel90", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, selectionType = "ParameterizedTournament90"),
    createAlgorithmConfig("Steady_OX_Swap_LS001_parSel90", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, selectionType = "ParameterizedTournament90"),
    createAlgorithmConfig("Classic_OX_Swap_LS001_unbiasedSel", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, selectionType = "UnbiasedTournament"),
    createAlgorithmConfig("Steady_OX_Swap_LS001_unbiasedSel", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, selectionType = "UnbiasedTournament"),

    createAlgorithmConfig("Classic_OX_Swap_noLS", "classic", "OX", "Swap", 0.1, false),
    createAlgorithmConfig("Steady_OX_Swap_noLS", "steady", "OX", "Swap", 0.1, false),

    createAlgorithmConfig("Classic_OX_Swap005_LS001", "classic", "OX", "Swap", 0.05, true, localSearchProbability = 0.01),
    createAlgorithmConfig("Steady_OX_Swap005_LS001", "steady", "OX", "Swap", 0.05, true, localSearchProbability = 0.01),
    createAlgorithmConfig("Classic_OX_Swap02_LS001", "classic", "OX", "Swap", 0.2, true, localSearchProbability = 0.01),
    createAlgorithmConfig("Steady_OX_Swap02_LS001", "steady", "OX", "Swap", 0.2, true, localSearchProbability = 0.01),

    createAlgorithmConfig("Classic_OX_Swap_LS005", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.05),
    createAlgorithmConfig("Steady_OX_Swap_LS005", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.05),
    createAlgorithmConfig("Classic_OX_Swap_LS01", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.1),
    createAlgorithmConfig("Steady_OX_Swap_LS01", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.1),

    createAlgorithmConfig("Classic_OX_Swap_LS001_islandCap30", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", islandCapacity = 30),
    createAlgorithmConfig("Steady_OX_Swap_LS001_islandCap30", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", islandCapacity = 30),
    createAlgorithmConfig("Classic_OX_Swap_LS001_islandCap120", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", islandCapacity = 120),
    createAlgorithmConfig("Steady_OX_Swap_LS001_islandCap120", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", islandCapacity = 120),

    createAlgorithmConfig("Simple_Classic_OX_Swap_LS001_cap150", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "simple", simpleCapacity = 150),
    createAlgorithmConfig("Simple_Steady_OX_Swap_LS001_cap150", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "simple", simpleCapacity = 150),
    createAlgorithmConfig("Simple_Classic_OX_Swap_LS001_cap600", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "simple", simpleCapacity = 600),
    createAlgorithmConfig("Simple_Steady_OX_Swap_LS001_cap600", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "simple", simpleCapacity = 600),


    createAlgorithmConfig("Classic_OX_Swap_LS001_migr0.05", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", migrationRate = 0.05),
    createAlgorithmConfig("Steady_OX_Swap_LS001_migr0.05", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", migrationRate = 0.05),
    createAlgorithmConfig("Classic_OX_Swap_LS001_migr0.2", "classic", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", migrationRate = 0.2),
    createAlgorithmConfig("Steady_OX_Swap_LS001_migr0.2", "steady", "OX", "Swap", 0.1, true, localSearchProbability = 0.01, populationType = "island", migrationRate = 0.2),
)