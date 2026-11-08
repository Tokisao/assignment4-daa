import java.util.*;

public final class Algorithms {

    static Map<String,Object> lcs(Map<String,Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                    ? dp[i - 1][j - 1] + 1
                    : Math.max(dp[i - 1][j], dp[i][j - 1]);

        StringBuilder rev = new StringBuilder();
        int i = n, j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                rev.append(a.charAt(i - 1));
                i--; j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return Map.of("length", dp[n][m], "sequence", rev.reverse().toString());
    }

    @SuppressWarnings("unchecked")
    static Map<String,Object> merge(Map<String,Object> input) {
        List<Object> raw = (List<Object>) input.get("sizes");
        int n = raw.size();
        long[] s = new long[n];
        long[] pref = new long[n + 1];
        for (int k = 0; k < n; k++) {
            s[k] = ((Number) raw.get(k)).longValue();
            pref[k + 1] = pref[k] + s[k];
        }
        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];
        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len - 1 < n; l++) {
                int r = l + len - 1;
                dp[l][r] = Long.MAX_VALUE;
                for (int k = l; k < r; k++) {
                    long cost = dp[l][k] + dp[k + 1][r] + (pref[r + 1] - pref[l]);
                    if (cost < dp[l][r]) {
                        dp[l][r] = cost;
                        split[l][r] = k;
                    }
                }
            }
        }
        List<Object> plan = new ArrayList<>();
        if (n > 1) emitPlan(split, 0, n - 1, plan);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("cost", n > 1 ? dp[0][n - 1] : 0L);
        out.put("plan", plan);
        return out;
    }

    private static void emitPlan(int[][] split, int l, int r, List<Object> plan) {
        if (l == r) return;
        int k = split[l][r];
        emitPlan(split, l, k, plan);        // children first
        emitPlan(split, k + 1, r, plan);
        plan.add(List.of(l, k, r));         // parent merge last
    }

    @SuppressWarnings("unchecked")
    static Map<String,Object> tree(Map<String,Object> input) {
        int n = ((Number) input.get("n")).intValue();
        List<Object> wRaw = (List<Object>) input.get("weights");
        List<Object> eRaw = (List<Object>) input.get("edges");
        long[] w = new long[n];
        for (int k = 0; k < n; k++) w[k] = ((Number) wRaw.get(k)).longValue();

        if (n == 0) {
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("weight", 0L);
            out.put("selected", new ArrayList<>());
            return out;
        }

        List<List<Integer>> g = new ArrayList<>();
        for (int k = 0; k < n; k++) g.add(new ArrayList<>());
        for (Object eo : eRaw) {
            List<Object> e = (List<Object>) eo;
            int u = ((Number) e.get(0)).intValue();
            int v = ((Number) e.get(1)).intValue();
            g.get(u).add(v); g.get(v).add(u);
        }

        int[] parent = new int[n];
        Arrays.fill(parent, -2);
        int[] order = new int[n];
        int oi = 0;
        Deque<Integer> stack = new ArrayDeque<>();
        parent[0] = -1;
        stack.push(0);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            order[oi++] = u;
            for (int v : g.get(u)) {
                if (parent[v] == -2) { parent[v] = u; stack.push(v); }
            }
        }

        long[] take = new long[n], skip = new long[n];
        for (int t = oi - 1; t >= 0; t--) {
            int u = order[t];
            take[u] = w[u];
            for (int v : g.get(u)) {
                if (parent[v] == u) {
                    take[u] += skip[v];
                    skip[u] += Math.max(take[v], skip[v]);
                }
            }
        }

        boolean[] chosen = new boolean[n];
        List<Integer> sel = new ArrayList<>();
        for (int t = 0; t < oi; t++) {
            int u = order[t];
            boolean blocked = parent[u] >= 0 && chosen[parent[u]];
            chosen[u] = !blocked && take[u] > skip[u];
            if (chosen[u]) sel.add(u);
        }
        Collections.sort(sel);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("weight", Math.max(take[0], skip[0]));
        out.put("selected", sel);
        return out;
    }

    static Map<String,Object> naive(Map<String,Object> input) {
        String text = (String) input.get("text");
        String pat = (String) input.get("pattern");
        List<Object> pos = new ArrayList<>();
        int n = text.length(), m = pat.length();
        if (m == 0) {
            for (int i = 0; i <= n; i++) pos.add(i);
        } else if (m <= n) {
            for (int i = 0; i + m <= n; i++) {
                boolean ok = true;
                for (int k = 0; k < m; k++)
                    if (text.charAt(i + k) != pat.charAt(k)) { ok = false; break; }
                if (ok) pos.add(i);
            }
        }
        return Map.of("positions", pos);
    }

    static Map<String,Object> kmp(Map<String,Object> input) {
        String text = (String) input.get("text");
        String pat = (String) input.get("pattern");
        List<Object> pos = new ArrayList<>();
        int n = text.length(), m = pat.length();
        if (m == 0) {
            for (int i = 0; i <= n; i++) pos.add(i);
            return Map.of("positions", pos);
        }
        if (m > n) return Map.of("positions", pos);

        int[] pi = new int[m];
        for (int k = 1; k < m; k++) {
            int j = pi[k - 1];
            while (j > 0 && pat.charAt(k) != pat.charAt(j)) j = pi[j - 1];
            if (pat.charAt(k) == pat.charAt(j)) j++;
            pi[k] = j;
        }
        int j = 0;
        for (int i = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pat.charAt(j)) j = pi[j - 1];
            if (text.charAt(i) == pat.charAt(j)) j++;
            if (j == m) {
                pos.add(i - m + 1);
                j = pi[j - 1];
            }
        }
        return Map.of("positions", pos);
    }

    static Map<String,Object> rk(Map<String,Object> input) {
        String text = (String) input.get("text");
        String pat = (String) input.get("pattern");
        List<Object> pos = new ArrayList<>();
        int n = text.length(), m = pat.length();
        if (m == 0) {
            for (int i = 0; i <= n; i++) pos.add(i);
            return Map.of("positions", pos);
        }
        if (m > n) return Map.of("positions", pos);

        final long B = 31, MOD = 101;
        long hPat = 0, hWin = 0, pow = 1;
        for (int k = 0; k < m - 1; k++) pow = (pow * B) % MOD;
        for (int k = 0; k < m; k++) {
            hPat = (hPat * B + pat.charAt(k)) % MOD;
            hWin = (hWin * B + text.charAt(k)) % MOD;
        }
        for (int i = 0; i + m <= n; i++) {
            if (hWin == hPat) {
                boolean ok = true;
                for (int k = 0; k < m; k++)
                    if (text.charAt(i + k) != pat.charAt(k)) { ok = false; break; }
                if (ok) pos.add(i);
            }
            if (i + m < n) {
                hWin = (hWin - text.charAt(i) * pow) % MOD;
                if (hWin < 0) hWin += MOD;
                hWin = (hWin * B + text.charAt(i + m)) % MOD;
            }
        }
        return Map.of("positions", pos);
    }

    static Map<String,Object> suffix(Map<String,Object> input) {
        String t = (String) input.get("text");
        int n = t.length();
        if (n == 0) {
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("sa", new ArrayList<>());
            out.put("lcp", new ArrayList<>());
            out.put("repeated", "");
            return out;
        }

        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        for (int k = 0; k < n; k++) { sa[k] = k; rank[k] = t.charAt(k); }
        for (int k = 1; k < n; k *= 2) {
            final int half = k;
            final int[] rk = rank;
            Arrays.sort(sa, (x, y) -> {
                if (rk[x] != rk[y]) return Integer.compare(rk[x], rk[y]);
                int rx = x + half < n ? rk[x + half] : -1;
                int ry = y + half < n ? rk[y + half] : -1;
                return Integer.compare(rx, ry);
            });
            int[] nr = new int[n];
            nr[sa[0]] = 0;
            for (int idx = 1; idx < n; idx++) {
                int prev = sa[idx - 1], cur = sa[idx];
                int px = prev + half < n ? rank[prev + half] : -1;
                int cx = cur + half < n ? rank[cur + half] : -1;
                nr[cur] = nr[prev] + ((rank[cur] != rank[prev] || cx != px) ? 1 : 0);
            }
            rank = nr;
            if (rank[sa[n - 1]] == n - 1) break;
        }

        int[] lcp = new int[n];
        int[] inv = new int[n];
        for (int k = 0; k < n; k++) inv[sa[k]] = k;
        int h = 0;
        for (int k = 0; k < n; k++) {
            if (inv[k] > 0) {
                int prev = sa[inv[k] - 1];
                while (k + h < n && prev + h < n && t.charAt(k + h) == t.charAt(prev + h)) h++;
                lcp[inv[k]] = h;
                if (h > 0) h--;
            } else {
                h = 0;
            }
        }

        int best = 0;
        for (int v : lcp) best = Math.max(best, v);
        String rep = best == 0 ? "" : t.substring(sa[invIndexOfMaxLcp(lcp) - 1], sa[invIndexOfMaxLcp(lcp) - 1] + best);

        List<Object> saOut = new ArrayList<>();
        for (int v : sa) saOut.add(v);
        List<Object> lcpOut = new ArrayList<>();
        for (int v : lcp) lcpOut.add(v);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("sa", saOut);
        out.put("lcp", lcpOut);
        out.put("repeated", rep);
        return out;
    }

    private static int invIndexOfMaxLcp(int[] lcp) {
        int best = 0, idx = 0;
        for (int k = 0; k < lcp.length; k++) if (lcp[k] > best) { best = lcp[k]; idx = k; }
        return idx;
    }

    @SuppressWarnings("unchecked")
    static Map<String,Object> aho(Map<String,Object> input) {
        List<Object> pats = (List<Object>) input.get("patterns");
        List<Object> texts = (List<Object>) input.get("texts");
        int p = pats.size();

        // Trie
        List<Map<Character,Integer>> next = new ArrayList<>();
        next.add(new HashMap<>());
        List<List<Integer>> term = new ArrayList<>();
        term.add(new ArrayList<>());
        for (int pi = 0; pi < p; pi++) {
            String s = (String) pats.get(pi);
            int cur = 0;
            for (int k = 0; k < s.length(); k++) {
                char c = s.charAt(k);
                Map<Character,Integer> mp = next.get(cur);
                Integer nx = mp.get(c);
                if (nx == null) {
                    nx = next.size();
                    mp.put(c, nx);
                    next.add(new HashMap<>());
                    term.add(new ArrayList<>());
                }
                cur = nx;
            }
            term.get(cur).add(pi);
        }
        int S = next.size();

        int[] fail = new int[S];
        Arrays.fill(fail, 0);
        Deque<Integer> q = new ArrayDeque<>();
        for (int c : next.get(0).values()) { fail[c] = 0; q.add(c); }
        while (!q.isEmpty()) {
            int u = q.poll();
            for (Map.Entry<Character,Integer> e : next.get(u).entrySet()) {
                char ch = e.getKey();
                int v = e.getValue();
                int f = fail[u];
                while (f != 0 && !next.get(f).containsKey(ch)) f = fail[f];
                Integer nf = next.get(f).get(ch);
                fail[v] = (nf != null && nf != v) ? nf : 0;
                q.add(v);
            }
        }

        List<Object> allRows = new ArrayList<>();
        for (Object to : texts) {
            String txt = (String) to;
            long[] cnt = new long[p];
            int cur = 0;
            for (int k = 0; k < txt.length(); k++) {
                char c = txt.charAt(k);
                while (cur != 0 && !next.get(cur).containsKey(c)) cur = fail[cur];
                Integer nx = next.get(cur).get(c);
                cur = nx != null ? nx : 0;
                // walk output links and count terminals
                int t = cur;
                while (true) {
                    for (int idx : term.get(t)) cnt[idx]++;
                    if (t == 0) break;
                    t = fail[t];
                }
            }
            List<Object> row = new ArrayList<>();
            for (long v : cnt) row.add(v);
            allRows.add(row);
        }
        return Map.of("counts", allRows);
    }
}