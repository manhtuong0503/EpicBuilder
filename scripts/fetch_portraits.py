#!/usr/bin/env python3
"""Tải ảnh chân dung hero từ các nguồn cộng đồng Epic Seven vào assets/portraits.

Chạy trong CI (GitHub Actions) trước bước build APK. Hero nào không tải được
sẽ bị bỏ qua — app tự fallback về avatar chữ cái, nên script không làm fail build.
"""
import json
import os
import sys
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HEROES_JSON = os.path.join(ROOT, "app/src/main/assets/heroes.json")
OUT_DIR = os.path.join(ROOT, "app/src/main/assets/portraits")

# Một số id trong app khác slug trên các trang cộng đồng
ALIASES = {
    "ainz": ["ainz-ooal-gown", "ainz"],
}

SOURCES = [
    "https://epic7db.com/images/heroes/{slug}.png",
    "https://epic7db.com/images/heroes/{slug}.webp",
    "https://assets.epicsevendb.com/hero/{slug}/icon.png",
    "https://epic7x.com/wp-content/uploads/characters/{slug}.png",
]

HEADERS = {
    "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/126.0 Safari/537.36",
    "Accept": "image/webp,image/png,image/*,*/*;q=0.8",
}


def try_download(url: str) -> bytes | None:
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            if resp.status != 200:
                return None
            data = resp.read()
            content_type = resp.headers.get("Content-Type", "")
            if len(data) < 500 or "image" not in content_type:
                return None
            return data
    except Exception:
        return None


def main() -> int:
    with open(HEROES_JSON, encoding="utf-8") as f:
        heroes = json.load(f)["heroes"]
    os.makedirs(OUT_DIR, exist_ok=True)

    ok, missing = [], []
    for hero in heroes:
        hero_id = hero["id"]
        existing = [
            p for p in (f"{hero_id}.png", f"{hero_id}.webp")
            if os.path.exists(os.path.join(OUT_DIR, p))
        ]
        if existing:
            ok.append(hero_id)
            continue

        slugs = ALIASES.get(hero_id, [hero_id])
        data, ext = None, "png"
        for slug in slugs:
            for pattern in SOURCES:
                url = pattern.format(slug=slug)
                data = try_download(url)
                if data:
                    ext = "webp" if url.endswith(".webp") else "png"
                    print(f"  [OK] {hero_id} <- {url}")
                    break
            if data:
                break

        if data:
            with open(os.path.join(OUT_DIR, f"{hero_id}.{ext}"), "wb") as f:
                f.write(data)
            ok.append(hero_id)
        else:
            missing.append(hero_id)
            print(f"  [MISS] {hero_id}")

    print(f"\nPortraits: {len(ok)}/{len(heroes)} downloaded, {len(missing)} missing")
    if missing:
        print("Missing:", ", ".join(missing))
    return 0


if __name__ == "__main__":
    sys.exit(main())
