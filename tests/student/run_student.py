"""Run tests/student/edge.json through the launcher and public validator."""
import json
import pathlib
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from public_validate import check  # same checker used for public cases

FIXTURES = pathlib.Path(__file__).with_name('edge.json')


def main():
    cases = json.loads(FIXTURES.read_text(encoding='utf-8'))
    data = ''.join(json.dumps(c['input'], separators=(',', ':')) + '\n' for c in cases)

    cmd = [sys.executable, str(ROOT / 'tools' / 'run.py'),
           '--language', 'Java', '--input', '/dev/stdin']
    # Simpler: build once, then pipe. Use tools/run.py --input for single file,
    # or invoke Java directly after build. Here we build once and exec Main.
    build = subprocess.run(
        [sys.executable, str(ROOT / 'tools' / 'run.py'), '--language', 'Java', '--setup'],
        capture_output=True, text=True)
    if build.returncode != 0:
        print('setup failed:', build.stderr, file=sys.stderr)
        return 1

    java = subprocess.run(
        ['java', '-Xmx384m', '-cp', str(ROOT / 'Java' / 'build'), 'Main'],
        input=data, capture_output=True, text=True, timeout=20)
    if java.returncode != 0:
        print('program failed:', java.stderr, file=sys.stderr)
        return 1

    lines = java.stdout.splitlines()
    if len(lines) != len(cases):
        print(f'expected {len(cases)} lines, got {len(lines)}', file=sys.stderr)
        return 1

    failures = 0
    for case, line in zip(cases, lines):
        try:
            ans = json.loads(line)
        except json.JSONDecodeError as e:
            print(f'FAIL {case["name"]}: invalid JSON ({e})')
            failures += 1
            continue
        if not check(case['input'], ans, case['output']):
            print(f'FAIL {case["name"]}: got {line}')
            failures += 1
    if failures:
        print(f'{failures} student test(s) failed')
        return 1
    print(f'PASS all {len(cases)} student tests')
    return 0


if __name__ == '__main__':
    sys.exit(main())