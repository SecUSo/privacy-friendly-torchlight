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
import android.hardware.Camera;
import android.widget.Toast;

/**
 * @author Christopher Beckmann
 * @version 20161211
 *
 */

public class CameraNormal implements ICamera {

    private Context mContext;
    private Camera mCamera;
    private Camera.Parameters mCameraParams;

    @Override
    public void init(Context context) {

        mContext = context;

        try {
            mCamera = Camera.open();
            mCameraParams = mCamera.getParameters();
            //mCameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            //mCamera.setParameters(mCameraParams);
        } catch (RuntimeException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public  boolean toggle(boolean enable) {

        if(mCamera == null || mCameraParams == null) {
            return false;
        }

        if (enable) {
            mCameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mCameraParams);
            mCamera.startPreview();
        } else {
            mCameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mCameraParams);
            mCamera.stopPreview();
        }
        return true;
    }

    @Override
    public void release() {
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCameraParams = null;
        }
    }


}
