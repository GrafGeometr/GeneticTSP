import org.example.model.Point
import org.example.operators.crossover.Crossover
import org.example.operators.localsearch.HillClimbing2Opt
import org.example.operators.mutation.Mutation
import org.example.operators.population.PopulationManager
import org.example.operators.selection.Selection

data class ExperimentConfig(
    val name: String,
    val datasetPath: String,
    val selection: Selection,
    val crossover: Crossover,
    val mutation: Mutation?,
    val localSearch: HillClimbing2Opt?,
    val mutationRate: Double,
    val localSearchProbability: Double,
    val maxGenerations: Int,
    val maxStagnation: Int = 50,
    val repeats: Int = 10,
    val populationManagerFactory: () -> PopulationManager<Point>
)