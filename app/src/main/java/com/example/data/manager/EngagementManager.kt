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

    companion object {
        private const val KEY_LAUNCH_COUNT = "key_launch_count"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_TOTAL_ACTIVE_TIME_MS = "key_total_active_time_ms"
        private const val KEY_INTERACTION_COUNT = "key_interaction_count"
        private const val KEY_LAST_PROMPT_TIME = "key_last_prompt_time"
        private const val KEY_RATED_OR_OPTED_OUT = "key_rated_or_opted_out"
        private const val KEY_DISMISSED_VERSION = "key_dismissed_version"
        private const val KEY_LAST_SHARE_PROMPT = "key_last_share_prompt"

        // Minimum time between automated prompts: 3 days (259,200,000 ms)
        private const val MIN_PROMPT_INTERVAL_MS = 3L * 24 * 60 * 60 * 1000
        // Minimum active usage (in millis) before rating prompt: 3 minutes (180,000 ms)
        private const val MIN_ACTIVE_TIME_FOR_RATE_MS = 3L * 60 * 1000
        // Minimum active usage before share prompt: 10 minutes (600,000 ms)
        private const val MIN_ACTIVE_TIME_FOR_SHARE_MS = 10L * 60 * 1000
    }

    fun recordAppLaunch() {
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)

        val editor = prefs.edit()
        editor.putInt(KEY_LAUNCH_COUNT, launches)
        if (firstLaunch == 0L) {
            editor.putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis())
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
     * Prevents nagging, respects quiet intervals, and prioritizes updates over ratings and sharing.
     */
    fun checkPendingPrompt(currentVersionCode: Int = 1, isUserBusy: Boolean = false): PromptType? {
        if (isUserBusy) return null

        val now = System.currentTimeMillis()
        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT_TIME, 0L)
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 1)
        val activeMs = prefs.getLong(KEY_TOTAL_ACTIVE_TIME_MS, 0L)
        val interactions = prefs.getInt(KEY_INTERACTION_COUNT, 0)
        val ratedOrOptedOut = prefs.getBoolean(KEY_RATED_OR_OPTED_OUT, false)

        // Don't show prompts if one was shown within the cooldown window
        if (lastPrompt > 0 && (now - lastPrompt) < MIN_PROMPT_INTERVAL_MS) {
            return null
        }

        // 1. Check if update is available from remote configuration (no hardcoded fake version)
        val availableRemoteVersion = prefs.getInt("remote_latest_version", currentVersionCode)
        val dismissedVersion = prefs.getInt(KEY_DISMISSED_VERSION, 0)
        if (availableRemoteVersion > currentVersionCode && dismissedVersion < availableRemoteVersion && launches >= 3) {
            return PromptType.UPDATE
        }

        // 2. Check rating prompt (after user has meaningful engagement: 3+ launches OR 3+ min active OR 4+ interactions)
        if (!ratedOrOptedOut && (launches >= 3 || activeMs >= MIN_ACTIVE_TIME_FOR_RATE_MS || interactions >= 4)) {
            return PromptType.RATE
        }

        // 3. Gentle Share suggestion (after sustained usage: 6+ launches OR 10+ min active OR 10+ interactions)
        val lastSharePrompt = prefs.getLong(KEY_LAST_SHARE_PROMPT, 0L)
        if ((launches >= 6 || activeMs >= MIN_ACTIVE_TIME_FOR_SHARE_MS || interactions >= 10) &&
            (now - lastSharePrompt) > MIN_PROMPT_INTERVAL_MS) {
            return PromptType.SHARE
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
        prefs.edit()
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
        prefs.edit()
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
