# Own tests

This directory contains custom request fixtures and a runner that uses the course validator (`tools/public_validate.check`).

## Files

| File | Purpose |
|---|---|
| `edge.json` | 43 custom cases in the `public.json` format: `{name, input, output}`. |
| `edge.jsonl` | The same cases as JSON Lines, for a quick manual run. |
| `run_student.py` | Runs `edge.json` and validates each answer using `public_validate.check`. |
| `SearchBench.java` | Reproduction script for the naive/KMP/RK benchmark. |

## How to run tests

```sh
python tests/student/run_student.py
```

Expected output: `PASS all 43 student tests`.

## How to run the benchmark

From the `tests/student` directory:

```sh
javac --release 17 SearchBench.java
java SearchBench
```

This prints the median runtimes for naive, KMP, and exact RK on identical inputs. The raw runs and medians are recorded in `ANALYSIS.md`.

## What each case detects

`public_validate.check` accepts **any valid optimum** for `lcs`, `merge`, `tree`, and `suffix` (witness / plan / selected set / repeat substring). The `output` field in `edge.json` is a reference only; the validator verifies optimality and feasibility, not exact string equality.

### LCS — `lcs`
- `lcs-empty-left`, `lcs-empty-both`: Empty inputs must short-circuit.
- `lcs-not-substring`: Detects confusing *subsequence* with *substring*.
- `lcs-tie`: Checks tie-handling during backtracking.
- `lcs-repeats`: Ensures matching does not reuse the same position.
- `lcs-spaces-punct`: Confirms spaces and punctuation are treated as ordinary characters.

### Merge — `merge`
- `merge-empty`, `merge-one`: Validates base cases for empty and single-element arrays.
- `merge-zero-sizes`: Confirms zero-cost merges still emit a chronological plan.
- `merge-64bit`: Detects integer overflow when costs exceed 32-bit limits.
- `merge-tie`: Tests multiple optimal plans; the validator replays the plan to check validity.

### Tree MWIS — `tree`
- `tree-empty`, `tree-single-zero`: Validates base cases.
- `tree-all-negative`: Ensures the empty set is allowed.
- `tree-tie`: Tests multiple valid optimal sets.
- `tree-deep-chain`: Detects recursion overflow; iterative traversal must be used.

### Search (`naive`, `kmp`, `rk`)
- Empty pattern: Ensures all `0..n` boundaries are returned.
- `m > n`: Ensures an empty list `[]` is returned.
- Overlaps: Confirms `aaaa` with pattern `aa` finds all three starting positions.
- Case and spaces: Verifies no normalization occurs.
- `rk-collision`: Ensures `bc` and `ek` hashes collide but characters are verified.
- `rk-negative-remainder`: Validates normalization of negative remainders.

### Suffix array — `suffix`
- Empty and single-char: Validates base cases.
- `suffix-all-equal`: Ensures sentinels do not leak and overlapping repeats are found.
- `suffix-all-distinct`: Ensures correct behavior on text with no repeats.
- `suffix-no-sentinel`: Verifies the output contains exactly the nonempty suffixes.

### Aho–Corasick — `aho`
- `aho-empty-dictionary`: Validates one empty row per text.
- `aho-no-texts`: Validates no rows are produced.
- `aho-suffix-count`: Ensures suffix patterns are counted via failure links.
- `aho-overlap-duplicate-reset`: Tests overlaps, duplicate columns, and state reset between texts.
- `aho-dense-suffix`: Ensures dense overlapping suffixes do not skip terminals.

### Consecutive-request state reset
- `reset-consecutive-mixed`, `reset-consecutive-after-aho`: These run after an `aho`-intensive batch to ensure no mutable state leaks between requests in the same process.