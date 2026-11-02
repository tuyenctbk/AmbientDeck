package com.example.data.voice

import androidx.annotation.StringRes
import com.example.R
import com.example.data.model.LandscapeCollection
import java.util.Locale

enum class VoiceAction(
    @StringRes val feedbackRes: Int
) {
    TOGGLE_WEATHER(R.string.voice_feedback_weather),
    SHOW_WEATHER(R.string.voice_feedback_weather),
    HIDE_WEATHER(R.string.voice_feedback_weather),

    TOGGLE_CRYPTO(R.string.voice_feedback_crypto),
    SHOW_CRYPTO(R.string.voice_feedback_crypto),
    HIDE_CRYPTO(R.string.voice_feedback_crypto),

    TOGGLE_RADAR(R.string.voice_feedback_radar),
    SHOW_RADAR(R.string.voice_feedback_radar),
    HIDE_RADAR(R.string.voice_feedback_radar),

    TOGGLE_HISTORY(R.string.voice_feedback_history),
    SHOW_HISTORY(R.string.voice_feedback_history),
    HIDE_HISTORY(R.string.voice_feedback_history),

    TOGGLE_CLOCK(R.string.voice_feedback_clock),
    SHOW_CLOCK(R.string.voice_feedback_clock),
    HIDE_CLOCK(R.string.voice_feedback_clock),

    SET_COLLECTION_MOUNTAINS(R.string.voice_feedback_collection),
    SET_COLLECTION_COASTAL(R.string.voice_feedback_collection),
    SET_COLLECTION_FOREST(R.string.voice_feedback_collection),
    SET_COLLECTION_AURORA(R.string.voice_feedback_collection),
    SET_COLLECTION_DESERT(R.string.voice_feedback_collection),
    SET_COLLECTION_LAKES(R.string.voice_feedback_collection),
    SET_COLLECTION_GOLDEN_HOUR(R.string.voice_feedback_collection),
    SET_COLLECTION_AERIAL(R.string.voice_feedback_collection),

    NEXT_WALLPAPER(R.string.voice_feedback_next_photo),
    PREV_WALLPAPER(R.string.voice_feedback_prev_photo),
    CYCLE_COLLECTION(R.string.voice_feedback_cycle_collection),
    REFRESH_ART(R.string.voice_feedback_refresh_art),

    OPEN_ART_GALLERY(R.string.voice_feedback_gallery),
    CLOSE_ART_GALLERY(R.string.voice_feedback_gallery),

    OPEN_SETTINGS(R.string.voice_feedback_settings),
    CLOSE_SETTINGS(R.string.voice_feedback_settings),

    START_SCREENSAVER(R.string.voice_feedback_screensaver),
    CYCLE_LAYOUT(R.string.voice_feedback_layout),
    TOGGLE_TEMP_UNIT(R.string.voice_feedback_temp_unit),

    UNKNOWN(R.string.voice_feedback_unknown)
}

data class VoiceCommandResult(
    val action: VoiceAction,
    val rawText: String,
    val matchedKeyword: String,
    val targetCollection: LandscapeCollection? = null,
    val isSuccess: Boolean = true
)

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    data class Success(val result: VoiceCommandResult) : VoiceState()
    data class Error(@StringRes val messageRes: Int, val detail: String? = null) : VoiceState()
}

object VoiceCommandParser {

    fun parse(rawTranscript: String): VoiceCommandResult {
        val clean = rawTranscript.lowercase(Locale.ROOT).trim()

        // 1. Screensaver commands
        if (clean.contains("screensaver") || clean.contains("screen saver") || clean.contains("sleep mode") || clean.contains("dim screen")) {
            return VoiceCommandResult(VoiceAction.START_SCREENSAVER, rawTranscript, "screensaver")
        }

        // 2. Art Gallery & Collection commands
        if (clean.contains("mountain") || clean.contains("alpine peaks") || clean.contains("alps") || clean.contains("montaña") || clean.contains("montagne")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_MOUNTAINS, rawTranscript, "mountains", LandscapeCollection.MOUNTAINS)
        }
        if (clean.contains("coastal") || clean.contains("ocean") || clean.contains("beach") || clean.contains("surf") || clean.contains("sea") || clean.contains("costa") || clean.contains("plage")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_COASTAL, rawTranscript, "coastal", LandscapeCollection.COASTAL)
        }
        if (clean.contains("forest") || clean.contains("woods") || clean.contains("trees") || clean.contains("redwood") || clean.contains("bosque") || clean.contains("forêt") || clean.contains("wald")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_FOREST, rawTranscript, "forest", LandscapeCollection.FOREST)
        }
        if (clean.contains("aurora") || clean.contains("northern light") || clean.contains("nordic") || clean.contains("polar light")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_AURORA, rawTranscript, "aurora", LandscapeCollection.NORDIC_AURORA)
        }
        if (clean.contains("desert") || clean.contains("canyon") || clean.contains("dunes") || clean.contains("sahara") || clean.contains("sand")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_DESERT, rawTranscript, "desert", LandscapeCollection.DESERT_CANYONS)
        }
        if (clean.contains("lake") || clean.contains("alpine lake") || clean.contains("lac") || clean.contains("see")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_LAKES, rawTranscript, "lakes", LandscapeCollection.LAKES)
        }
        if (clean.contains("golden hour") || clean.contains("sunset") || clean.contains("sunrise") || clean.contains("twilight")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_GOLDEN_HOUR, rawTranscript, "golden hour", LandscapeCollection.GOLDEN_HOUR)
        }
        if (clean.contains("aerial") || clean.contains("drone") || clean.contains("bird eye") || clean.contains("earth view")) {
            return VoiceCommandResult(VoiceAction.SET_COLLECTION_AERIAL, rawTranscript, "aerial", LandscapeCollection.AERIAL)
        }

        // Photo / Wallpaper navigation
        if (clean.contains("next photo") || clean.contains("next wallpaper") || clean.contains("next art") || clean.contains("next picture") || clean == "next") {
            return VoiceCommandResult(VoiceAction.NEXT_WALLPAPER, rawTranscript, "next photo")
        }
        if (clean.contains("previous photo") || clean.contains("prev photo") || clean.contains("previous wallpaper") || clean.contains("back photo") || clean == "previous" || clean == "back") {
            return VoiceCommandResult(VoiceAction.PREV_WALLPAPER, rawTranscript, "previous photo")
        }
        if (clean.contains("cycle collection") || clean.contains("next collection") || clean.contains("switch collection")) {
            return VoiceCommandResult(VoiceAction.CYCLE_COLLECTION, rawTranscript, "cycle collection")
        }
        if (clean.contains("refresh art") || clean.contains("fetch fresh") || clean.contains("refresh wallpaper") || clean.contains("new photo") || clean == "refresh") {
            return VoiceCommandResult(VoiceAction.REFRESH_ART, rawTranscript, "refresh art")
        }
        if (clean.contains("open gallery") || clean.contains("show gallery") || clean.contains("art gallery") || clean == "gallery") {
            return VoiceCommandResult(VoiceAction.OPEN_ART_GALLERY, rawTranscript, "art gallery")
        }
        if (clean.contains("close gallery") || clean.contains("exit gallery")) {
            return VoiceCommandResult(VoiceAction.CLOSE_ART_GALLERY, rawTranscript, "close gallery")
        }

        // 3. Widget visibility toggles
        // Flight Radar
        if (clean.contains("radar") || clean.contains("flight") || clean.contains("plane") || clean.contains("aircraft")) {
            val action = when {
                clean.contains("show") || clean.contains("enable") || clean.contains("open") -> VoiceAction.SHOW_RADAR
                clean.contains("hide") || clean.contains("disable") || clean.contains("close") -> VoiceAction.HIDE_RADAR
                else -> VoiceAction.TOGGLE_RADAR
            }
            return VoiceCommandResult(action, rawTranscript, "radar")
        }

        // Crypto Tickers
        if (clean.contains("crypto") || clean.contains("bitcoin") || clean.contains("ticker") || clean.contains("coins") || clean.contains("market")) {
            val action = when {
                clean.contains("show") || clean.contains("enable") || clean.contains("open") -> VoiceAction.SHOW_CRYPTO
                clean.contains("hide") || clean.contains("disable") || clean.contains("close") -> VoiceAction.HIDE_CRYPTO
                else -> VoiceAction.TOGGLE_CRYPTO
            }
            return VoiceCommandResult(action, rawTranscript, "crypto")
        }

        // Weather
        if (clean.contains("weather") || clean.contains("forecast") || clean.contains("temperature") || clean.contains("clima") || clean.contains("météo") || clean.contains("wetter")) {
            val action = when {
                clean.contains("show") || clean.contains("enable") || clean.contains("open") -> VoiceAction.SHOW_WEATHER
                clean.contains("hide") || clean.contains("disable") || clean.contains("close") -> VoiceAction.HIDE_WEATHER
                else -> VoiceAction.TOGGLE_WEATHER
            }
            return VoiceCommandResult(action, rawTranscript, "weather")
        }

        // Today in History
        if (clean.contains("history") || clean.contains("today in history") || clean.contains("event") || clean.contains("histoire")) {
            val action = when {
                clean.contains("show") || clean.contains("enable") || clean.contains("open") -> VoiceAction.SHOW_HISTORY
                clean.contains("hide") || clean.contains("disable") || clean.contains("close") -> VoiceAction.HIDE_HISTORY
                else -> VoiceAction.TOGGLE_HISTORY
            }
            return VoiceCommandResult(action, rawTranscript, "history")
        }

        // Clock
        if (clean.contains("clock") || clean.contains("time") || clean.contains("horloge") || clean.contains("reloj")) {
            val action = when {
                clean.contains("show") || clean.contains("enable") || clean.contains("open") -> VoiceAction.SHOW_CLOCK
                clean.contains("hide") || clean.contains("disable") || clean.contains("close") -> VoiceAction.HIDE_CLOCK
                else -> VoiceAction.TOGGLE_CLOCK
            }
            return VoiceCommandResult(action, rawTranscript, "clock")
        }

        // Layout
        if (clean.contains("layout") || clean.contains("switch layout") || clean.contains("change layout") || clean.contains("cinematic") || clean.contains("grid") || clean.contains("zen")) {
            return VoiceCommandResult(VoiceAction.CYCLE_LAYOUT, rawTranscript, "layout")
        }

        // Units
        if (clean.contains("celsius") || clean.contains("fahrenheit") || clean.contains("unit") || clean.contains("degrees")) {
            return VoiceCommandResult(VoiceAction.TOGGLE_TEMP_UNIT, rawTranscript, "temperature unit")
        }

        // Settings
        if (clean.contains("settings") || clean.contains("configuration") || clean.contains("preference") || clean.contains("options")) {
            return if (clean.contains("close") || clean.contains("exit")) {
                VoiceCommandResult(VoiceAction.CLOSE_SETTINGS, rawTranscript, "close settings")
            } else {
                VoiceCommandResult(VoiceAction.OPEN_SETTINGS, rawTranscript, "settings")
            }
        }

        // Fallback: Unknown command
        return VoiceCommandResult(
            action = VoiceAction.UNKNOWN,
            rawText = rawTranscript,
            matchedKeyword = "",
            isSuccess = false
        )
    }
}
