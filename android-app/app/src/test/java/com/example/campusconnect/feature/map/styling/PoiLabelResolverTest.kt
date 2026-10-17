package com.example.campusconnect.feature.map.styling

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoiLabelResolverTest {

    @Test
    fun testIsPoiVisibleAtZoom() {
        // Priority 4: Always visible at any zoom
        assertTrue(PoiLabelResolver.isPoiVisibleAtZoom(4, 1.0f))
        assertTrue(PoiLabelResolver.isPoiVisibleAtZoom(4, 3.0f))

        // Priority 3: Visible only at zoom >= 2.3f
        assertFalse(PoiLabelResolver.isPoiVisibleAtZoom(3, 2.0f))
        assertTrue(PoiLabelResolver.isPoiVisibleAtZoom(3, 2.3f))
        assertTrue(PoiLabelResolver.isPoiVisibleAtZoom(3, 3.0f))

        // Priority 2: Visible only at zoom >= 4.2f
        assertFalse(PoiLabelResolver.isPoiVisibleAtZoom(2, 3.0f))
        assertFalse(PoiLabelResolver.isPoiVisibleAtZoom(2, 4.0f))
        assertTrue(PoiLabelResolver.isPoiVisibleAtZoom(2, 4.2f))
        assertTrue(PoiLabelResolver.isPoiVisibleAtZoom(2, 4.5f))
    }
}
