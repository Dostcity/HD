#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dusuk frekansli, kisisel olcekli fiyat tarayici.
scraper/products.json icindeki urunleri Trendyol'dan kazir,
sonuclari kok dizine prices.json (uygulamanin okuyacagi) ve
scraper/last_run.json (hata ayiklama) olarak yazar.
Sadece Python standart kutuphanesi kullanir (pip gerekmez).
"""
import json
import re
import time
import random
import urllib.request
import urllib.error
from datetime import datetime, timezone

CONFIG = "scraper/products.json"
OUT_PRICES = "prices.json"
OUT_DEBUG = "scraper/last_run.json"

UA = ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
HEADERS = {
    "User-Agent": UA,
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
    "Cache-Control": "no-cache",
}


def now_iso():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def fetch(url):
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            return getattr(r, "status", 200), r.read().decode("utf-8", "ignore")
    except urllib.error.HTTPError as e:
        body = ""
        try:
            body = e.read().decode("utf-8", "ignore")
        except Exception:
            pass
        return e.code, body
    except Exception as e:
        return -1, "EXC: " + str(e)


def looks_blocked(status, html):
    if status in (401, 403, 429, 503):
        return True
    low = html.lower()
    for kw in ("datadome", "px-captcha", "are you a human", "access denied",
               "/security/", "captcha"):
        if kw in low:
            return True
    return False


def extract_price(html):
    # 1) Trendyol baslangic durumu: discountedPrice / sellingPrice {value: N}
    for key in ("discountedPrice", "sellingPrice", "originalPrice"):
        m = re.search(r'"' + key + r'"\s*:\s*\{[^{}]*?"value"\s*:\s*([0-9]+(?:\.[0-9]+)?)', html)
        if m:
            try:
                return float(m.group(1)), key
            except ValueError:
                pass
    # 2) JSON-LD offers.price
    for m in re.finditer(
            r'<script[^>]*type="application/ld\+json"[^>]*>(.*?)</script>',
            html, re.DOTALL | re.IGNORECASE):
        try:
            data = json.loads(m.group(1).strip())
        except Exception:
            continue
        items = data if isinstance(data, list) else [data]
        for d in items:
            if not isinstance(d, dict):
                continue
            offers = d.get("offers")
            offer_list = offers if isinstance(offers, list) else [offers]
            for o in offer_list:
                if isinstance(o, dict) and o.get("price") is not None:
                    try:
                        return float(str(o["price"]).replace(",", ".")), "jsonld"
                    except ValueError:
                        pass
    # 3) meta product:price:amount
    m = re.search(r'<meta[^>]+(?:property|name)="product:price:amount"[^>]+content="([0-9.,]+)"', html)
    if m:
        v = m.group(1)
        try:
            if "," in v and "." in v:
                v = v.replace(".", "").replace(",", ".")
            elif "," in v:
                v = v.replace(",", ".")
            return float(v), "meta"
        except ValueError:
            pass
    return None, None


def main():
    with open(CONFIG, "r", encoding="utf-8") as f:
        products = json.load(f)

    out_products = []
    debug = {"ranAt": now_iso(), "results": []}

    for p in products:
        prices = []
        ty_url = p.get("trendyol")
        if ty_url:
            status, html = fetch(ty_url)
            time.sleep(random.uniform(1.0, 2.5))
            blocked = looks_blocked(status, html) if isinstance(html, str) else True
            price, method = (None, None)
            if isinstance(html, str) and not blocked:
                price, method = extract_price(html)
            debug["results"].append({
                "key": p.get("key"),
                "market": "Trendyol",
                "status": status,
                "blocked": blocked,
                "price": price,
                "method": method,
                "htmlLen": len(html) if isinstance(html, str) else 0,
            })
            if price is not None:
                prices.append({
                    "market": "Trendyol",
                    "price": price,
                    "url": ty_url,
                    "currency": "TL",
                })
        out_products.append({
            "query": p.get("query"),
            "name": p.get("name"),
            "prices": prices,
        })

    result = {"updatedAt": now_iso(), "products": out_products}
    with open(OUT_PRICES, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    with open(OUT_DEBUG, "w", encoding="utf-8") as f:
        json.dump(debug, f, ensure_ascii=False, indent=2)

    print("Tarama sonucu:", json.dumps(debug, ensure_ascii=False))


if __name__ == "__main__":
    main()
