# Portfolio asset provenance

These assets support [the detailed portfolio case study](../../PORTFOLIO_CASE_STUDY.md).
Prepared on 5 October 2026; no application services were started for this export.

| Asset | Type and source | Intended use |
| --- | --- | --- |
| `system-architecture.svg` / `.png` | Original vector diagram derived from root Maven modules, Compose and database initialization | Architecture section |
| `approval-reliability.svg` / `.png` | Original vector diagram derived from delivery workers and revision checks | Reliability section |
| `search-benchmark.svg` / `.png` | Original vector chart derived from `docs/benchmarks/2026-10-05-catalogue.json` | Measured-results section |
| `dashboard-desktop.png` | Unmodified copy of `artifacts/visual-acceptance/dashboard-seeded-desktop.png` | Actual desktop UI evidence |
| `dashboard-mobile.png` | Unmodified copy of `artifacts/visual-acceptance/dashboard-seeded-mobile.png` | Actual full-page mobile UI evidence |

The screenshots show synthetic automated-acceptance data. They were visually
reviewed before copying; no passwords, tokens, email addresses or phone numbers
are visible. They are not a new browser run or an AI-generated interface.

The diagrams are deterministic SVG assets, not AI-generated raster illustrations.
This keeps labels, counts, workflow arrows and benchmark values auditable. PNG
exports make the same assets usable on portfolio platforms without SVG support.
The architecture is a logical view, not an exhaustive dependency graph or a
cloud deployment screenshot.

## Regenerate PNG diagrams

Requires the frontend's installed development dependencies and local Chrome.
From the repository root:

```powershell
node scripts/render-portfolio.mjs
```

The renderer reads only the three listed SVG files, blocks remote requests and
overwrites their derived PNG exports. It does not start Docker, access `.env`,
log in to the application or send SMS. Edit the SVG sources rather than the PNGs.

The benchmark chart shows **p95**, not mean latency. It uses five workloads
with 200 measured requests each, four workers and ten warm-ups per workload.
The source JSON and case study carry the measurement limitations.
