import java.util.*;

/** Standalone search benchmark. Compile and run directly:
 *    javac --release 17 SearchBench.java
 *    java SearchBench
 *  Prints median-of-5 times in microseconds for naive / KMP / RK
 *  on identical (text, pattern) pairs. Also verifies outputs agree
 *  before timing.
 */
public class SearchBench {

    // ---- naive ----
    static List<Integer> naive(String t, String p) {
        List<Integer> r = new ArrayList<>();
        int n = t.length(), m = p.length();
        if (m == 0) { for (int i = 0; i <= n; i++) r.add(i); return r; }
        if (m > n) return r;
        for (int i = 0; i + m <= n; i++) {
            boolean ok = true;
            for (int k = 0; k < m; k++)
                if (t.charAt(i + k) != p.charAt(k)) { ok = false; break; }
            if (ok) r.add(i);
        }
        return r;
    }

    // ---- kmp ----
    static List<Integer> kmp(String t, String p) {
        List<Integer> r = new ArrayList<>();
        int n = t.length(), m = p.length();
        if (m == 0) { for (int i = 0; i <= n; i++) r.add(i); return r; }
        if (m > n) return r;
        int[] pi = new int[m];
        for (int k = 1; k < m; k++) {
            int j = pi[k - 1];
            while (j > 0 && p.charAt(k) != p.charAt(j)) j = pi[j - 1];
            if (p.charAt(k) == p.charAt(j)) j++;
            pi[k] = j;
        }
        int j = 0;
        for (int i = 0; i < n; i++) {
            while (j > 0 && t.charAt(i) != p.charAt(j)) j = pi[j - 1];
            if (t.charAt(i) == p.charAt(j)) j++;
            if (j == m) { r.add(i - m + 1); j = pi[j - 1]; }
        }
        return r;
    }

    // ---- rk ----
    static List<Integer> rk(String t, String p) {
        List<Integer> r = new ArrayList<>();
        int n = t.length(), m = p.length();
        if (m == 0) { for (int i = 0; i <= n; i++) r.add(i); return r; }
        if (m > n) return r;
        final long B = 31, MOD = 101;
        long hp = 0, hw = 0, pow = 1;
        for (int k = 0; k < m - 1; k++) pow = (pow * B) % MOD;
        for (int k = 0; k < m; k++) {
            hp = (hp * B + p.charAt(k)) % MOD;
            hw = (hw * B + t.charAt(k)) % MOD;
        }
        for (int i = 0; i + m <= n; i++) {
            if (hw == hp) {
                boolean ok = true;
                for (int k = 0; k < m; k++)
                    if (t.charAt(i + k) != p.charAt(k)) { ok = false; break; }
                if (ok) r.add(i);
            }
            if (i + m < n) {
                hw = (hw - t.charAt(i) * pow) % MOD;
                if (hw < 0) hw += MOD;
                hw = (hw * B + t.charAt(i + m)) % MOD;
            }
        }
        return r;
    }

    // ---- timing ----
    static long timeUs(Runnable body, int warmup, int runs) {
        for (int i = 0; i < warmup; i++) body.run();
        long[] t = new long[runs];
        for (int i = 0; i < runs; i++) {
            long s = System.nanoTime();
            body.run();
            t[i] = System.nanoTime() - s;
        }
        Arrays.sort(t);
        return t[runs / 2] / 1000; // µs
    }

    static String randomText(Random rnd, int n, boolean rep) {
        char[] s = new char[n];
        for (int i = 0; i < n; i++) s[i] = rep ? 'a' : (char) ('a' + rnd.nextInt(26));
        return new String(s);
    }
    static String randomPattern(Random rnd, int m, boolean rep) {
        char[] s = new char[m];
        for (int i = 0; i < m; i++) s[i] = rep ? 'a' : (char) ('a' + rnd.nextInt(26));
        return new String(s);
    }

    public static void main(String[] args) {
        long seed = 20261108L;
        int[] ns  = {500, 1500, 3000, 1000, 3000};
        int[] ms  = {10,  50,   200,  20,   100};
        boolean[] rep = {false, false, false, true, true};
        String[] names = {"random-small","random-mid","random-large",
                          "repetitive-small","repetitive-large"};

        System.out.println("seed=" + seed + ", warmup=10, runs=5, unit=µs (median)");
        System.out.printf("%-18s %5s %5s | %10s %10s %10s | %s%n",
                "case","n","m","naive","kmp","rk","outputs equal");

        for (int c = 0; c < ns.length; c++) {
            Random rnd = new Random(seed);
            String text = randomText(rnd, ns[c], rep[c]);
            String pat  = randomPattern(rnd, ms[c], rep[c]);

            List<Integer> a = naive(text, pat);
            List<Integer> b = kmp(text, pat);
            List<Integer> d = rk(text, pat);
            if (!a.equals(b) || !a.equals(d))
                throw new AssertionError("outputs differ on " + names[c]);

            long tn = timeUs(() -> naive(text, pat), 10, 5);
            long tk = timeUs(() -> kmp(text, pat),   10, 5);
            long tr = timeUs(() -> rk(text, pat),    10, 5);

            System.out.printf("%-18s %5d %5d | %10d %10d %10d | yes%n",
                    names[c], ns[c], ms[c], tn, tk, tr);
        }
    }
}