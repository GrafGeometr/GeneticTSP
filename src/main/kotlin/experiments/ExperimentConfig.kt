import org.example.model.TSP
import org.example.model.Tour
import org.example.model.Point
import org.example.operators.population.PopulationManager
import org.example.strategies.evolution.EvolutionCycle

data class ExperimentConfig(
    val name: String,
    val datasetPath: String,
    val populationManagerFactory: (TSP<Point>) -> PopulationManager<Tour<Point>, TSP<Point>>,
    val evolutionCycleFactory: () -> EvolutionCycle<Tour<Point>, TSP<Point>>,
    val maxGenerations: Int,
    val maxStagnation: Int = Int.MAX_VALUE,
    val repeats: Int = 5,
    val loggingRate: Int = 10,
)
