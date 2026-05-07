import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path
import seaborn as sns

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

def plot_config(config_name):
    config_dir = RESULTS_DIR / config_name
    csv_files = sorted(config_dir.glob("run_*.csv"))
    if not csv_files:
        print(f"Нет файлов для конфигурации {config_name}")
        return

    runs = [load_run(f) for f in csv_files]
    max_gen = min(max(r['generation']) for r in runs)

    generations = range(max_gen + 1)
    best_matrix = np.array([run['best'].values[:max_gen + 1] for run in runs])
    avg_best = np.mean(best_matrix, axis=0)
    median_best = np.median(best_matrix, axis=0)

    # 1. Полный график (как было)
    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['best'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL, label=f'run {i}')
    ax.plot(generations, avg_best, 'k-', linewidth=2, label='Среднее')
    ax.plot(generations, median_best, 'k--', linewidth=2, label='Медиана')
    ax.set_xlabel('Поколение')
    ax.set_ylabel('Лучший фитнес (длина маршрута)')
    ax.set_title(f'{config_name}: Сходимость лучшего фитнеса')
    ax.legend(loc='upper right', fontsize='small')
    fig.tight_layout()
    fig.savefig(PLOTS_DIR / f'{config_name}_fitness.png', dpi=150)
    plt.close(fig)

    # 2. Логарифмическая шкала по Y (zoom_log)
    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['best'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL, label=f'run {i}')
    ax.plot(generations, avg_best, 'k-', linewidth=2, label='Среднее')
    ax.plot(generations, median_best, 'k--', linewidth=2, label='Медиана')
    ax.set_yscale('log')
    ax.set_xlabel('Поколение')
    ax.set_ylabel('Лучший фитнес (log scale)')
    ax.set_title(f'{config_name}: Сходимость (log Y)')
    ax.legend(loc='upper right', fontsize='small')
    fig.tight_layout()
    fig.savefig(PLOTS_DIR / f'{config_name}_fitness_log.png', dpi=150)
    plt.close(fig)

    # 3. Линейный zoom на "хвосте" (отсечка сверху, когда средний фитнес приближается к минимуму)
    min_best_overall = np.min(best_matrix)
    # Ищем поколение, на котором средний фитнес впервые падает ниже min_best_overall * 1.5
    threshold = min_best_overall * 1.5
    tail_start_gen = 0
    for gen in range(max_gen + 1):
        if avg_best[gen] < threshold:
            tail_start_gen = gen
            break
    # Для верхней границы Y берём максимум best в поколении tail_start_gen
    top_y = np.max(best_matrix[:, tail_start_gen]) if tail_start_gen > 0 else avg_first_gen

    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['best'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL, label=f'run {i}')
    ax.plot(generations, avg_best, 'k-', linewidth=2, label='Среднее')
    ax.plot(generations, median_best, 'k--', linewidth=2, label='Медиана')
    ax.set_ylim(min_best_overall * 0.999, top_y)
    ax.set_xlabel('Поколение')
    ax.set_ylabel('Лучший фитнес (длина маршрута)')
    ax.set_title(f'{config_name}: Сходимость (zoom tail)')
    ax.legend(loc='upper right', fontsize='small')
    fig.tight_layout()
    fig.savefig(PLOTS_DIR / f'{config_name}_fitness_tail.png', dpi=150)
    plt.close(fig)

    # 4. График разнообразия
    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for i, run in enumerate(runs):
        ax.plot(run['generation'], run['unique_edges'], color=COLORMAP(i % 10),
                alpha=ALPHA_INDIVIDUAL, label=f'run {i}')
    unique_matrix = np.array([run['unique_edges'].values[:max_gen + 1] for run in runs])
    avg_unique = np.mean(unique_matrix, axis=0)
    median_unique = np.median(unique_matrix, axis=0)
    ax.plot(generations, avg_unique, 'k-', linewidth=2, label='Среднее')
    ax.plot(generations, median_unique, 'k--', linewidth=2, label='Медиана')
    ax.set_xlabel('Поколение')
    ax.set_ylabel('Доля уникальных рёбер')
    ax.set_title(f'{config_name}: Разнообразие популяции')
    ax.legend(loc='upper right', fontsize='small')
    ax.set_ylim(0, 1.05)
    fig.tight_layout()
    fig.savefig(PLOTS_DIR / f'{config_name}_diversity.png', dpi=150)
    plt.close(fig)

def plot_comparison(config_names):
    # Собираем данные для всех конфигураций
    config_data = {}
    for config_name in config_names:
        config_dir = RESULTS_DIR / config_name
        csv_files = sorted(config_dir.glob("run_*.csv"))
        if not csv_files:
            continue
        runs = [load_run(f) for f in csv_files]
        max_gen = min(max(r['generation']) for r in runs)
        generations = range(max_gen + 1)
        best_matrix = np.array([run['best'].values[:max_gen + 1] for run in runs])
        avg_best = np.mean(best_matrix, axis=0)
        config_data[config_name] = {
            'avg_best': avg_best,
            'generations': generations,
            'min_best': np.min(best_matrix)
        }

    if not config_data:
        print("Нет данных для сравнения")
        return

    # Определяем общий порог для zoom tail (например, минимальное значение среди всех конфигураций * 1.5)
    overall_min = min(d['min_best'] for d in config_data.values())
    threshold = overall_min * 1.5

    # ----- 1. Полный график -----
    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for idx, (config_name, data) in enumerate(config_data.items()):
        ax.plot(data['generations'], data['avg_best'], color=COLORMAP(idx % 10),
                linewidth=2, label=config_name)
    ax.set_xlabel('Поколение')
    ax.set_ylabel('Средний лучший фитнес')
    ax.set_title('Сравнение конфигураций по среднему лучшему фитнесу')
    ax.legend()
    fig.tight_layout()
    fig.savefig(PLOTS_DIR / 'comparison_fitness.png', dpi=150)
    plt.close(fig)

    # ----- 2. Логарифмическая шкала -----
    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for idx, (config_name, data) in enumerate(config_data.items()):
        ax.plot(data['generations'], data['avg_best'], color=COLORMAP(idx % 10),
                linewidth=2, label=config_name)
    ax.set_yscale('log')
    ax.set_xlabel('Поколение')
    ax.set_ylabel('Средний лучший фитнес (log scale)')
    ax.set_title('Сравнение конфигураций (log Y)')
    ax.legend()
    fig.tight_layout()
    fig.savefig(PLOTS_DIR / 'comparison_fitness_log.png', dpi=150)
    plt.close(fig)

    # ----- 3. Линейный zoom tail -----
    # Находим поколение, на котором все конфигурации уже ниже порога
    # Для верхней границы используем максимум среди средних фитнесов в этом поколении
    tail_start_gen = 0
    for gen in range(max(len(d['generations']) for d in config_data.values())):
        if all(data['avg_best'][gen] < threshold for data in config_data.values() if gen < len(data['avg_best'])):
            tail_start_gen = gen
            break

    top_y = max(data['avg_best'][tail_start_gen] for data in config_data.values() if tail_start_gen < len(data['avg_best']))
    bottom_y = overall_min * 0.999  # чуть ниже минимального достигнутого значения

    fig, ax = plt.subplots(figsize=FIG_SIZE)
    for idx, (config_name, data) in enumerate(config_data.items()):
        ax.plot(data['generations'], data['avg_best'], color=COLORMAP(idx % 10),
                linewidth=2, label=config_name)
    ax.set_ylim(bottom_y, top_y)
    ax.set_xlabel('Поколение')
    ax.set_ylabel('Средний лучший фитнес')
    ax.set_title('Сравнение конфигураций (zoom tail)')
    ax.legend()
    fig.tight_layout()
    fig.savefig(PLOTS_DIR / 'comparison_fitness_tail.png', dpi=150)
    plt.close(fig)

if __name__ == "__main__":
    configs = [d.name for d in RESULTS_DIR.iterdir() if d.is_dir()]
    if not configs:
        print("Папка 'results' пуста. Запустите эксперименты.")
    else:
        for cfg in configs:
            plot_config(cfg)
        if len(configs) > 1:
            plot_comparison(configs)
