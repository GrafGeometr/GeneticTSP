import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path
import seaborn as sns
import re

RESULTS_DIR = Path("results")
PLOTS_DIR = Path("plots")
PLOTS_DIR.mkdir(exist_ok=True)

FIG_SIZE = (10, 5)
ALPHA_INDIVIDUAL = 0.3
COLORMAP = plt.cm.tab10

sns.set_style("whitegrid")

def load_run(file_path):
    df = pd.read_csv(file_path)
    df.columns = df.columns.str.lower()
    return df

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

def plot_individual_algorithm(dataset_name, algo_name, runs):
    data = prepare_algo_data(runs)
    ds_plot_dir = PLOTS_DIR / dataset_name
    ds_plot_dir.mkdir(exist_ok=True)

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
    fig.savefig(ds_plot_dir / f'{algo_name}_fitness.png', dpi=150)
    plt.close(fig)

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
    fig.savefig(ds_plot_dir / f'{algo_name}_fitness_log.png', dpi=150)
    plt.close(fig)

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
    fig.savefig(ds_plot_dir / f'{algo_name}_fitness_tail.png', dpi=150)
    plt.close(fig)

    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['unique_edges'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL)
    ax.plot(data['generations'], data['avg_unique'], 'k-', lw=2, label='Mean')
    ax.set_title(f'{algo_name}: Edge Diversity')
    ax.legend(fontsize='small')
    ax.set_ylim(0, 1.05)
    fig.tight_layout()
    fig.savefig(ds_plot_dir / f'{algo_name}_diversity.png', dpi=150)
    plt.close(fig)

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
    fig.savefig(ds_plot_dir / f'{algo_name}_diversity_scaled.png', dpi=150)
    plt.close(fig)

def classify(algo_name):
    feat = {'pop': 'simple' if 'Simple_' in algo_name else 'island',
            'cycle': 'classic' if 'Classic' in algo_name else 'steady',
            'crossover': 'Uniform' if 'Uniform' in algo_name else 'OX'}

    # Тип мутации
    if 'Inversion' in algo_name:
        feat['mutation'] = 'Inversion'
    elif 'Scramble' in algo_name:
        feat['mutation'] = 'Scramble'
    else:
        feat['mutation'] = 'Swap'


    m = re.search(r'Swap(\d+\.?\d*)', algo_name)
    if m and m.group(1) not in ('', '0'):
        rate_str = m.group(1)
        if '.' in rate_str:
            feat['mutation_rate'] = float(rate_str)
        else:
            rate_val = float(rate_str)
            if rate_val < 1:
                feat['mutation_rate'] = rate_val
            elif rate_val >= 100:
                feat['mutation_rate'] = rate_val / 1000
            elif rate_val >= 10:
                feat['mutation_rate'] = rate_val / 100
            else:
                feat['mutation_rate'] = rate_val / 10

            if len(rate_str) == 2:
                feat['mutation_rate'] = rate_val / 10
            elif len(rate_str) == 3:
                feat['mutation_rate'] = rate_val / 100
            else:
                feat['mutation_rate'] = rate_val / 10
    elif feat['mutation'] == 'Swap':
        feat['mutation_rate'] = 0.1
    else:
        feat['mutation_rate'] = None

    m = re.search(r'LS(\d+)', algo_name)
    if m:
        digits = m.group(1)
        val = float(digits)
        if len(digits) == 2:
            feat['ls_prob'] = val / 10
        else:
            feat['ls_prob'] = val / 100
        feat['use_ls'] = True
    else:
        feat['ls_prob'] = None
        feat['use_ls'] = 'noLS' not in algo_name

    if 'unbiasedSel' in algo_name:
        feat['selection'] = 'UnbiasedTournament'
    elif 'parSel75' in algo_name:
        feat['selection'] = 'Parameterized75'
    elif 'parSel90' in algo_name:
        feat['selection'] = 'Parameterized90'
    else:
        feat['selection'] = 'SimpleTournament2'

    if feat['pop'] == 'island':
        m = re.search(r'islandCap(\d+)', algo_name)
        feat['pop_size'] = int(m.group(1)) if m else 60
        m = re.search(r'migr(\d+\.?\d*)', algo_name)
        feat['migration_rate'] = float(m.group(1)) if m else 0.1
    else:
        m = re.search(r'cap(\d+)', algo_name)
        feat['pop_size'] = int(m.group(1)) if m else 300
        feat['migration_rate'] = None

    return feat

def plot_comparison(dataset_name, algos_dict, group_name, metric='avg_best',
                    ylabel=None, title_prefix=None, log=False, ylim=None, save_dir=None):
    if len(algos_dict) < 2:
        return
    if save_dir is None:
        save_dir = PLOTS_DIR / dataset_name
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
    filename = f"{group_name}_{metric}.png"
    fig.savefig(save_dir / filename, dpi=150)
    plt.close(fig)

def plot_comparison_with_tail(dataset_name, algos_dict, group_name, save_dir):
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

def plot_comparison_diversity(dataset_name, algos_dict, group_name, save_dir):
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

def split_by_cycle(grp_dict):
    """Разделяет словарь на classic и steady подгруппы."""
    sub = {'classic': {}, 'steady': {}}
    for name, data in grp_dict.items():
        if 'Classic' in name:
            sub['classic'][name] = data
        else:
            sub['steady'][name] = data
    return {k: v for k, v in sub.items() if v}

def process_group(dataset_name, group_name, grp_dict, split_cycle=True):
    comp_dir = PLOTS_DIR / dataset_name / "comparisons" / group_name
    if not split_cycle or group_name == 'Cycle':
        if len(grp_dict) >= 2:
            plot_comparison_with_tail(dataset_name, grp_dict, group_name, comp_dir)
            plot_comparison_diversity(dataset_name, grp_dict, group_name, comp_dir)
        return
    sub_groups = split_by_cycle(grp_dict)
    for suffix, sub_dict in sub_groups.items():
        if len(sub_dict) >= 2:
            full_name = f"{group_name}_{suffix}"
            plot_comparison_with_tail(dataset_name, sub_dict, full_name, comp_dir)
            plot_comparison_diversity(dataset_name, sub_dict, full_name, comp_dir)

def main():
    datasets = [d for d in RESULTS_DIR.iterdir() if d.is_dir()]
    if not datasets:
        print("Results folder empty")
        return

    for ds_dir in datasets:
        ds_name = ds_dir.name
        print(f"\n{'='*60}")
        print(f"Processing dataset: {ds_name}")
        all_data = {}

        for algo_dir in sorted(ds_dir.iterdir()):
            if not algo_dir.is_dir():
                continue
            algo_name = algo_dir.name
            csv_files = sorted(algo_dir.glob("run_*.csv"))
            if not csv_files:
                continue
            runs = [load_run(f) for f in csv_files]
            plot_individual_algorithm(ds_name, algo_name, runs)
            all_data[algo_name] = prepare_algo_data(runs)

        groups = {
            'Cycle': {},
            'PopType': {},
            'Mutation': {},
            'Selection': {},
            'Crossover': {},
            'LSProbability': {}
        }

        for name, data in all_data.items():
            f = classify(name)

            if (f['pop'] == 'island' and f['crossover'] == 'OX' and f['mutation'] == 'Swap' and
                    f['mutation_rate'] == 0.1 and f['use_ls'] and f['ls_prob'] == 0.01 and
                    f['selection'] == 'SimpleTournament2' and f['pop_size'] == 60 and
                    f['migration_rate'] == 0.1):
                groups['Cycle'][name] = data

            if (f['crossover'] == 'OX' and f['mutation'] == 'Swap' and
                    f['mutation_rate'] == 0.1 and f['use_ls'] and f['ls_prob'] == 0.01 and
                    f['selection'] == 'SimpleTournament2'):
                if f['pop'] == 'island' and f['pop_size'] == 60 and f['migration_rate'] == 0.1:
                    groups['PopType'][name] = data
                elif f['pop'] == 'simple' and f['pop_size'] == 300:
                    groups['PopType'][name] = data

            if (f['pop'] == 'island' and f['crossover'] == 'OX' and
                    f['use_ls'] and f['ls_prob'] == 0.01 and
                    f['selection'] == 'SimpleTournament2' and
                    f['pop_size'] == 60 and f['migration_rate'] == 0.1):
                groups['Mutation'][name] = data

            if (f['pop'] == 'island' and f['crossover'] == 'OX' and
                    f['mutation'] == 'Swap' and f['mutation_rate'] == 0.1 and
                    f['use_ls'] and f['ls_prob'] == 0.01):
                groups['Selection'][name] = data

            if (f['pop'] == 'island' and f['mutation'] == 'Swap' and
                    f['mutation_rate'] == 0.1 and f['use_ls'] and f['ls_prob'] == 0.01 and
                    f['selection'] == 'SimpleTournament2' and
                    f['pop_size'] == 60 and f['migration_rate'] == 0.1):
                groups['Crossover'][name] = data

            if (f['pop'] == 'island' and f['crossover'] == 'OX' and
                    f['mutation'] == 'Swap' and f['mutation_rate'] == 0.1 and
                    f['selection'] == 'SimpleTournament2'):
                groups['LSProbability'][name] = data

        for grp_name, grp_dict in groups.items():
            n = len(grp_dict)
            print(f"  Group '{grp_name}': {n} configs", end="")
            if n > 0:
                names = list(grp_dict.keys())
                print(f" {names[0]}, ..., {names[-1]}" if len(names) > 2 else f" {', '.join(names)}")
            else:
                print("  EMPTY!")

        for grp_name, grp_dict in groups.items():
            if len(grp_dict) < 2:
                if len(grp_dict) == 0:
                    print(f"  Skipping '{grp_name}' (no configs matched)")
                else:
                    print(f"  Skipping '{grp_name}' (only 1 config: {list(grp_dict.keys())[0]})")
                continue
            split = (grp_name != 'Cycle')
            process_group(ds_name, grp_name, grp_dict, split_cycle=split)

    print("\n" + "="*60)
    print("Done. Plots saved in plots/")

if __name__ == "__main__":
    main()