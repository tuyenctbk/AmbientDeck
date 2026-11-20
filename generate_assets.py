#!/usr/bin/env python3
"""
AmbientDeck Asset Generator
Generates:
1. High-Resolution App Icon (512x512) for Google Play Store & Android
2. Feature Graphic (1024x500) for Google Play Store Listing
3. Android TV Banners: 1920x1080, 1280x720, and 320x180
4. In-App Android Resources across all required densities
"""

import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def get_font(size, bold=False):
    # Try high-quality system fonts on macOS
    candidates = []
    if bold:
        candidates = [
            "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
            "/System/Library/Fonts/Supplemental/Futura.ttc",
            "/System/Library/Fonts/SFNSMono.ttf",
            "/System/Library/Fonts/Supplemental/Verdana Bold.ttf",
            "/System/Library/Fonts/HelveticaNeue.ttc",
        ]
    else:
        candidates = [
            "/System/Library/Fonts/Supplemental/Arial.ttf",
            "/System/Library/Fonts/SFNSMono.ttf",
            "/System/Library/Fonts/Supplemental/Verdana.ttf",
            "/System/Library/Fonts/HelveticaNeue.ttc",
        ]
    for path in candidates:
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size)
            except Exception:
                continue
    return ImageFont.load_default()

def draw_cyber_grid(draw, width, height, step=32, color=(0, 229, 255, 18)):
    for x in range(0, width, step):
        draw.line([(x, 0), (x, height)], fill=color, width=1)
    for y in range(0, height, step):
        draw.line([(0, y), (width, y)], fill=color, width=1)

def draw_rounded_rect(draw, bbox, radius, fill=None, outline=None, width=1):
    draw.rounded_rectangle(bbox, radius=radius, fill=fill, outline=outline, width=width)

def generate_monitor_icon(size=512):
    """
    Renders the AmbientDeck TV / Monitor graphic with high precision.
    Keeps all elements strictly within safe circle diameter = 62% of size
    (radius = 0.31 * 512 = 158px from center 256, 256).
    """
    if size != 512:
        master = generate_monitor_icon(512)
        return master.resize((size, size), Image.Resampling.LANCZOS)

    canvas_size = 1024
    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    cx, cy = canvas_size // 2, canvas_size // 2

    # 1. Dark Void Radial Gradient Background
    for r in range(cx, 0, -2):
        factor = r / cx
        # Gradient from deep sapphire (#0B172A) at center to void black (#030508) at edges
        red = int(3 + 12 * (1 - factor))
        green = int(5 + 22 * (1 - factor))
        blue = int(8 + 38 * (1 - factor))
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(red, green, blue, 255))

    # 2. Subtle Precision Grid
    draw_cyber_grid(draw, canvas_size, canvas_size, step=64, color=(0, 229, 255, 16))

    # 3. Outer Radial Neon Pulse behind monitor
    glow_img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_img)
    glow_r = int(cx * 0.58)
    for gr in range(glow_r, 0, -4):
        alpha = int(45 * (1 - gr / glow_r))
        glow_draw.ellipse([cx - gr, cy - gr, cx + gr, cy + gr], fill=(0, 229, 255, alpha))
    glow_img = glow_img.filter(ImageFilter.GaussianBlur(16))
    img.alpha_composite(glow_img)
    draw = ImageDraw.Draw(img)

    # 4. Monitor Dimension Specifications (Strictly safe within circle radius cx * 0.60)
    # At canvas_size = 1024, safe radius = 1024 * 0.30 = 307px
    mw = int(canvas_size * 0.44)  # 450px wide
    mh = int(canvas_size * 0.28)  # 286px tall
    m_top = cy - int(mh * 0.65)
    m_left = cx - mw // 2
    m_right = m_left + mw
    m_bottom = m_top + mh

    # Stand dimensions
    stand_w = int(canvas_size * 0.05)
    stand_h = int(canvas_size * 0.07)
    base_w = int(canvas_size * 0.22)
    base_h = int(canvas_size * 0.022)

    stand_top = m_bottom
    stand_bottom = stand_top + stand_h
    base_top = stand_bottom
    base_bottom = base_top + base_h

    # Draw Stand & Base
    draw.rectangle([cx - stand_w//2, stand_top, cx + stand_w//2, stand_bottom], fill=(0, 229, 255, 255))
    draw_rounded_rect(draw, [cx - base_w//2, base_top, cx + base_w//2, base_bottom], radius=6, fill=(0, 229, 255, 255))
    # Emerald neon status accent on base
    inner_base_h = max(2, int(base_h * 0.5))
    inner_base_top = (base_top + base_bottom) // 2 - inner_base_h // 2
    inner_base_bottom = inner_base_top + inner_base_h
    draw_rounded_rect(draw, [cx - int(base_w*0.35), inner_base_top, cx + int(base_w*0.35), inner_base_bottom], radius=max(1, inner_base_h//2), fill=(0, 230, 118, 255))

    # Outer Monitor Bezel (Chiseled Cyber Blue)
    draw_rounded_rect(draw, [m_left, m_top, m_right, m_bottom], radius=24, fill=(24, 36, 56, 255), outline=(0, 229, 255, 220), width=4)

    # Inner Screen Area
    screen_pad = 12
    s_left = m_left + screen_pad
    s_top = m_top + screen_pad
    s_right = m_right - screen_pad
    s_bottom = m_bottom - screen_pad
    draw_rounded_rect(draw, [s_left, s_top, s_right, s_bottom], radius=16, fill=(8, 14, 24, 255), outline=(0, 229, 255, 120), width=2)

    # Widgets Inside Screen:
    sw = s_right - s_left
    sh = s_bottom - s_top

    # --- Widget 1: Top-Left Ambient Clock & Golden Sun ---
    sun_x = s_left + int(sw * 0.16)
    sun_y = s_top + int(sh * 0.26)
    sun_r = int(sh * 0.13)
    draw.ellipse([sun_x - sun_r, sun_y - sun_r, sun_x + sun_r, sun_y + sun_r], fill=(255, 179, 0, 255))
    # Clock digits bar
    bar_x = sun_x + sun_r + 14
    draw_rounded_rect(draw, [bar_x, sun_y - 12, bar_x + int(sw * 0.22), sun_y - 2], radius=4, fill=(0, 229, 255, 255))
    draw_rounded_rect(draw, [bar_x, sun_y + 4, bar_x + int(sw * 0.15), sun_y + 12], radius=3, fill=(129, 212, 250, 200))

    # --- Widget 2: Top-Right Radar Scanner ---
    radar_cx = s_right - int(sw * 0.22)
    radar_cy = s_top + int(sh * 0.28)
    radar_r = int(sh * 0.20)
    # Range rings
    draw.ellipse([radar_cx - radar_r, radar_cy - radar_r, radar_cx + radar_r, radar_cy + radar_r], outline=(0, 230, 118, 200), width=2)
    draw.ellipse([radar_cx - radar_r//2, radar_cy - radar_r//2, radar_cx + radar_r//2, radar_cy + radar_r//2], outline=(0, 230, 118, 120), width=1)
    # Crosshairs
    draw.line([(radar_cx - radar_r, radar_cy), (radar_cx + radar_r, radar_cy)], fill=(0, 230, 118, 140), width=1)
    draw.line([(radar_cx, radar_cy - radar_r), (radar_cx, radar_cy + radar_r)], fill=(0, 230, 118, 140), width=1)
    # Aircraft blip & sweep ping
    blip_x = radar_cx + int(radar_r * 0.45)
    blip_y = radar_cy - int(radar_r * 0.45)
    draw.polygon([(blip_x, blip_y - 6), (blip_x + 6, blip_y + 6), (blip_x, blip_y + 3), (blip_x - 6, blip_y + 6)], fill=(0, 229, 255, 255))

    # --- Widget 3: Bottom-Left Crypto Sparkline ---
    chart_x0 = s_left + int(sw * 0.08)
    chart_y0 = s_bottom - int(sh * 0.12)
    chart_w = int(sw * 0.38)
    chart_h = int(sh * 0.30)
    # Points for a surge chart
    pts = [
        (chart_x0, chart_y0),
        (chart_x0 + int(chart_w * 0.25), chart_y0 - int(chart_h * 0.35)),
        (chart_x0 + int(chart_w * 0.50), chart_y0 - int(chart_h * 0.20)),
        (chart_x0 + int(chart_w * 0.75), chart_y0 - int(chart_h * 0.85)),
        (chart_x0 + chart_w, chart_y0 - chart_h)
    ]
    # Draw filled gradient area under sparkline
    fill_poly = [(pts[0][0], chart_y0)] + pts + [(pts[-1][0], chart_y0)]
    draw.polygon(fill_poly, fill=(0, 230, 118, 45))
    for i in range(len(pts) - 1):
        draw.line([pts[i], pts[i+1]], fill=(0, 230, 118, 255), width=4)
    # Pulse dot at peak
    last_pt = pts[-1]
    draw.ellipse([last_pt[0]-5, last_pt[1]-5, last_pt[0]+5, last_pt[1]+5], fill=(0, 230, 118, 255))

    # --- Widget 4: Bottom-Right History / Telemetry Cards ---
    card_x = s_right - int(sw * 0.42)
    card_y = s_bottom - int(sh * 0.42)
    card_w = int(sw * 0.36)
    card_h = int(sh * 0.34)
    draw_rounded_rect(draw, [card_x, card_y, card_x + card_w, card_y + card_h], radius=8, fill=(20, 32, 50, 220), outline=(0, 229, 255, 90), width=1)
    # Telemetry data lines inside card
    draw_rounded_rect(draw, [card_x + 8, card_y + 8, card_x + 36, card_y + 16], radius=3, fill=(255, 179, 0, 255))
    draw_rounded_rect(draw, [card_x + 42, card_y + 9, card_x + card_w - 8, card_y + 15], radius=2, fill=(129, 212, 250, 220))
    draw_rounded_rect(draw, [card_x + 8, card_y + 22, card_x + card_w - 12, card_y + 28], radius=2, fill=(129, 212, 250, 160))

    # Downsample to target size with high-quality Lanczos resampling
    final_img = img.resize((size, size), Image.Resampling.LANCZOS)
    return final_img

def generate_tv_banner(width=1280, height=720):
    """
    Renders 16:9 Android TV Banner for Leanback launcher and Play Store.
    Combines glowing monitor emblem, crisp AmbientDeck typography, and tagline badges.
    """
    img = Image.new("RGBA", (width, height), (4, 6, 10, 255))
    draw = ImageDraw.Draw(img)

    # 1. Background Cinematic Dark Gradient & Cyber Grid
    for y in range(height):
        # Vertical gradient: deep dark at top/bottom, slight sapphire tint at middle
        factor = abs(y - height // 2) / (height // 2)
        r = int(5 + 10 * (1 - factor))
        g = int(8 + 18 * (1 - factor))
        b = int(14 + 32 * (1 - factor))
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))

    draw_cyber_grid(draw, width, height, step=int(height * 0.08), color=(0, 229, 255, 14))

    # Top & Bottom glowing neon accent borders
    draw.line([(0, 0), (width, 0)], fill=(0, 229, 255, 180), width=int(height * 0.008))
    draw.line([(0, height - 1), (width, height - 1)], fill=(0, 230, 118, 160), width=int(height * 0.008))

    # 2. Embed Glowing Monitor Graphic on Left Side
    icon_size = int(height * 0.72)
    icon_img = generate_monitor_icon(icon_size)
    icon_x = int(width * 0.07)
    icon_y = (height - icon_size) // 2
    img.paste(icon_img, (icon_x, icon_y), icon_img)

    # 3. Typography on Right Side
    text_x = icon_x + icon_size + int(width * 0.04)

    title_font_size = int(height * 0.19)
    sub_font_size = int(height * 0.052)
    badge_font_size = int(height * 0.038)

    title_font = get_font(title_font_size, bold=True)
    sub_font = get_font(sub_font_size, bold=True)
    badge_font = get_font(badge_font_size, bold=False)

    title_y = int(height * 0.22)

    # Draw Title with subtle cyan shadow glow
    draw.text((text_x + 3, title_y + 3), "AmbientDeck", fill=(0, 229, 255, 120), font=title_font)
    draw.text((text_x, title_y), "AmbientDeck", fill=(255, 255, 255, 255), font=title_font)

    # Subtitle / Tagline
    sub_y = title_y + int(height * 0.24)
    draw.text((text_x, sub_y), "SMART TV & AMBIENT WALL DASHBOARD", fill=(0, 229, 255, 240), font=sub_font)

    # Feature Badges Row
    badges_y = sub_y + int(height * 0.10)
    badges = ["4K Art", "Weather & AQI", "Crypto Tickers", "Flight Radar", "Voice Control"]
    badge_x = text_x
    for badge in badges:
        # Measure text
        bbox = draw.textbbox((badge_x, badges_y), badge, font=badge_font)
        bw = bbox[2] - bbox[0] + 16
        bh = bbox[3] - bbox[1] + 10
        draw_rounded_rect(draw, [badge_x, badges_y - 2, badge_x + bw, badges_y + bh - 2], radius=6, fill=(15, 26, 44, 220), outline=(0, 229, 255, 100), width=1)
        draw.text((badge_x + 8, badges_y), badge, fill=(200, 235, 255, 240), font=badge_font)
        badge_x += bw + int(width * 0.012)

    return img

def generate_feature_graphic(width=1024, height=500):
    """
    Renders 1024x500 Feature Graphic for Google Play Store Listing.
    """
    img = Image.new("RGBA", (width, height), (5, 8, 14, 255))
    draw = ImageDraw.Draw(img)

    # 1. Cosmic Aurora Gradient & Deep Void Background
    for x in range(width):
        factor = x / width
        r = int(4 + 12 * factor)
        g = int(6 + 24 * factor)
        b = int(12 + 48 * factor)
        draw.line([(x, 0), (x, height)], fill=(r, g, b, 255))

    draw_cyber_grid(draw, width, height, step=40, color=(0, 229, 255, 16))

    # Ambient Aurora Waves in Background
    aurora_img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    aurora_draw = ImageDraw.Draw(aurora_img)
    aurora_draw.ellipse([width * 0.45, -height * 0.3, width * 1.1, height * 0.9], fill=(0, 230, 118, 25))
    aurora_draw.ellipse([width * 0.1, height * 0.4, width * 0.8, height * 1.2], fill=(0, 229, 255, 30))
    aurora_img = aurora_img.filter(ImageFilter.GaussianBlur(50))
    img.alpha_composite(aurora_img)
    draw = ImageDraw.Draw(img)

    # Top & Bottom Glowing Border
    draw.line([(0, 0), (width, 0)], fill=(0, 229, 255, 200), width=4)
    draw.line([(0, height - 1), (width, height - 1)], fill=(0, 230, 118, 180), width=4)

    # 2. Left Side: Brand Logo & Feature List
    logo_size = 140
    logo_img = generate_monitor_icon(logo_size)
    logo_x = 48
    logo_y = 50
    img.paste(logo_img, (logo_x, logo_y), logo_img)

    # App Title & Subtitle next to Logo
    title_font = get_font(52, bold=True)
    sub_font = get_font(20, bold=True)
    body_font = get_font(15, bold=False)
    tag_font = get_font(13, bold=True)

    title_x = logo_x + logo_size + 24
    draw.text((title_x + 2, logo_y + 12), "AmbientDeck", fill=(0, 229, 255, 140), font=title_font)
    draw.text((title_x, logo_y + 10), "AmbientDeck", fill=(255, 255, 255, 255), font=title_font)

    draw.text((title_x, logo_y + 74), "SMART TV & WALL DASHBOARD", fill=(0, 229, 255, 255), font=sub_font)
    draw.text((title_x, logo_y + 104), "Transforms your Smart TV into a stunning ambient kiosk", fill=(170, 190, 215, 220), font=body_font)

    # Feature Bullet Pillars
    pillars_y = logo_y + logo_size + 40
    features = [
        ("✦ Live 4K Art & Sceneries", "Nordic Aurora, Mountains, Coastal & Aerials"),
        ("✦ Real-Time Weather & Air Quality", "Live temp, humidity, UV & AQI index"),
        ("✦ Crypto Ticker & Sparklines", "BTC, ETH, SOL streaming market data"),
        ("✦ Overhead Flight Radar", "Live transponder ADS-B tracking"),
        ("✦ D-Pad Remote & Voice Controls", "Hands-free smart TV experience")
    ]

    fy = pillars_y
    for title, desc in features:
        draw.text((logo_x + 6, fy), title, fill=(0, 230, 118, 255), font=get_font(15, bold=True))
        draw.text((logo_x + 300, fy + 1), desc, fill=(160, 185, 210, 200), font=get_font(13, bold=False))
        fy += 34

    # Platform Badges at Bottom
    badge_y = height - 48
    draw_rounded_rect(draw, [logo_x, badge_y, logo_x + 150, badge_y + 26], radius=6, fill=(18, 30, 48, 220), outline=(0, 229, 255, 120), width=1)
    draw.text((logo_x + 12, badge_y + 4), "GOOGLE TV READY", fill=(0, 229, 255, 255), font=tag_font)

    draw_rounded_rect(draw, [logo_x + 162, badge_y, logo_x + 322, badge_y + 26], radius=6, fill=(18, 30, 48, 220), outline=(0, 230, 118, 120), width=1)
    draw.text((logo_x + 174, badge_y + 4), "1080P & 4K RESOLUTION", fill=(0, 230, 118, 255), font=tag_font)

    # 3. Right Side: Stylized Cinematic Dashboard Mockup Window
    mockup_x = 640
    mockup_y = 60
    mockup_w = 345
    mockup_h = 380

    # Window container
    draw_rounded_rect(draw, [mockup_x, mockup_y, mockup_x + mockup_w, mockup_y + mockup_h], radius=18, fill=(12, 18, 30, 230), outline=(0, 229, 255, 160), width=2)
    # Header bar
    draw_rounded_rect(draw, [mockup_x, mockup_y, mockup_x + mockup_w, mockup_y + 36], radius=18, fill=(18, 28, 44, 255))
    draw.rectangle([mockup_x, mockup_y + 18, mockup_x + mockup_w, mockup_y + 36], fill=(18, 28, 44, 255))
    draw.line([(mockup_x, mockup_y + 36), (mockup_x + mockup_w, mockup_y + 36)], fill=(0, 229, 255, 100), width=1)
    # Window dots
    for i, dot_color in enumerate([(255, 95, 87), (254, 188, 46), (40, 200, 64)]):
        draw.ellipse([mockup_x + 14 + i * 14, mockup_y + 13, mockup_x + 22 + i * 14, mockup_y + 21], fill=dot_color)
    draw.text((mockup_x + 65, mockup_y + 10), "AmbientDeck Live HUD", fill=(180, 205, 230, 220), font=get_font(12, bold=True))

    # Mockup Content: Giant Clock Widget
    clock_card_y = mockup_y + 46
    draw_rounded_rect(draw, [mockup_x + 12, clock_card_y, mockup_x + mockup_w - 12, clock_card_y + 90], radius=12, fill=(18, 26, 42, 220), outline=(0, 229, 255, 80), width=1)
    draw.text((mockup_x + 24, clock_card_y + 14), "10:42", fill=(255, 255, 255, 255), font=get_font(38, bold=True))
    draw.text((mockup_x + 138, clock_card_y + 24), ":38", fill=(0, 229, 255, 255), font=get_font(20, bold=True))
    draw.text((mockup_x + 24, clock_card_y + 60), "Wednesday, November 20 · New York 72°F", fill=(140, 165, 195, 220), font=get_font(11, bold=False))

    # Mockup Content: Flight Radar Widget & Weather AQI
    middle_y = clock_card_y + 100
    mid_w = (mockup_w - 30) // 2
    # Radar card
    draw_rounded_rect(draw, [mockup_x + 12, middle_y, mockup_x + 12 + mid_w, middle_y + 100], radius=12, fill=(14, 24, 38, 220), outline=(0, 230, 118, 80), width=1)
    draw.text((mockup_x + 22, middle_y + 10), "FLIGHT RADAR", fill=(0, 230, 118, 255), font=get_font(10, bold=True))
    rcx = mockup_x + 12 + mid_w // 2
    rcy = middle_y + 55
    rr = 28
    draw.ellipse([rcx - rr, rcy - rr, rcx + rr, rcy + rr], outline=(0, 230, 118, 180), width=1)
    draw.line([(rcx - rr, rcy), (rcx + rr, rcy)], fill=(0, 230, 118, 100), width=1)
    draw.line([(rcx, rcy - rr), (rcx, rcy + rr)], fill=(0, 230, 118, 100), width=1)
    draw.polygon([(rcx + 8, rcy - 12), (rcx + 14, rcy - 4), (rcx + 6, rcy - 6)], fill=(0, 229, 255, 255))

    # Weather AQI Card
    draw_rounded_rect(draw, [mockup_x + 18 + mid_w, middle_y, mockup_x + mockup_w - 12, middle_y + 100], radius=12, fill=(14, 24, 38, 220), outline=(255, 179, 0, 80), width=1)
    draw.text((mockup_x + 28 + mid_w, middle_y + 10), "AIR QUALITY", fill=(255, 179, 0, 255), font=get_font(10, bold=True))
    draw.text((mockup_x + 28 + mid_w, middle_y + 32), "AQI 24", fill=(0, 230, 118, 255), font=get_font(20, bold=True))
    draw.text((mockup_x + 28 + mid_w, middle_y + 60), "Good · PM2.5 Low", fill=(140, 165, 195, 220), font=get_font(10, bold=False))

    # Mockup Content: Crypto Ticker Tape
    crypto_y = middle_y + 110
    draw_rounded_rect(draw, [mockup_x + 12, crypto_y, mockup_x + mockup_w - 12, crypto_y + 90], radius=12, fill=(16, 24, 40, 220), outline=(0, 229, 255, 80), width=1)
    draw.text((mockup_x + 22, crypto_y + 10), "MARKET TELEMETRY", fill=(0, 229, 255, 255), font=get_font(10, bold=True))
    draw.text((mockup_x + 22, crypto_y + 32), "BTC $94,250", fill=(255, 255, 255, 255), font=get_font(15, bold=True))
    draw.text((mockup_x + 130, crypto_y + 34), "+4.2%", fill=(0, 230, 118, 255), font=get_font(13, bold=True))
    draw.text((mockup_x + 22, crypto_y + 58), "ETH $3,420 (+2.8%) · SOL $214 (+6.1%)", fill=(140, 165, 195, 220), font=get_font(10, bold=False))

    return img

def main():
    print("=== Generating Store Listing & In-App Assets ===")
    os.makedirs("store_listing", exist_ok=True)

    # 1. 512x512 Master App Icon
    print("Generating 512x512 App Icon...")
    icon_512 = generate_monitor_icon(512)
    icon_512.save("store_listing/icon_512.png", "PNG")
    icon_512.save("ic_launcher_512.png", "PNG")
    icon_512.save("app/src/main/res/drawable/ic_launcher_512.png", "PNG")
    icon_512.save("app/src/main/res/mipmap-xxxhdpi/ic_launcher_512.png", "PNG")
    if os.path.exists("app/src/main/res/drawable-xxxhdpi"):
        icon_512.save("app/src/main/res/drawable-xxxhdpi/ic_launcher_512.png", "PNG")

    # Generate Mipmap Icons
    mipmap_sizes = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192
    }
    for density, sz in mipmap_sizes.items():
        res_dir = f"app/src/main/res/mipmap-{density}"
        os.makedirs(res_dir, exist_ok=True)
        mip = icon_512.resize((sz, sz), Image.Resampling.LANCZOS)
        mip.save(f"{res_dir}/ic_launcher.webp", "WEBP", quality=100)

        # Circular mask for round icon
        mask = Image.new("L", (sz, sz), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.ellipse([0, 0, sz, sz], fill=255)
        round_mip = Image.new("RGBA", (sz, sz), (0, 0, 0, 0))
        round_mip.paste(mip, (0, 0), mask)
        round_mip.save(f"{res_dir}/ic_launcher_round.webp", "WEBP", quality=100)
    print("Mipmap webp icons generated.")

    # 2. TV Banners
    print("Generating Android TV Banners...")
    banner_1920 = generate_tv_banner(1920, 1080)
    banner_1920.save("store_listing/tv_banner_1920x1080.png", "PNG")

    banner_1280 = banner_1920.resize((1280, 720), Image.Resampling.LANCZOS)
    banner_1280.save("store_listing/tv_banner_1280x720.png", "PNG")

    banner_640 = banner_1920.resize((640, 360), Image.Resampling.LANCZOS)
    banner_640.save("app/src/main/res/drawable-xxxhdpi/banner.png", "PNG")

    banner_480 = banner_1920.resize((480, 270), Image.Resampling.LANCZOS)
    banner_480.save("app/src/main/res/drawable-xxhdpi/banner.png", "PNG")

    banner_320 = banner_1920.resize((320, 180), Image.Resampling.LANCZOS)
    banner_320.save("store_listing/tv_banner_320x180.png", "PNG")
    banner_320.save("app/src/main/res/drawable/banner.png", "PNG")
    banner_320.save("app/src/main/res/drawable-xhdpi/banner.png", "PNG")
    banner_320.save("app/src/main/res/drawable-tvdpi/banner.png", "PNG")

    banner_240 = banner_1920.resize((240, 135), Image.Resampling.LANCZOS)
    banner_240.save("app/src/main/res/drawable-hdpi/banner.png", "PNG")
    print("Android TV banners generated.")

    # 3. Feature Graphic (1024x500)
    print("Generating Google Play Feature Graphic (1024x500)...")
    feat_1024 = generate_feature_graphic(1024, 500)
    feat_1024.save("store_listing/feature_graphic_1024x500.png", "PNG")
    print("Feature graphic generated.")

    print("\nAll assets successfully generated in store_listing/ and app/src/main/res/!")

if __name__ == "__main__":
    main()
