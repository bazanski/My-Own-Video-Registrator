package com.bazanski.movr;

import java.io.IOException;
import java.util.List;
 
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
 
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
 
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private List<Size> sizes;
 
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
 
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
 
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
 
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}
 
		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}
 
		// make any resize, rotate or reformatting changes here
		Camera.Parameters params = mCamera.getParameters();
		sizes = params.getSupportedPreviewSizes();
		Log.d("PREVIEW SIZES", String.valueOf(getMaxSupportedVideoSize().width)+":"+String.valueOf(getMaxSupportedVideoSize().height));
		params.setPreviewSize(getMaxSupportedVideoSize().width, getMaxSupportedVideoSize().height);
		mCamera.setParameters(params);
		// start preview with new settings
		try {
 
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
 
		} catch (Exception e) {
		}
	}
 
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
		}
	}
 
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
 
	}
 
	public Size getMaxSupportedVideoSize() {
		int maximum = sizes.get(0).width;
		int position = 0;
		for (int i = 0; i < sizes.size() - 1; i++) {
			if (sizes.get(i).width > maximum) {
				maximum = sizes.get(i).width; // new maximum
				position = i - 1;
			}
		}
		if (position == 0) {
			int secondMax = sizes.get(1).width;
			position = 1;
			for (int j = 1; j < sizes.size() - 1; j++) {
				if (sizes.get(j).width > secondMax) {
					secondMax = sizes.get(j).width; // new maximum
					position = j;
				}
 
			}
		}
 
		return sizes.get(position);
		// end method max
 
	}
 
}
