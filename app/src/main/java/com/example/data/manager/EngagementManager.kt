package com.example.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

enum class PromptType {
    UPDATE,
    RATE,
    SHARE
}

class EngagementManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("ambient_deck_engagement", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAUNCH_COUNT = "key_launch_count"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_LAST_PROMPT_TIME = "key_last_prompt_time"
        private const val KEY_RATED_OR_OPTED_OUT = "key_rated_or_opted_out"
        private const val KEY_DISMISSED_VERSION = "key_dismissed_version"
        private const val KEY_LAST_SHARE_PROMPT = "key_last_share_prompt"

        // Minimum time (in millis) between prompts: 4 days (345,600,000 ms)
        private const val MIN_PROMPT_INTERVAL_MS = 4L * 24 * 60 * 60 * 1000
        // Minimum sessions before first rating prompt: 3
        private const val MIN_SESSIONS_FOR_RATE = 3
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

    /**
     * Smart calculation of prompt eligibility.
     * Prevents nagging, respects quiet intervals, and prioritizes updates over ratings.
     */
    fun checkPendingPrompt(currentVersionCode: Int = 1): PromptType? {
        val now = System.currentTimeMillis()
        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT_TIME, 0L)
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 1)
        val ratedOrOptedOut = prefs.getBoolean(KEY_RATED_OR_OPTED_OUT, false)

        // Don't show prompts if one was shown recently
        if (lastPrompt > 0 && (now - lastPrompt) < MIN_PROMPT_INTERVAL_MS) {
            return null
        }

        // 1. Check if mock/cloud update is available (simulated v1.1.0 = code 2)
        val availableRemoteVersion = 2
        val dismissedVersion = prefs.getInt(KEY_DISMISSED_VERSION, 0)
        if (availableRemoteVersion > currentVersionCode && dismissedVersion < availableRemoteVersion && launches >= 2) {
            return PromptType.UPDATE
        }

        // 2. Check rating prompt (only after user has experienced the app across at least 3 sessions)
        if (!ratedOrOptedOut && launches >= MIN_SESSIONS_FOR_RATE) {
            return PromptType.RATE
        }

        // 3. Gentle Share suggestion (after sustained usage, e.g. launch 6+)
        val lastSharePrompt = prefs.getLong(KEY_LAST_SHARE_PROMPT, 0L)
        if (launches >= 6 && (now - lastSharePrompt) > (MIN_PROMPT_INTERVAL_MS * 2)) {
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
