#!/usr/bin/env python3
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path
import seaborn as sns
import argparse
import sys

RESULTS_DIR = Path("results")
PLOTS_DIR = Path("plots")
PLOTS_DIR.mkdir(exist_ok=True)

FIG_SIZE = (10, 5)
ALPHA_INDIVIDUAL = 0.3
COLORMAP = plt.cm.tab10

sns.set_style("whitegrid")


def load_run(file_path):
    try:
        df = pd.read_csv(file_path)
        if df.empty:
            return None
        df.columns = df.columns.str.lower()
        return df
    except (pd.errors.EmptyDataError, Exception):
        return None


def get_common_generations(runs):
    gen0 = runs[0]['generation'].values
    for run in runs:
        if not np.array_equal(run['generation'].values, gen0):
            return np.sort(pd.concat([r['generation'] for r in runs]).unique())
    return gen0


def prepare_algo_data(runs):
    common_gen = get_common_generations(runs)
    best_mat = np.array([
        run.set_index('generation').loc[common_gen]['best'].values
        for run in runs
    ])
    unique_mat = np.array([
        run.set_index('generation').loc[common_gen]['unique_edges'].values
        for run in runs
    ])
    return {
        'generations': common_gen,
        'avg_best': np.mean(best_mat, axis=0),
        'median_best': np.median(best_mat, axis=0),
        'min_best': np.min(best_mat),
        'best_matrix': best_mat,
        'avg_unique': np.mean(unique_mat, axis=0),
        'unique_matrix': unique_mat
    }


def plot_individual_algorithm(dataset_name, algo_name, runs, save_dir=None):
    data = prepare_algo_data(runs)
    if save_dir is None:
        save_dir = PLOTS_DIR / dataset_name / algo_name
    save_dir.mkdir(parents=True, exist_ok=True)

    def save_fig(fig, name):
        fig.savefig(save_dir / f'{name}.png', dpi=150)
        plt.close(fig)

    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['best'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL, label=f'run {i}')
    ax.plot(data['generations'], data['avg_best'], 'k-', lw=2, label='Mean')
    ax.plot(data['generations'], data['median_best'], 'k--', lw=2, label='Median')
    ax.set_title(f'{algo_name}: Best Fitness')
    ax.legend(fontsize='small')
    ax.set_xlabel('Generation')
    ax.set_ylabel('Fitness')
    fig.tight_layout()
    save_fig(fig, 'fitness')

    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['best'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL, label=f'run {i}')
    ax.plot(data['generations'], data['avg_best'], 'k-', lw=2, label='Mean')
    ax.plot(data['generations'], data['median_best'], 'k--', lw=2, label='Median')
    ax.set_yscale('log')
    ax.set_title(f'{algo_name}: Fitness (log)')
    ax.legend(fontsize='small')
    ax.set_xlabel('Generation')
    ax.set_ylabel('Fitness')
    fig.tight_layout()
    save_fig(fig, 'fitness_log')

    min_best_overall = np.min(data['best_matrix'])
    threshold = min_best_overall * 1.5
    tail_start_idx = 0
    for idx, val in enumerate(data['avg_best']):
        if val < threshold:
            tail_start_idx = idx
            break
    top_y = np.max(data['best_matrix'][:, tail_start_idx]) if tail_start_idx > 0 else data['avg_best'][0]
    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['best'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL, label=f'run {i}')
    ax.plot(data['generations'], data['avg_best'], 'k-', lw=2, label='Mean')
    ax.plot(data['generations'], data['median_best'], 'k--', lw=2, label='Median')
    ax.set_ylim(min_best_overall * 0.999, top_y)
    ax.set_title(f'{algo_name}: Fitness (zoom tail)')
    ax.legend(fontsize='small')
    ax.set_xlabel('Generation')
    ax.set_ylabel('Fitness')
    fig.tight_layout()
    save_fig(fig, 'fitness_tail')

    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['unique_edges'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL)
    ax.plot(data['generations'], data['avg_unique'], 'k-', lw=2, label='Mean')
    ax.set_title(f'{algo_name}: Edge Diversity')
    ax.legend(fontsize='small')
    ax.set_ylim(0, 1.05)
    fig.tight_layout()
    save_fig(fig, 'diversity')

    uniq_mat = data['unique_matrix']
    data_min = np.min(uniq_mat)
    data_max = np.max(uniq_mat)
    margin = 0.05 * (data_max - data_min) if data_max > data_min else 0.01
    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['unique_edges'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL)
    ax.plot(data['generations'], data['avg_unique'], 'k-', lw=2, label='Mean')
    ax.set_title(f'{algo_name}: Edge Diversity (scaled)')
    ax.legend(fontsize='small')
    ax.set_ylim(data_min - margin, data_max + margin)
    fig.tight_layout()
    save_fig(fig, 'diversity_scaled')


def plot_comparison(dataset_name, algos_dict, group_name, metric='avg_best',
                    ylabel=None, title_prefix=None, log=False, ylim=None, save_dir=None):
    if len(algos_dict) < 2:
        return
    if save_dir is None:
        save_dir = PLOTS_DIR / dataset_name / "comparisons" / group_name
    save_dir.mkdir(parents=True, exist_ok=True)

    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for idx, (algo_name, data) in enumerate(algos_dict.items()):
        vals = data[metric]
        gens = data['generations']
        ax.plot(gens, vals, color=COLORMAP(idx % 10), lw=2, label=algo_name)
    if log:
        ax.set_yscale('log')
    if ylim:
        ax.set_ylim(ylim)
    ax.set_xlabel('Generation')
    ax.set_ylabel(ylabel if ylabel else metric)
    title = f"{dataset_name}: {title_prefix if title_prefix else group_name}"
    ax.set_title(title)
    ax.legend()
    fig.tight_layout()
    filename = f"{metric}.png"
    fig.savefig(save_dir / filename, dpi=150)
    plt.close(fig)


def plot_comparison_with_tail(dataset_name, algos_dict, group_name, save_dir=None):
    if save_dir is None:
        save_dir = PLOTS_DIR / dataset_name / "comparisons" / group_name
    save_dir.mkdir(parents=True, exist_ok=True)

    plot_comparison(dataset_name, algos_dict, group_name, metric='avg_best',
                    ylabel='Avg Best Fitness', title_prefix=group_name, save_dir=save_dir)
    plot_comparison(dataset_name, algos_dict, group_name, metric='avg_best',
                    ylabel='Avg Best Fitness (log)', title_prefix=f"{group_name} (log)", log=True, save_dir=save_dir)

    overall_min = min(d['min_best'] for d in algos_dict.values())
    threshold = overall_min * 1.5
    max_len = max(len(d['generations']) for d in algos_dict.values())
    tail_start_idx = 0
    for idx in range(max_len):
        all_below = True
        for d in algos_dict.values():
            if idx < len(d['avg_best']):
                if d['avg_best'][idx] >= threshold:
                    all_below = False
                    break
            else:
                all_below = False
                break
        if all_below:
            tail_start_idx = idx
            break
    top_y = max(d['avg_best'][tail_start_idx] for d in algos_dict.values()
                if tail_start_idx < len(d['avg_best']))
    bottom_y = overall_min * 0.999
    plot_comparison(dataset_name, algos_dict, group_name, metric='avg_best',
                    ylabel='Avg Best Fitness', title_prefix=f"{group_name} (zoom tail)",
                    ylim=(bottom_y, top_y), save_dir=save_dir)


def plot_comparison_diversity(dataset_name, algos_dict, group_name, save_dir=None):
    if save_dir is None:
        save_dir = PLOTS_DIR / dataset_name / "comparisons" / group_name
    save_dir.mkdir(parents=True, exist_ok=True)

    plot_comparison(dataset_name, algos_dict, group_name, metric='avg_unique',
                    ylabel='Avg Unique Edges', title_prefix=f"{group_name} Diversity",
                    ylim=(0, 1.05), save_dir=save_dir)
    all_unique = np.concatenate([d['avg_unique'] for d in algos_dict.values()])
    data_min = np.min(all_unique)
    data_max = np.max(all_unique)
    margin = 0.05 * (data_max - data_min) if data_max > data_min else 0.01
    plot_comparison(dataset_name, algos_dict, group_name, metric='avg_unique',
                    ylabel='Avg Unique Edges', title_prefix=f"{group_name} Diversity (scaled)",
                    ylim=(data_min - margin, data_max + margin), save_dir=save_dir)


def list_datasets():
    return sorted([d.name for d in RESULTS_DIR.iterdir() if d.is_dir()])


def list_algos_for_dataset(dataset_name):
    ds_path = RESULTS_DIR / dataset_name
    if not ds_path.exists():
        return []
    return sorted([d.name for d in ds_path.iterdir() if d.is_dir()])


def main():
    parser = argparse.ArgumentParser(description='Plot TSP experiment results.')
    parser.add_argument('--dataset', required=True, help='Dataset name (e.g. tsp_51_1)')
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--algo', help='Single algorithm name to plot')
    group.add_argument('--algos', nargs='+', help='List of algorithm names to compare')
    parser.add_argument('--group-name', help='Custom name for comparison group (default: auto-generated from algo names)')
    args = parser.parse_args()

    ds_path = RESULTS_DIR / args.dataset
    if not ds_path.exists():
        print(f"Dataset {args.dataset} not found in results/")
        return

    if args.algo:
        algo_dir = ds_path / args.algo
        if not algo_dir.exists():
            print(f"Algorithm {args.algo} not found for dataset {args.dataset}")
            return
        csv_files = sorted(algo_dir.glob("run_*.csv"))
        if not csv_files:
            print(f"No CSV runs found for {args.algo} in {args.dataset}")
            return
        runs = [load_run(f) for f in csv_files]
        runs = [r for r in runs if r is not None]
        if not runs:
            print("No valid run data loaded.")
            return
        plot_individual_algorithm(args.dataset, args.algo, runs)
        print(f"Individual plots saved for {args.algo} in plots/{args.dataset}/{args.algo}/")
        return

    if args.algos:
        algos_dict = {}
        for algo in args.algos:
            algo_dir = ds_path / algo
            if not algo_dir.exists():
                print(f"Algorithm {algo} not found, skipping")
                continue
            csv_files = sorted(algo_dir.glob("run_*.csv"))
            if not csv_files:
                print(f"No runs for {algo}, skipping")
                continue
            runs = [load_run(f) for f in csv_files]
            runs = [r for r in runs if r is not None]
            if not runs:
                print(f"No valid run data for {algo}, skipping")
                continue
            algos_dict[algo] = prepare_algo_data(runs)

        if len(algos_dict) < 2:
            print("Not enough algorithms to compare (need at least 2).")
            return

        group_name = args.group_name if args.group_name else "_vs_".join(args.algos)
        plot_comparison_with_tail(args.dataset, algos_dict, group_name)
        plot_comparison_diversity(args.dataset, algos_dict, group_name)
        print(f"Comparison plots saved in plots/{args.dataset}/comparisons/{group_name}/")


if __name__ == "__main__":
    main()