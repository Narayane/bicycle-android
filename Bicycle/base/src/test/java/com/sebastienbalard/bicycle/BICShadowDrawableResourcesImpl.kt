/**
 * Copyright © 2018 Bicycle (Sébastien BALARD)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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