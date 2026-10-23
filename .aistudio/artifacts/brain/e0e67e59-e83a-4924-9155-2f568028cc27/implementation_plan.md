# AmbientDeck: Smart TV Wall & Kiosk

An ultra-sleek, modular ambient dashboard and kiosk application built for Android TV, living room displays, tablets, and smart monitors. AmbientDeck turns idle screens into an informative, cinematic command center featuring rotating 4K wallpapers, real-time local weather & air quality, live overhead flight radar, streaming crypto tickers, and daily historical trivia.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Key Architecture & Input Design Decisions:**
> - **Input Model**: Dual-mode input system supporting Android TV D-Pad remote navigation (`isFocusable`, focus rings, keyboard DPAD keys) and seamless touchscreen / mouse interaction for kiosks and tablets.
> - **Default Experience**: Cinematic Split / Ambient Wall with high-resolution imagery backdrop, glassmorphic floating widgets, live digital clock, and auto-refreshing background data streams.
> - **Live Radar & Coordinates**: Pre-configured with prominent world metros (New York, London, Tokyo, Paris, San Francisco, Singapore, Sydney, Berlin) plus custom latitude/longitude entry and location auto-detect so OpenSky flight tracking and Open-Meteo weather work instantly anywhere without friction.
> - **Zero-Key Public APIs**: Uses strictly verified, free, rate-friendly endpoints (Open-Meteo Forecast & Air Quality, Binance 24h Tickers, OpenSky Network Live States, and MuffinLabs Today-In-History) with resilient offline caching and graceful fallback handling.

---

### 1. Overview & Core Concept

- **What It Does**: AmbientDeck transforms any Android TV or stationary display into a living ambient wall. It continuously renders live modular information cards over an ambient 4K art/landscape background with subtle particle/shimmer animations, time display, and burn-in prevention.
- **Target Audience & Setting**: Living rooms (Android TV / Chromecast with Google TV), kitchen counters, office reception kiosks, and tech command stations.
- **Key Modules**:
  1. **Ambient Time & Horizon**: Minimalist typography clock, date, sun cycle, and subtle ambient drift.
  2. **Open-Meteo Weather & Air Quality (AQI)**: Current temperature, conditions, apparent temp, wind speed, UV index, and European/US Air Quality Index with pollutant breakdown (PM2.5, PM10).
  3. **Live Overhead Flight Radar (OpenSky Network)**: Real-time aircraft positions, callsigns, altitudes, headings, and ground speeds in the airspace above the user's location, rendered on an ambient radar HUD.
  4. **Crypto Market Ticker (Binance / CoinGecko)**: 24h streaming prices, percentage gains/losses, sparkline-styled trends for Bitcoin (BTC), Ethereum (ETH), Solana (SOL), and top tokens.
  5. **Today in History (MuffinLabs / Wikimedia)**: Fascinating daily historical facts, milestone events, and trivia from this calendar day.
  6. **Curated 4K Wallpaper Canvas**: Atmospheric scenic, architecture, astronomy, and fine-art rotation with adjustable interval and dimming overlays.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Ambient Living Room Idle Flow**:
   - The app launches directly into the ambient dashboard in full-screen immersive mode.
   - Smooth cross-fade wallpaper transitions occur periodically (default: 60s).
   - Live widgets update silently in the background: Weather (every 15 min), Crypto (every 30s), Flights (every 20s), History (daily).
2. **Remote / Touch Navigation Flow**:
   - Pressing D-Pad on TV remote or tapping on any widget highlights it with a luminous frosted glass border and expands quick insights (e.g. flight details, 7-day forecast, hourly crypto changes).
   - Pressing `Menu` or selecting the quick floating toolbar button opens the **Deck Customizer**:
     - Toggle widget visibility on/off.
     - Switch layout presets: *Cinematic Ambient*, *Dense Kiosk Grid*, *Zen Clock Minimal*.
     - Change active location (select metro or enter coords).
     - Adjust theme backdrop style (Scenic Nature, Cyberpunk/Night, Cosmic/Space, Classic Art, or Solid Dark).
     - Toggle burn-in protection (gentle 2-pixel ambient shift every few minutes).

#### Visual Identity & Theme
- **Aesthetic Direction**: *Luxury Ambient Darkroom* & *Glassmorphic TV HUD*. Designed to be stunning from 10 feet away on a 65" TV, yet crisp up close on a 10" tablet.
- **Color Palette**:
  - Background: Deep Void (`#0B0E14`), translucent charcoal surfaces (`#161B22` with 60–80% alpha for frosted backdrop blur).
  - Accent Primary: Electric Cyan (`#00E5FF`) and Emerald Green (`#00E676`) for positive indicators and radar sweeps.
  - Accent Secondary: Sunset Amber (`#FF9100`) for historical badges and weather alerts.
  - Text: Polar White (`#F0F6FC`) with high contrast subtext (`#8B949E`).
- **Typography & Scale**:
  - Giant ambient clock using clean, high-legibility display numbers (`80sp` to `110sp`).
  - Clear label hierarchy with pill badges, micro-monospaced flight stats, and clean weather iconography.
- **TV Accessibility & Focus States**:
  - Generous focus rings (`2.5dp` neon glow) and scale enlargement (`1.03x`) on focused elements when using TV D-pad.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct Coroutines & OkHttp/Retrofit Client vs Complex Third-Party SDKs**
  - *Chosen Approach*: Lightweight Retrofit & Moshi services communicating with standard JSON endpoints (Open-Meteo, Binance, OpenSky, MuffinLabs).
  - *Why*: Reliable, zero API key hassles, fast cold start on Android TV hardware (often constrained quad-core processors).
- **Decision 2: Adaptive Dual Layout (Cinematic Wall vs Grid Kiosk)**
  - *Chosen Approach*: Support both a cinematic TV wall (hero background with floating cards) and an informative multi-pane kiosk grid through a one-click toggle.
  - *Why*: Accommodates both passive living room screen savers and active information kiosks.
- **Decision 3: Smart Geolocation & Preset Hubs**
  - *Chosen Approach*: Offer instant world metro presets alongside GPS/custom coordinates so users on Android TV (where typing is tedious with a remote) can pick their location in 2 clicks.
- **Decision 4: Coil Image Caching & High-Res Ambient Fallbacks**
  - *Chosen Approach*: Enable Coil for asynchronous image loading with curated high-resolution photography collections and built-in vector artwork fallbacks so the TV display is never blank or broken if offline.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        AmbientDeck Dashboard                           │
│                                                                        │
│  ┌───────────────────────────────┐  ┌────────────────────────────────┐ │
│  │     Hero Ambient Header       │  │       Overhead Radar HUD       │ │
│  │   • Digital Clock & Date      │  │   • OpenSky Live Flight BBox   │ │
│  │   • Open-Meteo Current & AQI  │  │   • Aircraft Altitude & Track  │ │
│  └───────────────────────────────┘  └────────────────────────────────┘ │
│                                                                        │
│  ┌───────────────────────────────┐  ┌────────────────────────────────┐ │
│  │      Crypto Ticker Deck       │  │        History Today Card      │ │
│  │   • Binance 24hr Tickers      │  │   • MuffinLabs / Wiki Events   │ │
│  │   • BTC / ETH / SOL / Top     │  │   • Year, Milestone & Trivia   │ │
│  └───────────────────────────────┘  └────────────────────────────────┘ │
│                                                                        │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │  Bottom Nav / TV Action Bar: Layout Mode | Settings | Refresh     │ │
│  └───────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
                                │
               ┌────────────────┴────────────────┐
               ▼                                 ▼
    ┌──────────────────────┐          ┌──────────────────────┐
    │ AmbientDeckViewModel │          │   DeckPreferences    │
    │  • StateFlow<DeckUI> │          │   • Widget Toggles   │
    │  • Refresh Timers    │          │   • Selected City    │
    │  • Focus / Selection │          │   • Layout Mode      │
    └──────────┬───────────┘          └──────────────────────┘
               │
    ┌──────────┴─────────────────────────────────────────┐
    │               Data Repositories Layer              │
    ├──────────────────┬─────────────────┬───────────────┤
    │ Weather & AQI    │ Crypto Service  │ Flight Radar  │ History Today │
    │ (Open-Meteo API) │ (Binance API)   │ (OpenSky API) │ (MuffinLabs)  │
    └──────────────────┴─────────────────┴───────────────┘
```

#### Key Components & Modules:
1. **`data/api/`**:
   - `WeatherApi`: Open-Meteo forecast and air quality models.
   - `CryptoApi`: Binance 24h ticker models (`symbol`, `lastPrice`, `priceChangePercent`, `highPrice`, `lowPrice`).
   - `OpenSkyApi`: Bounding box flight states (`icao24`, `callsign`, `originCountry`, `altitude`, `velocity`, `trueTrack`).
   - `HistoryApi`: MuffinLabs Today in History models (`date`, `events`, `births`).
2. **`data/repository/`**:
   - `AmbientDeckRepository`: Aggregates live feeds with error handling, retry backoff, and offline cache.
   - `LocationRepository`: Manages geographic coordinates (preset world cities + custom coords).
3. **`ui/components/`**:
   - `AmbientBackdrop`: Coil-powered cross-fading 4K wallpapers with gradient scrims.
   - `ClockWidget`: Large format ambient clock, date, and sunrise/sunset indicator.
   - `WeatherAqiWidget`: Temperature, conditions badge, humidity, wind, and color-coded AQI pill.
   - `FlightRadarWidget`: Circular radar scanner showing relative planes in flight overhead.
   - `CryptoTickerWidget`: Horizontal scrolling ticker & card list for major crypto pairs.
   - `HistoryWidget`: Compact daily trivia card with flip/cycle animation.
   - `SettingsDialog`: TV-friendly remote settings modal for layout, city, theme, and widget toggles.
4. **`MainActivity.kt`**:
   - Immersive fullscreen layout, D-pad key event handling, edge-to-edge support, and TV keep-screen-on flag.
