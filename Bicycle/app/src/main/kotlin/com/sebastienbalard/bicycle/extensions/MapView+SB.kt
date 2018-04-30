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

package com.sebastienbalard.bicycle.extensions

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.MapView

const val ZOOM_CONTROL_ID = 0x1
const val MY_LOCATION_CONTROL_ID = 0x2
const val NAVIGATION_CONTROL_ID = 0x4

fun MapView.alignTopZoomControls(context: Context) {

    val zoomControls = this.findViewById(ZOOM_CONTROL_ID) as View
    if (zoomControls.layoutParams is RelativeLayout.LayoutParams) {
        // zoom controls are inside of relative layout
        val params= zoomControls.layoutParams as RelativeLayout.LayoutParams

        // Align it to - parent top|right
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        // Update margins, set to 10dp
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                context.resources.displayMetrics).toInt()
        val margin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f,
                context.resources.displayMetrics).toInt()
        params.setMargins(margin, margin2, margin, margin)
    }
}
