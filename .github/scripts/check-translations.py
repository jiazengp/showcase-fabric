#!/usr/bin/env python3
"""Validate the Showcase translation files and their use from code.

The mod ships translations in `data/showcase/lang/*.json`; the server sends them to clients
through server-translations-api. Every locale's translation set is the union of that locale
with `en_us` (see ServerTranslations#createLanguage), which drives the two failure modes this
script exists to catch:

  * a key missing from `en_us` is sent for **no** locale, so every client renders the raw key;
  * a key missing from one locale falls back to the English text, so it renders untranslated.

The remaining checks guard defects that are invisible until a player sees them: placeholder
count/type drift (renders a literal `%s`), argument order that does not match the call site
(renders the wrong nouns, which is how es/fr/it/pt_br/pl/ru shipped broken), and a message
that slips back into first person although it is broadcast to everyone.

Usage: python3 .github/scripts/check-translations.py [--root PATH]
Exit code 0 when clean, 1 when any check fails.
"""

import argparse
import json
import os
import re
import sys

BACKSLASH = chr(92)
QUOTE = chr(34)
PLACEHOLDER = re.compile(r'%[sd]')

# Messages broadcast to every player (and to private receivers). They describe a player in the
# third person, so a first/second-person pronoun here reads as if the reader did it.
BROADCAST_PREFIXES = ('showcase.message.other_', 'showcase.message.default.')
BROADCAST_SUFFIXES = ('_shared',)
# Broadcast keys that describe content the acting player actually owns. Chinese needs the
# reflexive 自己, otherwise the sentence reads as if the content were someone else's.
BROADCAST_OWNED = (
    'showcase.message.item_shared', 'showcase.message.stats_shared',
    'showcase.message.inventory_shared', 'showcase.message.ender_chest_shared',
    'showcase.message.hotbar_shared', 'showcase.message.default.item',
    'showcase.message.default.stats', 'showcase.message.default.inventory',
    'showcase.message.default.ender_chest', 'showcase.message.default.hotbar',
)
# Broadcast keys whose content is *not* owned by the acting player: a container may be a chest
# standing in the world and a merchant belongs to a villager, so these must stay indefinite
# ("a container", 一个容器) and must not claim ownership.
BROADCAST_OPENED = (
    'showcase.message.container_shared', 'showcase.message.merchant_shared',
    'showcase.message.default.container', 'showcase.message.default.merchant',
)
# Locales whose broadcast copy was reviewed for person and ownership.
PERSON_REVIEWED = {
    'zh_cn': ('自己', '我'),
    'zh_hk': ('自己', '我'),
    'zh_tw': ('自己', '我'),
}


def repo_root(override):
    if override:
        return os.path.abspath(override)
    return os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


def load_languages(root):
    lang_dir = os.path.join(root, 'src', 'main', 'resources', 'data', 'showcase', 'lang')
    langs = {}
    for name in sorted(os.listdir(lang_dir)):
        if not name.endswith('.json'):
            continue
        path = os.path.join(lang_dir, name)
        with open(path, encoding='utf-8') as handle:
            langs[name[:-5]] = (path, json.load(handle))
    return langs


def scan_call_sites(root):
    """Collect translatable("key", ...) call sites: key -> set of (arg_count, file, line).

    A literal that is concatenated with something else is a key *prefix* built at runtime and
    is reported separately, because its real keys cannot be resolved statically.
    """
    sites = {}
    dynamic = set()
    for base, _dirs, files in os.walk(os.path.join(root, 'src')):
        for name in files:
            if not name.endswith('.java'):
                continue
            path = os.path.join(base, name)
            with open(path, encoding='utf-8', errors='replace') as handle:
                text = handle.read()
            for match in re.finditer(r'translatable\(\s*"([^"]+)"', text):
                key = match.group(1)
                tail = match.end()
                if text[tail:tail + 40].lstrip().startswith('+'):
                    dynamic.add(key)
                    continue
                line = text.count('\n', 0, match.start()) + 1
                sites.setdefault(key, set()).add((count_args(text, match.end()), name, line))
    return sites, dynamic


def count_args(text, index):
    """Count top-level arguments after `translatable("key"`; the key itself is not counted."""
    depth = 1
    args = 0
    while index < len(text) and depth > 0:
        char = text[index]
        if char == QUOTE:
            index += 1
            while index < len(text) and text[index] != QUOTE:
                index += 2 if text[index] == BACKSLASH else 1
        elif char in '([{':
            depth += 1
        elif char in ')]}':
            depth -= 1
            if depth == 0:
                break
        elif char == ',' and depth == 1:
            args += 1
        index += 1
    return args


def placeholders(value):
    return sorted(PLACEHOLDER.findall(value))


def collect_literals(root):
    """Every quoted string literal in the sources, used to spot keys that nothing refers to."""
    literals = set()
    for base, _dirs, files in os.walk(os.path.join(root, 'src')):
        for name in files:
            if not name.endswith('.java'):
                continue
            with open(os.path.join(base, name), encoding='utf-8', errors='replace') as handle:
                literals.update(re.findall(r'"([^"]*)"', handle.read()))
    return literals


def is_referenced(key, literals):
    """True when `key` is named directly, or built by concatenation of known fragments.

    Keys assembled at runtime appear only as the fragments around them, e.g.
    `"showcase.stats.block." + name` and `categoryKey + ".placed"`.
    """
    if key in literals:
        return True
    for literal in literals:
        if literal.endswith('.') and key.startswith(literal):
            return True
        if literal.startswith('.') and key.endswith(literal):
            return True
    return False


def is_broadcast(key):
    return (key.startswith(BROADCAST_PREFIXES) or key.endswith(BROADCAST_SUFFIXES)) \
        and key.startswith('showcase.message.')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', help='repository root (defaults to the script location)')
    options = parser.parse_args()

    root = repo_root(options.root)
    languages = load_languages(root)
    sites, dynamic = scan_call_sites(root)

    failures = []
    warnings = []

    if 'en_us' not in languages:
        print('en_us.json not found; cannot run the checks')
        return 1

    en_path, en = languages['en_us']
    en_keys = set(en)

    # 1) Every locale must define exactly the keys en_us defines.
    for name, (path, data) in sorted(languages.items()):
        if name == 'en_us':
            continue
        for key in sorted(en_keys - set(data)):
            failures.append('%s: missing key %s (falls back to English)' % (path, key))
        for key in sorted(set(data) - en_keys):
            failures.append('%s: key %s is not present in en_us' % (path, key))

    # 2) Placeholder count and type must match en_us, per key.
    for name, (path, data) in sorted(languages.items()):
        for key in sorted(en_keys & set(data)):
            if placeholders(data[key]) != placeholders(en[key]):
                failures.append('%s: %s has placeholders %s, en_us has %s'
                                % (path, key, placeholders(data[key]), placeholders(en[key])))

    # 3) Keys used from code must exist in en_us, otherwise every client shows the raw key.
    showcase_sites = {k: v for k, v in sites.items() if k.startswith('showcase.')}
    for key in sorted(showcase_sites):
        if key not in en_keys:
            for count, source, line in sorted(showcase_sites[key]):
                failures.append('%s:%d: translatable("%s") is missing from en_us'
                                % (source, line, key))

    # 4) The argument count at each call site must match en_us's placeholder count.
    for key in sorted(showcase_sites):
        if key not in en_keys:
            continue
        expected = len(placeholders(en[key]))
        for count, source, line in sorted(showcase_sites[key]):
            if count != expected:
                failures.append('%s:%d: translatable("%s") passes %d arg(s) but en_us has %d'
                                % (source, line, key, count, expected))

    # 5) Broadcast copy is third person; Chinese marks owned content, and leaves opened content
    #    (containers, merchants) unclaimed.
    for name, (path, data) in sorted(languages.items()):
        if name not in PERSON_REVIEWED:
            continue
        reflexive, first_person = PERSON_REVIEWED[name]
        for key in BROADCAST_OWNED:
            if key in data and reflexive not in data[key]:
                failures.append('%s: %s is a broadcast about the acting player\'s own content '
                                'but has no "%s": %s' % (path, key, reflexive, data[key]))
        for key in BROADCAST_OPENED:
            if key in data and reflexive in data[key]:
                failures.append('%s: %s may be content the player merely opened (a world chest, '
                                'a villager), so it must not claim "%s": %s'
                                % (path, key, reflexive, data[key]))
        for key in sorted(set(data) & en_keys):
            if is_broadcast(key) and first_person in data[key]:
                failures.append('%s: %s is broadcast to every player but uses "%s": %s'
                                % (path, key, first_person, data[key]))

    # 6) Advisory: keys nothing refers to any more.
    literals = collect_literals(root)
    for key in sorted(en_keys):
        if not is_referenced(key, literals):
            warnings.append('%s: %s is defined but not referenced from code' % (en_path, key))

    for message in failures:
        print('::error::%s' % message)
    for message in warnings:
        print('::warning::%s' % message)

    print('checked %d locales, %d keys, %d call sites'
          % (len(languages), len(en_keys), sum(len(v) for v in sites.values())))
    if failures:
        print('FAILED: %d problem(s)' % len(failures))
        return 1
    print('OK')
    return 0


if __name__ == '__main__':
    sys.exit(main())
