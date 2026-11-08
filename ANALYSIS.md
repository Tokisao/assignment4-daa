# Assignment 4 — Analysis

## Environment and commands

- **Language / runtime:** Java 17 (selected in `submission.json`).
- **OS / Hardware:** Windows, local environment.
- **Build / run / check commands:**
  ```sh
  python tools/run.py --language Java --setup
  python tools/run.py --language Java --check
  python tools/run.py --language Java --input tests/student/edge.jsonl
  ```

---
## Algorithm explanations

## Part 1 — LCS (`lcs`)

**State.** `dp[i][j]` is the length of the longest common subsequence of `a[0..i)` and `b[0..j)`. Row 0 and column 0 are 0 because an empty prefix shares no nonempty subsequence.

**Transition.** 
- If `a[i-1] == b[j-1]`: `dp[i][j] = dp[i-1][j-1] + 1`.
- Otherwise: `dp[i][j] = max(dp[i-1][j], dp[i][j-1])`.

**Base cases.** `dp[0][j] = dp[i][0] = 0`.

**Order.** Increasing `i`, then increasing `j`.

**Correctness.** Any common subsequence either uses the last characters (if they match, extending an optimal solution) or omits at least one of them. The transition covers all cases and takes the maximum.

**Reconstruction.** Walk back from `dp[n][m]`. On a match, record the character and move diagonally. On a mismatch, move to the neighbor with the larger value. Reverse the result. Any optimal answer is accepted.

**Complexity.** `O((|a|+1)(|b|+1))` time and memory. Empty inputs return `length=0, sequence=""`.

**Boundary test.** `a="axbyc", b="abc"` → `length=3, sequence="abc"`. Detects confusing substring with subsequence.

---

## Part 2 — Merge (`merge`)

**State.** `dp[l][r]` is the minimum total cost of merging the original blocks `l..r` into one. 

**Transition.** For a last split `k` (`l ≤ k < r`): `dp[l][r] = min over k of dp[l][k] + dp[k+1][r] + sum(sizes[l..r])`.

**Base cases.** `dp[l][l] = 0`. For `n ≤ 1`, `cost=0`, `plan=[]`.

**Order.** Increasing interval length `len = 2..n`.

**Correctness.** The last merge joins two adjacent blocks at some `k`. Both sides must be optimal, and the final merge costs the sum of sizes. Trying every `k` covers all last merges.

**Reconstruction.** Store `split[l][r] = k`. Recursively emit child merges first, then the parent merge `[l, k, r]`. This produces exactly `n-1` merges in chronological order.

**Complexity.** `O(n³)` time, `O(n²)` memory. Empty input is `O(1)`.

**Boundary test.** `sizes=[1000000000,1000000000,1000000000]` → `cost=5000000000`. Detects integer overflow.

---

## Part 3 — Tree MWIS (`tree`)

**State.** Root the tree at 0. For each vertex `u`:
- `take[u]`: best weight when `u` is selected.
- `skip[u]`: best weight when `u` is not selected.

**Transition.**
- `take[u] = w[u] + Σ skip[v]`
- `skip[u] = Σ max(take[v], skip[v])` over children `v`.

**Base cases.** Leaf: `take[u]=w[u]`, `skip[u]=0`. `n=0` → `weight=0, selected=[]`.

**Order.** Iterative DFS builds `parent[]` and `order[]`; processing `order` in reverse ensures children are solved before parents.

**Correctness.** Once the decision at `u` is fixed, child subtrees are independent. Trying both states covers every valid independent set. The empty set is allowed (returns 0 for all-negative inputs).

**Reconstruction.** Traverse in preorder. A node is blocked if its parent is selected. Otherwise, choose it if `take[u] > skip[u]`.

**Complexity.** `O(n)` time and memory.

**Boundary test.** Path with `weights=[-4,-1,-5]` → `weight=0, selected=[]`. Detects forcing a vertex when the empty set is allowed.

---

## Part 4 — Naive search (`naive`)

**Idea.** Compare pattern at every valid start `i ∈ [0, n-m]`. Report all starts including overlaps. Empty pattern matches `n+1` boundaries. `m > n` returns `[]`.

**Complexity.** `O(n·m + n + 1)` time, `O(1)` auxiliary memory.

**Boundary test.** `text="aaaaa", pattern="aaa"` → `[0,1,2]`. Detects stopping after the first match or missing overlaps.

---

## Part 5 — KMP (`kmp`)

**State.** `pi[i]` is the length of the longest proper prefix of `pattern[0..i]` that is also a suffix.

**Matching.** On mismatch with `j > 0`, fall back to `j = pi[j-1]`. On full match, report start and set `j = pi[m-1]` to preserve overlaps. Empty pattern returns `0..n`.

**Complexity.** `O(n + m + z)` time, `O(m)` auxiliary memory.

**Boundary test.** `text="abababa", pattern="aba"` → `[0,2,4]`. Detects resetting `j=0` after a match, which loses overlaps.

---

## Part 6 — Rabin–Karp (`rk`)

**State.** Polynomial hash of the current window: base 31, mod 101, ASCII codes.

**Rolling update.** Remove outgoing char (`h = h - text[i] * pow`), normalize negative remainder, shift (`h = h * 31`), add incoming char.

**Collision handling.** Equal hashes are only candidates. Actual characters are verified before reporting. `m=0` and `m>n` are handled before windowing.

**Complexity.** `O(n + m + c·m)` time, where `c` is the number of candidate windows. Worst case `O(n·m)`. The small modulus 101 is a deliberate collision laboratory.

**Boundary test.** `text="bcek", pattern="bc"` → `[0]`. `bc` and `ek` collide but only `bc` passes verification.

---

## Part 7 — Suffix array + Kasai LCP (`suffix`)

**Suffix array (prefix doubling).** Initial rank is the character. Sort suffixes by pairs `(rank[i], rank[i+k])` with `-1` for out-of-range. Re-rank. Stop when all ranks are distinct. Result `sa` is a permutation in lexicographic order. No sentinel is included in the output.

**Kasai LCP.** Reuses the common-prefix length `h`, decreasing it by at most 1 per text position. `lcp[0] = 0`, `lcp[i]` compares `sa[i-1]` and `sa[i]`.

**Longest repeat.** `max(lcp)` is the length of the longest substring with two distinct starts. Recover it as `text.substring(sa[idx-1], sa[idx-1] + max(lcp))`.

**Complexity.** `O(n log²(n+1))` time for doubling, `O(n)` for Kasai. `O(n)` memory.

**Boundary test.** `text="aaaa"` → `sa=[3,2,1,0], lcp=[0,1,2,3], repeated="aaa"`. Detects leaked sentinels and missed overlapping repeats.

---

## Part 8 — Aho–Corasick (`aho`)

**State.** Trie of all patterns, `fail[]` links (longest proper suffix that is a trie prefix), and terminal lists per state.

**Query.** For each text, reset state to 0. For each char, follow `fail` links until a transition exists, then walk the output links to count terminal patterns. Counts overlaps and suffix patterns. Duplicates share terminal states but retain separate output columns.

**Empty dictionary / texts.** Empty dictionary yields `[]` per text. Empty `texts` yields `[]`. Empty text yields zeros per dictionary position.

**Complexity.** Construction `O(L + 95·S + p)`. Query `O(|text| + z)`, where `z` counts visited terminal matches. The output memory is `O(q·(p+1))`.

**Boundary test.** `patterns=["he","she","he"], texts=["sheshe","sheshe",""]` → `[[2,2,2],[2,2,2],[0,0,0]]`. Detects missing suffix matches and leaked counts between texts.

---

## Own tests

All cases are located in `tests/student/edge.json`. They are validated using `public_validate.check`, which accepts alternative optimal witnesses for LCS, merge, tree, and suffix.

| Command | Input or fixture | Expected | Actual | What it checks |
|---|---|---|---|---|
| lcs | `a="", b="abc"` | `length=0` | `length=0` | Empty input |
| lcs | `a="axbyc", b="abc"` | `length=3` | `length=3` | Subsequence, not substring |
| lcs | `a="ABC", b="BAC"` | `length=2` | `length=2` | Tie handling |
| merge | `sizes=[]` | `cost=0` | `cost=0` | Zero blocks |
| merge | `sizes=[10]` | `cost=0` | `cost=0` | One block |
| merge | `sizes=[1e9, 1e9, 1e9]` | `cost=5e9` | `cost=5e9` | 64-bit cost |
| tree | `n=0` | `weight=0` | `weight=0` | Empty tree |
| tree | `[-4,-1,-5]` | `weight=0` | `weight=0` | Empty set allowed |
| tree | Chain `[1,10,1,10,1,10]` | `weight=30` | `weight=30` | Deep chain, no stack overflow |
| naive | `pattern=""` | `[0..n]` | `[0..n]` | Empty pattern boundaries |
| naive | `text="aaaa", pattern="aa"` | `[0,1,2]` | `[0,1,2]` | Overlaps |
| kmp | `text="abababa", pattern="aba"` | `[0,2,4]` | `[0,2,4]` | Overlaps after match |
| rk | `text="bcek", pattern="bc"` | `[0]` | `[0]` | Collision verification |
| rk | `text="ace", pattern="ce"` | `[1]` | `[1]` | Negative remainder normalization |
| suffix | `text="aaaa"` | `lcp=[0,1,2,3]` | `lcp=[0,1,2,3]` | Overlaps, no sentinel |
| aho | `patterns=[]` | `[[],[]]` | `[[],[]]` | Empty dictionary |

Run with:
```sh
python tests/student/run_student.py
```

---

## Search comparison

The benchmark was generated using `tests/student/SearchBench.java` (reproduction script included). It compares naive, KMP, and exact RK on identical text/pattern pairs (fixed seed = `20261108`). All three output lists were verified to be identical before timing.

**Timing scope:** Only the algorithm call. Warm-up: 10 discarded runs. Measured: 5 runs (median reported). Compilation and JSON I/O are excluded.

| Case | n | m | Seed / generator | Distribution | Algorithm | Median time (µs) | Outputs checked |
|---|---:|---:|---|---|---|---:|---|
| random-small | 500 | 10 | fixed seed | uniform a-z | naive | 25 | yes |
| | | | | | kmp | 21 | yes |
| | | | | | rk | 32 | yes |
| random-mid | 1500 | 50 | fixed seed | uniform a-z | naive | 69 | yes |
| | | | | | kmp | 62 | yes |
| | | | | | rk | 93 | yes |
| random-large | 3000 | 200 | fixed seed | uniform a-z | naive | 126 | yes |
| | | | | | kmp | 134 | yes |
| | | | | | rk | 182 | yes |
| repetitive-small | 1000 | 20 | fixed seed | 'a' only | naive | 200 | yes |
| | | | | | kmp | 27 | yes |
| | | | | | rk | 201 | yes |
| repetitive-large | 3000 | 100 | fixed seed | 'a' only | naive | 346 | yes |
| | | | | | kmp | 164 | yes |
| | | | | | rk | 326 | yes |

**Interpretation.**
- **Random inputs:** KMP is consistently the fastest. Naive is competitive on small `m` because of its tiny constant, but its quadratic worst case becomes visible as `m` grows (`random-large`). RK has hashing overhead but avoids many character comparisons, although it still scales worse than KMP due to the modulo arithmetic.
- **Repetitive inputs (`'a'` only):** Naive degrades to `O(n·m)` because mismatches happen late, giving its worst-case behavior (`346 µs` vs `126 µs`). KMP stays extremely fast because the prefix function handles the repetition in linear time. RK degrades significantly due to hash collisions (base 31, mod 101) and requires many character verifications, making it slower than KMP.
- **Collision lab:** The small modulus 101 is a deliberate educational choice to expose hash collisions (tested in `rk-collision`). It does not represent production hash implementations (which use larger moduli or 64-bit integers to minimize collisions).
- **Asymptotics:** These medians illustrate theoretical behavior but do not prove asymptotic complexity. They reflect local hardware, small inputs, and JVM warm-up effects.

---

## Sources and AI assistance

Tools and materials used:
- Course materials: Lecture 8 (DP), Lecture 9 (KMP/hash), Lecture 10 (suffix/Kasai/Aho).
- CLRS 4th edition, chapter 14.
- A large language model (assistant) was used as a coding and explanation assistant. It suggested the Java skeleton, the analysis text, and the benchmark harness layout.

My own verification:
- Re-derived the recurrences manually and adapted the suggested code to fit the contract.
- Verified all public cases and the repeated mixed batch pass via `python tools/run.py --check`.
- Wrote the `tests/student/edge.json` cases and verified they pass.
- Ran `SearchBench.java` locally (Windows) to obtain the raw runs, verifying identical outputs before timing.

I take responsibility for the correctness and originality of the submitted code and analysis.