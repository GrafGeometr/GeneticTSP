# Техническое руководство

## Требования
- Java 11+
- Python 3.8+
- Python-пакеты: `matplotlib`, `pandas`

Установка зависимостей Python:
```bash
pip install matplotlib pandas
```

## Запуск решателя
Рекомендуется использовать сборщик мусора G1GC для стабильной работы на тяжёлых конфигурациях.

**Важно!** Запуск решателя производит вычисления параллельно и может задействовать все свободные ресурсы процессора.

```bash
java -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -jar solver.jar configs/example.json
```

Конфигурации лежат в папке `configs/` и позволяют гибко задавать операторы (OX, Swap, Scramble), тип популяции (simple, island), цикл эволюции, частоту мутации и локального поиска.

## Визуализация результатов
Скрипт `generate_plots.py` автоматически группирует запуски и строит графики сходимости и рёберного разнообразия.

```bash
python3 generate_plots.py --dataset tsp_574_1 --algo L01_big
```

```bash
 python3 plot_results.py --algos LS1_big Inv_LSmax_big Scr_LSmax_big  --dataset tsp_574_1
```

Готовые изображения сохраняются в папке `plots/`
```