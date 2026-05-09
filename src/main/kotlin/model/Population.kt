package org.example.model


class Population<S : Solution, P : Problem<S>>(
    val capacity: Int,
    initialIndividuals: List<S> = emptyList()
) {
    private val individuals: MutableList<S> = initialIndividuals.toMutableList()

    val size: Int get() = individuals.size

    fun getAll(): List<S> = individuals.toList()

    fun getBest(problem: P): S? = individuals.minByOrNull { problem.fitness(it) }

    fun add(individual: S) {
        require(individuals.size < capacity) { "Population is full" }
        individuals.add(individual)
    }

    fun replaceWorst(offspring: S, problem: P): Boolean {
        if (individuals.isEmpty()) return false
        val worstIdx = individuals.indices.maxByOrNull { problem.fitness(individuals[it]) } ?: return false
        if (problem.fitness(offspring) < problem.fitness(individuals[worstIdx])) {
            individuals[worstIdx] = offspring
            return true
        }
        return false
    }

    fun replaceAll(newIndividuals: List<S>) {
        individuals.clear()
        individuals.addAll(newIndividuals.take(capacity))
    }

    fun isFull(): Boolean = individuals.size >= capacity

    fun initializeRandom(problem: P, solutionGenerator: (P) -> S) {
        individuals.clear()
        repeat(capacity) {
            individuals.add(solutionGenerator(problem))
        }
    }
}