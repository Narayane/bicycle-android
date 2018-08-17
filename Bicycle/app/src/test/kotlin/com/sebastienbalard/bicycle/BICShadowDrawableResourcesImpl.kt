package com.sebastienbalard.bicycle

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.TypedValue
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowResourcesImpl

@Implements(className = "android.content.res.ResourcesImpl", inheritImplementationMethods = true, isInAndroidSdk = false, minSdk = 24)
class BICShadowDrawableResourcesImpl : ShadowResourcesImpl() {

    @Implementation
    @Throws(Resources.NotFoundException::class)
    override fun loadDrawable(wrapper: Resources, value: TypedValue, id: Int, theme: Resources.Theme, useCache: Boolean): Drawable {
        return try {
            super.loadDrawable(wrapper, value, id, theme, useCache)
        } catch (e: Exception) {
            VectorDrawable()
        }

    }

    @Implementation
    override fun loadDrawable(wrapper: Resources, value: TypedValue, id: Int, density: Int, theme: Resources.Theme): Drawable {
        return try {
            super.loadDrawable(wrapper, value, id, density, theme)
        } catch (e: Exception) {
            VectorDrawable()
        }

    }
}