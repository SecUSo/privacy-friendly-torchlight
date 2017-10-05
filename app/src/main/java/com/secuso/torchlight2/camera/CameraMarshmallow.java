package com.secuso.torchlight2.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.widget.Toast;

/**
 * Created by Chris on 11.12.2016.
 */

public class CameraMarshmallow implements ICamera {

    private Context mContext;
    private CameraManager mCameraManager;
    private String mCameraID;

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void init(Context context) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        
        try {
            for(final String cameraId : mCameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_BACK){
                    mCameraID= cameraId;
                }
            }
            
            
        } catch (CameraAccessException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public boolean toggle(boolean enable) {
        try {
            if (mCameraID != null){
                mCameraManager.setTorchMode(mCameraID, enable);
                return true;
            }else{
                 return false;
            }
        } catch (CameraAccessException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void release() {
        // do nothing :)
    }
}
