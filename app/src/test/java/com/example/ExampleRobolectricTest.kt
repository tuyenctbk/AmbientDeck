package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DefaultLocations
import com.example.data.repository.AmbientDeckRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
