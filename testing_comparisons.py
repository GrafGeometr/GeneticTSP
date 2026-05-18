#!/usr/bin/env python3
import subprocess
import sys
import argparse
from pathlib import Path

COMPARISON_GROUPS = {
    "LS_probability": [
        "LSprob_0", "LSprob_0.01", "LSprob_0.05", "LSprob_0.1",
        "LSprob_0.2", "LSprob_0.5", "LSprob_1.0"
    ],
    "LS_probability_no_zero": [
        "LSprob_0.01", "LSprob_0.05", "LSprob_0.1",
        "LSprob_0.2", "LSprob_0.5", "LSprob_1.0"
    ],
    "Mutation_noLS":   ["Mut_Swap_noLS", "Mut_Inversion_noLS", "Mut_Scramble_noLS"],
    "Mutation_LSmax":  ["Mut_Swap_LSmax", "Mut_Inversion_LSmax", "Mut_Scramble_LSmax"],

    "PopulationType_noLS":  ["Pop_simple_noLS", "Pop_island_noLS"],
    "PopulationType_LSmax": ["Pop_simple_LSmax", "Pop_island_LSmax"],

    "PopulationSize_noLS":  ["Size_12_noLS", "Size_24_noLS", "Size_48_noLS"],
    "PopulationSize_LSmax": ["Size_12_LSmax", "Size_24_LSmax", "Size_48_LSmax"],

    "Cycle_noLS":  ["Cycle_Classic_noLS", "Cycle_Steady_noLS"],
    "Cycle_LSmax": ["Cycle_Classic_LSmax", "Cycle_Steady_LSmax"],

    "Crossover_noLS":  ["Cross_OX_noLS", "Cross_Uniform_noLS", "Cross_Particle_noLS"],
    "Crossover_LSmax": ["Cross_OX_LSmax", "Cross_Uniform_LSmax", "Cross_Particle_LSmax"],

    "Selection_noLS":  ["Sel_Tourn_noLS", "Sel_Unbiased_noLS", "Sel_Par75_noLS", "Sel_Par90_noLS"],
    "Selection_LSmax": ["Sel_Tourn_LSmax", "Sel_Unbiased_LSmax", "Sel_Par75_LSmax", "Sel_Par90_LSmax"],

    "Rehope_noLS":  ["Rehope_noLS", "Rehope_rehope_noLS"],
    "Rehope_LSmax": ["Rehope_LSmax", "Rehope_rehope_LSmax"],

    "Initialization_noLS":  ["Init_Random_noLS", "Init_Greedy_noLS"],
    "Initialization_LSmax": ["Init_Random_LSmax", "Init_Greedy_LSmax"],
}


def run_comparison(dataset, group_name, algo_list):
    cmd = [
              sys.executable,
              "plot_results.py",
              "--dataset", dataset,
              "--group-name", group_name,
              "--algos"
          ] + algo_list

    print(f"\n>>> Сравнение: {group_name} (датасет: {dataset})")
    result = subprocess.run(cmd, capture_output=True, text=True)

    if result.returncode == 0:
        print(result.stdout.strip())
    else:
        print(f"Ошибка при построении группы '{group_name}':")
        print(result.stderr)


def main():
    parser = argparse.ArgumentParser(
        description="Генератор сравнительных графиков для TSP-экспериментов"
    )
    parser.add_argument(
        "--dataset", required=True,
        help="Название датасета (например, tsp_51_1, tsp_574_1)"
    )
    args = parser.parse_args()

    dataset = args.dataset
    for group_name, algos in COMPARISON_GROUPS.items():
        run_comparison(dataset, group_name, algos)

    print("\nВсе сравнения завершены.")


if __name__ == "__main__":
    main()