package com.secuso.torchlight2.camera;

import android.content.Context;
import android.hardware.Camera;
import android.widget.Toast;

/**
 * Created by Chris on 11.12.2016.
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
