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

package com.sebastienbalard.bicycle.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.sebastienbalard.bicycle.extensions.computeNorthEastBoundsCorner
import com.sebastienbalard.bicycle.extensions.computeSouthWestBoundsCorner
import com.sebastienbalard.bicycle.extensions.intersect
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class BICContractTester {

    private val albiCenter = LatLng(43.928601, 2.151699)
    private val stOrensCenter = LatLng(43.564757, 1.531124)

    lateinit var model: BICContract

    @Test
    fun testInit() {
        assertThat(model, notNullValue())
        assertThat(model.center, notNullValue())
        assertThat(model.bounds, notNullValue())
        assertThat(model.name, `is`(equalTo("Toulouse")))
    }

    @Test
    fun testIntersectWithInsideBounds() {
        val southWestCorner = model.center.computeSouthWestBoundsCorner(1000.0)
        val northEastCorner = model.center.computeNorthEastBoundsCorner(1000.0)
        val bound = LatLngBounds(southWestCorner, northEastCorner)
        assertThat(model.bounds.intersect(bound), `is`(true))
    }

    @Test
    fun testIntersectWithSameBounds() {
        assertThat(model.bounds.intersect(model.bounds), `is`(true))
    }

    @Test
    fun testIntersectWithIntersectingBounds() {
        val southWestCorner = stOrensCenter.computeSouthWestBoundsCorner(2000.0)
        val northEastCorner = stOrensCenter.computeNorthEastBoundsCorner(2000.0)
        val bound = LatLngBounds(southWestCorner, northEastCorner)
        assertThat(model.bounds.intersect(bound), `is`(true))
    }

    @Test
    fun testIntersectWithOutsideBounds() {
        val southWestCorner = albiCenter.computeSouthWestBoundsCorner(3000.0)
        val northEastCorner = albiCenter.computeNorthEastBoundsCorner(3000.0)
        val bound = LatLngBounds(southWestCorner, northEastCorner)
        assertThat(model.bounds.intersect(bound), `is`(false))
    }

    @Before
    fun setUp() {
        model = BICContract(1, "Toulouse", 43.604652, 1.444209, "FR", 278, 6000.0, "http://api.citybik.es/v2/networks/velo")
    }

    @After
    fun tearDown() {

    }
}
