#!/usr/bin/env python3
from __future__ import annotations
import json
import re
import sys
from pathlib import Path
import struct

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
errors: list[str] = []
notes: list[str] = []

# JSON syntax
json_files = sorted(RES.rglob("*.json")) + [RES / "pack.mcmeta"]
for path in json_files:
    try:
        json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"JSON parse failed: {path.relative_to(ROOT)}: {exc}")
notes.append(f"JSON checked: {len(json_files)} files")

# No stale namespace after the deliberate rename creativecore -> creationcore.
text_suffixes = {".java", ".json", ".toml", ".gradle", ".properties", ".md", ".mcmeta", ".yml", ".yaml"}
for path in ROOT.rglob("*"):
    if path.is_file() and path.suffix in text_suffixes:
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        if "creativecore" in text:
            errors.append(f"Stale creativecore namespace/name in {path.relative_to(ROOT)}")

# Texture dimensions from the PNG IHDR (stdlib only, so CI needs no Python packages).
textures = sorted((RES / "assets/creationcore/textures").rglob("*.png"))
for path in textures:
    try:
        raw = path.read_bytes()
        if raw[:8] != b"\x89PNG\r\n\x1a\n" or raw[12:16] != b"IHDR":
            raise ValueError("not a PNG with an IHDR header")
        width, height = struct.unpack(">II", raw[16:24])
        if (width, height) != (16, 16):
            errors.append(f"Texture is not 16x16: {path.relative_to(ROOT)} -> {(width, height)}")
    except Exception as exc:
        errors.append(f"Texture failed to inspect: {path.relative_to(ROOT)}: {exc}")
notes.append(f"Textures checked: {len(textures)} files")

# Resolve model texture references belonging to creationcore.
model_files = sorted((RES / "assets/creationcore/models").rglob("*.json"))
for model in model_files:
    data = json.loads(model.read_text(encoding="utf-8"))
    for ref in (data.get("textures") or {}).values():
        if not isinstance(ref, str) or ref.startswith("#") or not ref.startswith("creationcore:"):
            continue
        rel = ref.split(":", 1)[1]
        tex = RES / "assets/creationcore/textures" / f"{rel}.png"
        if not tex.exists():
            errors.append(f"Missing texture referenced by {model.relative_to(ROOT)}: {ref}")

# Resolve blockstate model references belonging to creationcore.
for state_file in sorted((RES / "assets/creationcore/blockstates").glob("*.json")):
    data = json.loads(state_file.read_text(encoding="utf-8"))
    variants = data.get("variants", {})
    for value in variants.values():
        values = value if isinstance(value, list) else [value]
        for entry in values:
            ref = entry.get("model") if isinstance(entry, dict) else None
            if isinstance(ref, str) and ref.startswith("creationcore:"):
                rel = ref.split(":", 1)[1]
                target = RES / "assets/creationcore/models" / f"{rel}.json"
                if not target.exists():
                    errors.append(f"Missing model referenced by {state_file.relative_to(ROOT)}: {ref}")

# Expected core files / recipes / compatibility tag.
expected = [
    RES / "data/creationcore/recipe/blank_matter.json",
    RES / "data/creationcore/recipe/cow_spawn_egg.json",
    RES / "data/creationcore/recipe/creative_crafting_table.json",
    RES / "data/creationcore/recipe/void_bottling.json",
    RES / "data/creationcore/tags/item/creative_core_containers.json",
    ROOT / ".github/workflows/build.yml",
]
for path in expected:
    if not path.exists():
        errors.append(f"Missing expected file: {path.relative_to(ROOT)}")

# Make sure all 17 vanilla shulker boxes are present in the compatibility tag.
tag_path = RES / "data/creationcore/tags/item/creative_core_containers.json"
if tag_path.exists():
    vals = json.loads(tag_path.read_text(encoding="utf-8")).get("values", [])
    if len(vals) != 17 or len(set(vals)) != 17:
        errors.append(f"creative_core_containers should contain 17 unique vanilla shulker boxes, found {len(vals)}")

# Recipe sanity assertions for the v0.1 agreed placeholders.
blank = json.loads((RES / "data/creationcore/recipe/blank_matter.json").read_text(encoding="utf-8"))
if blank.get("result", {}).get("id") != "creationcore:blank_matter" or blank.get("ingredients") != [{"item": "minecraft:paper"}]:
    errors.append("Blank Matter placeholder recipe is not exactly paper -> blank_matter")

cow = json.loads((RES / "data/creationcore/recipe/cow_spawn_egg.json").read_text(encoding="utf-8"))
if cow.get("pattern") != ["LBL", "BEB", "LBL"] or cow.get("result", {}).get("id") != "minecraft:cow_spawn_egg":
    errors.append("Creative Crafting cow spawn egg placeholder recipe does not match the agreed pattern")

smith = json.loads((RES / "data/creationcore/recipe/creative_crafting_table.json").read_text(encoding="utf-8"))
if not (smith.get("template", {}).get("item") == "creationcore:creative_core"
        and smith.get("base", {}).get("item") == "minecraft:crafting_table"
        and smith.get("addition", {}).get("item") == "minecraft:netherite_block"):
    errors.append("Creative Crafting Table smithing recipe does not match the agreed three slots")

# Obvious TODO/FIXME markers are useful to surface rather than silently ship.
markers = []
for path in (ROOT / "src/main/java").rglob("*.java"):
    text = path.read_text(encoding="utf-8")
    for n, line in enumerate(text.splitlines(), 1):
        if "TODO" in line or "FIXME" in line:
            markers.append(f"{path.relative_to(ROOT)}:{n}: {line.strip()}")
if markers:
    notes.append("TODO/FIXME markers:\n  " + "\n  ".join(markers))

print("Creation Core v0.1 static resource validation")
for note in notes:
    print("[INFO]", note)
if errors:
    print(f"[FAIL] {len(errors)} problem(s):")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print("[PASS] Resource/static checks passed.")
