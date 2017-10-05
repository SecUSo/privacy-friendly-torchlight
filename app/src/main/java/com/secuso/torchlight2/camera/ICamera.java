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
 * along with Privacy Friendly Torchlight. If not, see <http://www.gnu.org/licenses/>.
 */

package com.secuso.torchlight2.camera;

import android.content.Context;

/**
 * @author Christopher Beckmann
 * @version 20161211
 *
 */

public interface ICamera {
    void init(Context context);
    boolean toggle(boolean enable);
    void release();
}
