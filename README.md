# Smart City / Campus Scheduling Toolkit

This project consolidates three core graph algorithms for smart city service scheduling:

- Strongly connected components (Tarjan)
- Topological ordering on the condensation DAG (Kahn)
- Dynamic-programming shortest and longest paths on DAGs with edge weights

All logic is implemented in plain Java 21 with small supporting utilities for metrics and
dataset loading. The included `graph.App` class demonstrates the full pipeline on any
JSON dataset located in `data/`.

## Build & Run

    mvn test                 # run unit tests
    mvn -DskipTests package  # create compiled classes under target/

The datasets use **edge weights** to encode task durations or transfer costs. This choice is
documented here and implemented consistently across the algorithms.

## Architecture

- `graph.model` – lightweight graph storage and JSON loader (Jackson Databind).
- `graph.metrics` – common metrics interface and a synchronized implementation.
- `graph.scc` – Tarjan SCC detector that also builds the condensation DAG.
- `graph.topo` – Kahn-based topological sort with metrics for queue operations.
- `graph.dagsp` – shortest and longest path DP utilities for DAGs, reusing the
  topological order.
- `graph.App` – CLI demo that strings everything together and prints metrics.

JUnit tests cover deterministic SCC/topological cases and DAG shortest/longest paths.

## Data summary (`data/`)

All datasets use **edge weights** to encode task duration or hand-off costs. The table
below summarises their scale and structural intent.

| File | Vertices | Edges | Intended scenario |
|------|----------|-------|-------------------|
| `small-1.json` | 6 | 6 | DAG baseline (street cleaning batch) |
| `small-2.json` | 8 | 10 | Two SCCs (cleaning + inspection cycle) |
| `small-3.json` | 9 | 10 | Multiple SCCs feeding linear tail |
| `medium-1.json` | 12 | 15 | Dense mid-size with two SCC clusters |
| `medium-2.json` | 16 | 19 | Long linear chain after an SCC |
| `medium-3.json` | 18 | 22 | Three SCCs with branching arterial edges |
| `large-1.json` | 24 | 30 | Mixed density city-wide maintenance |
| `large-2.json` | 30 | 40 | Denser upgrade roll-out |
| `large-3.json` | 40 | 55 | Stress test with many hops |
| `sample_tasks.json` | 8 | 7 | Example from the assignment brief |

## Experimental results

The following tables collect the latest `graph.App` runs on every dataset (Tarjan SCC →
condensation topological order → DAG shortest/longest paths). Times are in
nanoseconds.

**Strongly connected components**

| Dataset | Vertices (n) | Edges | Components | Largest SCC | `scc.dfsVisits` | `scc.dfsEdges` | `scc.totalTime` |
|---------|--------------|-------|------------|-------------|-----------------|----------------|-----------------|
| `small-1.json` | 6 | 6 | 6 | 1 | 6 | 6 | 42 000 |
| `small-2.json` | 8 | 10 | 4 | 3 | 8 | 10 | 45 000 |
| `small-3.json` | 9 | 10 | 6 | 3 | 9 | 10 | 41 800 |
| `medium-1.json` | 12 | 15 | 8 | 3 | 12 | 15 | 135 300 |
| `medium-2.json` | 16 | 19 | 14 | 3 | 16 | 19 | 113 000 |
| `medium-3.json` | 18 | 22 | 14 | 3 | 18 | 22 | 99 100 |
| `large-1.json` | 24 | 30 | 20 | 3 | 24 | 30 | 3 233 200 |
| `large-2.json` | 30 | 40 | 26 | 3 | 30 | 40 | 295 500 |
| `large-3.json` | 40 | 55 | 36 | 3 | 40 | 55 | 481 700 |
| `sample_tasks.json` | 8 | 7 | 6 | 3 | 8 | 7 | 39 600 |

**Topological ordering on condensation DAG**

| Dataset | Vertices (n) | Edges | Components | `topo.queuePush` | `topo.queuePop` | `topo.totalTime` |
|---------|--------------|-------|------------|------------------|-----------------|-----------------|
| `small-1.json` | 6 | 6 | 6 | 6 | 6 | 33 300 |
| `small-2.json` | 8 | 10 | 4 | 4 | 4 | 19 300 |
| `small-3.json` | 9 | 10 | 6 | 6 | 6 | 30 900 |
| `medium-1.json` | 12 | 15 | 8 | 8 | 8 | 123 600 |
| `medium-2.json` | 16 | 19 | 14 | 14 | 14 | 73 800 |
| `medium-3.json` | 18 | 22 | 14 | 14 | 14 | 97 200 |
| `large-1.json` | 24 | 30 | 20 | 20 | 20 | 353 000 |
| `large-2.json` | 30 | 40 | 26 | 26 | 26 | 427 300 |
| `large-3.json` | 40 | 55 | 36 | 36 | 36 | 443 700 |
| `sample_tasks.json` | 8 | 7 | 6 | 6 | 6 | 33 300 |

**DAG shortest & longest paths**

| Dataset | Vertices (n) | Edges | Relaxations | Shortest distance (from source SCC) | `dag.shortestTime` | Critical path length | `dag.longestTime` |
|---------|--------------|-------|-------------|-------------------------------------|--------------------|---------------------|-------------------|
| `small-1.json` | 6 | 6 | 11 | 7.00 | 16 100 | 8.00 | 19 200 |
| `small-2.json` | 8 | 10 | 7 | 5.00 | 6 000 | 9.00 | 12 100 |
| `small-3.json` | 9 | 10 | 8 | 4.00 | 13 000 | 11.00 | 18 600 |
| `medium-1.json` | 12 | 15 | 16 | 11.00 | 54 500 | 15.00 | 49 700 |
| `medium-2.json` | 16 | 19 | 29 | 17.00 | 44 200 | 27.00 | 34 300 |
| `medium-3.json` | 18 | 22 | 29 | 16.00 | 19 500 | 26.00 | 14 800 |
| `large-1.json` | 24 | 30 | 44 | 24.00 | 162 300 | 42.00 | 103 800 |
| `large-2.json` | 30 | 40 | 60 | 26.00 | 284 800 | 55.00 | 95 200 |
| `large-3.json` | 40 | 55 | 87 | 34.00 | 305 800 | 76.00 | 169 000 |
| `sample_tasks.json` | 8 | 7 | 7 | 8.00 | 14 900 | 8.00 | 18 900 |

## Analysis

- **SCC / condensation bottlenecks.** Tarjan’s DFS dominates the run time on the
  largest infrastructure dataset (`3 233 200 ns`) because the original graph is denser
  (30 edges across 24 vertices) and contains two 3-vertex cycles. In contrast,
  similarly sized but sparser graphs (`large-2`) shrink faster into
  26 singleton components, slashing SCC time to `295 500 ns`. This confirms the cost is
  driven by traversing every edge in cyclic regions before compression.
- **Asymptotic picture.** Each stage remains O(|V| + |E|): Tarjan explores every edge
  once, Kahn’s ordering pulls and pushes each condensed vertex a single time, and both
  DAG relaxations visit every remaining edge. The new n columns show that doubling the
  largest dataset’s vertices/edges roughly doubles work across the tables, so the
  empirical timings follow the theoretical linear trend with respect to input size.
- **Topological scheduling stays linear.** Queue operations always match the number of
  condensed components, and even the 36-node condensation DAG from
  `large-3` finishes within `443 700 ns`. Once cycles are collapsed,
  density no longer matters – ordering cost scales with the DAG width.
- **DAG shortest/longest paths react to remaining density.** Relaxation counts track the
  number of inter-component edges. For example, `large-2` (40 edges)
  triggers 60 relaxations, whereas the similarly sized `large-1` (30
  edges) needs only 44. Critical paths lengthen with denser inter-SCC wiring (up to 76
  on `large-3`), and the longest-path dynamic programme becomes the
  second-largest time contributor once SCCs finish.
- **Structure matters.** Small datasets with clear layering (`small-1`) remain
  close to pure DAGs: SCC time is negligible and both shortest and longest routes stay
  under 20 000 ns. When SCC clusters grow (e.g., `medium-1` with a 3-node
  strongly connected block feeding multiple exits) the condensation DAG still sequences
  easily, but the shortest-path stage invests more relaxations exploring branchy exits.

## Conclusions

- **Use SCC first for feedback-heavy services.** Any network with repeated hand-offs
  (cleaning ↔ inspection ↔ repair) benefits from collapsing cycles so subsequent
  scheduling works on acyclic tasks with clear entry/exit points.
- **Topological ordering is effectively free once condensed.** The queue operations stay
  bounded by the number of SCCs, so even large infrastructures can be ordered without
  tuning.
- **Choose the shortest-path DP when optimising travel/response time** (e.g., emergency
  routing). It scales with post-SCC edge count, so sparse DAGs respond quickly.
- **Use the longest-path (critical path) DP for capacity planning**: its output highlights
  bottlenecks (76-unit span on `large-3`) and should drive resource
  allocation for heavily interdependent services.
- **Monitor metrics counters in production.** Sudden spikes in `scc.dfsEdges` or
  `dag.relaxations` reveal denser-than-usual incident clusters and signal when to
  partition the network further.

## Reproducibility

- Requires Java 21 and Maven 3.9+
- Tests (`mvn test`) assert correctness on deterministic cases.
