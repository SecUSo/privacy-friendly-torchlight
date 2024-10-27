/**
 * This file is part of Privacy Friendly Torchlight.
 * Privacy Friendly Torchlight is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Torchlight is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Torchlight. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.secuso.torchlight2.ui

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.secuso.torchlight2.R
import org.secuso.pfacore.model.DrawerMenu
import org.secuso.pfacore.ui.activities.DrawerActivity

/**
 * @author Christopher Beckmann
 * @version 20160704
 */
abstract class BaseActivity : DrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun drawer() = DrawerMenu.build {
        name = ContextCompat.getString(this@BaseActivity, R.string.app_name)
        icon = R.mipmap.ic_launcher
        section {
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.nav_main)
                icon = R.drawable.ic_menu_home
                clazz = MainActivity::class.java
            }
        }
        defaultDrawerSection(this)
    }
}
