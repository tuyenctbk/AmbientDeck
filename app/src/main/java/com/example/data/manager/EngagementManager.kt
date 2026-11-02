package com.example.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri

enum class PromptType {
    UPDATE,
    RATE,
    SHARE
}

data class UsageStats(
    val sessions: Int,
    val activeTimeMinutes: Int,
    val interactionCount: Int
)

class EngagementManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("ambient_deck_engagement", Context.MODE_PRIVATE)

    private var sessionStartMs: Long = System.currentTimeMillis()

    companion object {
        private const val KEY_LAUNCH_COUNT = "key_launch_count"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_TOTAL_ACTIVE_TIME_MS = "key_total_active_time_ms"
        private const val KEY_INTERACTION_COUNT = "key_interaction_count"
        private const val KEY_LAST_PROMPT_TIME = "key_last_prompt_time"
        private const val KEY_RATED_OR_OPTED_OUT = "key_rated_or_opted_out"
        private const val KEY_DISMISSED_VERSION = "key_dismissed_version"
        private const val KEY_LAST_SHARE_PROMPT = "key_last_share_prompt"
        private const val KEY_RATE_PROMPT_COUNT = "key_rate_prompt_count"
        private const val KEY_SHARE_PROMPT_COUNT = "key_share_prompt_count"

        // Minimum session active time before any prompt can show: 90 seconds (never on app start)
        private const val MIN_SESSION_ACTIVE_BEFORE_PROMPT_MS = 90_000L
        // Minimum time since first install before prompts: 3 days
        private const val MIN_DAYS_SINCE_FIRST_INSTALL_MS = 3L * 24 * 60 * 60 * 1000L
        // Global quiet cooldown between ANY prompt: 7 days
        private const val MIN_GLOBAL_COOLDOWN_MS = 7L * 24 * 60 * 60 * 1000L
        // Specific cooldown after rating dismissal: 10 days
        private const val MIN_RATE_COOLDOWN_MS = 10L * 24 * 60 * 60 * 1000L
        // Specific cooldown after share dismissal: 14 days
        private const val MIN_SHARE_COOLDOWN_MS = 14L * 24 * 60 * 60 * 1000L
        // Minimum active usage before rating: 5 minutes
        private const val MIN_ACTIVE_TIME_FOR_RATE_MS = 5L * 60 * 1000L
        // Minimum active usage before sharing: 10 minutes
        private const val MIN_ACTIVE_TIME_FOR_SHARE_MS = 10L * 60 * 1000L
        // Lifetime frequency caps
        private const val MAX_RATE_PROMPT_COUNT = 2
        private const val MAX_SHARE_PROMPT_COUNT = 2
    }

    fun recordAppLaunch() {
        sessionStartMs = System.currentTimeMillis()
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)

        val editor = prefs.edit()
        editor.putInt(KEY_LAUNCH_COUNT, launches)
        if (firstLaunch == 0L) {
            editor.putLong(KEY_FIRST_LAUNCH, sessionStartMs)
        }
        editor.apply()
    }

    fun recordActiveTime(timeMs: Long) {
        val current = prefs.getLong(KEY_TOTAL_ACTIVE_TIME_MS, 0L)
        prefs.edit().putLong(KEY_TOTAL_ACTIVE_TIME_MS, current + timeMs).apply()
    }

    fun recordInteraction() {
        val current = prefs.getInt(KEY_INTERACTION_COUNT, 0)
        prefs.edit().putInt(KEY_INTERACTION_COUNT, current + 1).apply()
    }

    fun getUsageStats(): UsageStats {
        val sessions = prefs.getInt(KEY_LAUNCH_COUNT, 1)
        val activeMs = prefs.getLong(KEY_TOTAL_ACTIVE_TIME_MS, 0L)
        val interactions = prefs.getInt(KEY_INTERACTION_COUNT, 0)
        return UsageStats(
            sessions = sessions,
            activeTimeMinutes = (activeMs / 60000L).toInt(),
            interactionCount = interactions
        )
    }

    /**
     * Smart calculation of prompt eligibility.
     * Prevents nagging, respects quiet intervals, never shows on app start,
     * and prioritizes updates over ratings and sharing.
     */
    fun checkPendingPrompt(currentVersionCode: Int = 1, isUserBusy: Boolean = false): PromptType? {
        if (isUserBusy) return null

        val now = System.currentTimeMillis()

        // 1. Never show on app start / early session (must settle for at least 90s)
        if ((now - sessionStartMs) < MIN_SESSION_ACTIVE_BEFORE_PROMPT_MS) {
            return null
        }

        // 2. Must have installed the app at least 3 days ago
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, now)
        if ((now - firstLaunch) < MIN_DAYS_SINCE_FIRST_INSTALL_MS) {
            return null
        }

        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT_TIME, 0L)
        // 3. Global cooldown: at least 7 days between any popups
        if (lastPrompt > 0 && (now - lastPrompt) < MIN_GLOBAL_COOLDOWN_MS) {
            return null
        }

        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 1)
        val activeMs = prefs.getLong(KEY_TOTAL_ACTIVE_TIME_MS, 0L)
        val interactions = prefs.getInt(KEY_INTERACTION_COUNT, 0)
        val ratedOrOptedOut = prefs.getBoolean(KEY_RATED_OR_OPTED_OUT, false)
        val rateCount = prefs.getInt(KEY_RATE_PROMPT_COUNT, 0)
        val shareCount = prefs.getInt(KEY_SHARE_PROMPT_COUNT, 0)

        // 4. Remote app update check (only if non-dismissed newer version)
        val availableRemoteVersion = prefs.getInt("remote_latest_version", currentVersionCode)
        val dismissedVersion = prefs.getInt(KEY_DISMISSED_VERSION, 0)
        if (availableRemoteVersion > currentVersionCode && dismissedVersion < availableRemoteVersion && launches >= 3) {
            return PromptType.UPDATE
        }

        // 5. Rate App check (requires proven engagement: 4+ launches, 5+ min active, 6+ interactions, max 2 lifetime prompts)
        if (!ratedOrOptedOut && rateCount < MAX_RATE_PROMPT_COUNT) {
            if (launches >= 4 && activeMs >= MIN_ACTIVE_TIME_FOR_RATE_MS && interactions >= 6) {
                if (lastPrompt == 0L || (now - lastPrompt) >= MIN_RATE_COOLDOWN_MS) {
                    return PromptType.RATE
                }
            }
        }

        // 6. Gentle Share suggestion (requires sustained loyalty: 6+ launches, 10+ min active, 10+ interactions, max 2 lifetime prompts)
        val lastSharePrompt = prefs.getLong(KEY_LAST_SHARE_PROMPT, 0L)
        if (shareCount < MAX_SHARE_PROMPT_COUNT && (now - lastSharePrompt) >= MIN_SHARE_COOLDOWN_MS) {
            if (launches >= 6 && activeMs >= MIN_ACTIVE_TIME_FOR_SHARE_MS && interactions >= 10) {
                return PromptType.SHARE
            }
        }

        return null
    }

    fun onRateApp() {
        prefs.edit()
            .putBoolean(KEY_RATED_OR_OPTED_OUT, true)
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .apply()

        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun onRateLater() {
        val currentCount = prefs.getInt(KEY_RATE_PROMPT_COUNT, 0)
        prefs.edit()
            .putInt(KEY_RATE_PROMPT_COUNT, currentCount + 1)
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .apply()
    }

    fun onRateNever() {
        prefs.edit()
            .putBoolean(KEY_RATED_OR_OPTED_OUT, true)
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .apply()
    }

    fun onShareApp() {
        prefs.edit()
            .putLong(KEY_LAST_SHARE_PROMPT, System.currentTimeMillis())
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .apply()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(com.example.R.string.share_subject))
            putExtra(
                Intent.EXTRA_TEXT,
                context.getString(
                    com.example.R.string.share_body,
                    "https://play.google.com/store/apps/details?id=${context.packageName}"
                )
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, context.getString(com.example.R.string.share_dialog_title)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun onDismissShare() {
        val currentCount = prefs.getInt(KEY_SHARE_PROMPT_COUNT, 0)
        prefs.edit()
            .putInt(KEY_SHARE_PROMPT_COUNT, currentCount + 1)
            .putLong(KEY_LAST_SHARE_PROMPT, System.currentTimeMillis())
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .apply()
    }

    fun onUpdateApp() {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun onDismissUpdate(versionCode: Int) {
        prefs.edit()
            .putInt(KEY_DISMISSED_VERSION, versionCode)
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .apply()
    }
}
