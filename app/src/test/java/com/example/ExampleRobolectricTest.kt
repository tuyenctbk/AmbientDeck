package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.manager.EngagementManager
import com.example.data.model.DefaultLocations
import com.example.data.repository.AmbientDeckRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AmbientDeck", appName)

        val cryptoTitle = context.getString(R.string.crypto_title)
        assertTrue(cryptoTitle.contains("CoinGecko"))

        val forecastTitle = context.getString(R.string.five_day_forecast)
        assertEquals("5-Day Forecast", forecastTitle)
    }

    @Test
    fun `verify default locations presets`() {
        assertTrue(DefaultLocations.ALL.isNotEmpty())
        val nyc = DefaultLocations.NEW_YORK
        assertEquals("New York", nyc.name)
        assertTrue(nyc.latitude > 40.0)
    }

    @Test
    fun `verify wallpaper curation catalogs`() {
        val repo = AmbientDeckRepository()
        val scenic = repo.getWallpapersForCategory(com.example.data.model.ArtCategory.SCENIC)
        assertTrue(scenic.isNotEmpty())
        assertNotNull(scenic.first().imageUrl)
    }

    @Test
    fun `verify engagement manager suppresses prompts on launch and early session`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = EngagementManager(context)
        manager.recordAppLaunch()

        // Right after launch, checkPendingPrompt must ALWAYS return null
        val promptOnStart = manager.checkPendingPrompt(currentVersionCode = 1, isUserBusy = false)
        assertNull("Prompts must never show on app start or early session", promptOnStart)
    }

    @Test
    fun `verify all landscape collections have high resolution unsplash curation`() {
        val repo = AmbientDeckRepository()
        com.example.data.model.LandscapeCollection.values().forEach { col ->
            val wallpapers = repo.getCuratedCollection(col)
            assertTrue("Collection $col must have curated wallpapers", wallpapers.isNotEmpty())
            wallpapers.forEach { wp ->
                assertTrue(wp.imageUrl.startsWith("https://images.unsplash.com"))
                assertTrue(wp.title.isNotBlank())
                assertTrue(wp.author.isNotBlank())
            }
        }
    }

    @Test
    fun `verify fetchLandscapeCollection falls back gracefully without unsplash key`() = kotlinx.coroutines.runBlocking {
        val repo = AmbientDeckRepository()
        val photos = repo.fetchLandscapeCollection(com.example.data.model.LandscapeCollection.NORDIC_AURORA)
        assertTrue(photos.isNotEmpty())
        assertEquals(com.example.data.model.LandscapeCollection.NORDIC_AURORA, photos.first().collection)
    }

    @Test
    fun `verify screensaver configuration defaults and string resources`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val title = context.getString(R.string.sec_screensaver_title)
        assertTrue(title.contains("Screensaver") || title.contains("شاشة") || title.contains("屏保") || title.contains("Wygaszacz"))

        val defaultSettings = com.example.data.model.DeckSettings()
        assertEquals(120, defaultSettings.screensaverTimeoutSec)
        assertEquals(com.example.data.model.ScreensaverStyle.SLIDESHOW, defaultSettings.screensaverStyle)
    }
}
