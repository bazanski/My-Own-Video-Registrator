package com.bazanski.movr;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
 
public class MainActivity extends FragmentActivity implements
		Camera.ErrorCallback, SurfaceHolder.Callback, OnClickListener,
		LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener{
 
	// Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    private GoogleMap map;
    
	private static final String TAG = "VideoRecording";
 
	// A request to connect to Location Services
    private LocationRequest mLocationRequest;
	
 // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
	
    Location loc;
    public Timer timerMin;
    private SubRip sr;
    public static String nameOfFile;
    String info = "";
    
	Handler handler = new Handler();
	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	private Button captureButton;
	private boolean isRecording = false;
	private List<Size> sizes;
 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// если хотим, чтобы приложение постоянно имело портретную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // если хотим, чтобы приложение было полноэкранным
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // и без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);
		// Create an instance of Camera.
		if (mCamera == null) {
			mCamera = getCameraInstance();
			// Create preview view and set it as the content of our activity.
			mPreview = new CameraPreview(this, mCamera);
		} else {
			mCamera.release();
			mCamera = getCameraInstance();
			// Create preview view and set it as the content of our activity.
			mPreview = new CameraPreview(this, mCamera);
 
		}
		int i = R.id.videoview;
		Object o = this.findViewById(i);
		FrameLayout preview = (FrameLayout) o;
		preview.addView(mPreview);
 
		Camera.Parameters params = mCamera.getParameters();
		params.set("cam_mode", 1);
		mCamera.setParameters(params);
 
		sizes = params.getSupportedPreviewSizes();
 
		// Add a listener to the Capture button
		captureButton = (Button) findViewById(R.id.mybutton);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRecording) {
					mMediaRecorder.stop();
					// TODO Auto-generated method stub
					sr.writeSubSD(nameOfFile, info);
					info = "";
					// stop recording and release camera
					// stop the recording
					releaseMediaRecorder(); // release the MediaRecorder object
					mCamera.lock(); // take camera access back from
									// MediaRecorder
					// inform the user that recording has stopped
					setCaptureButtonText("Start Recording");
					isRecording = false;
					
					timerMin.cancel();
			     	timerMin.purge();
			     	timerMin = null;
					
				} else {
 
					// initialize video camera
					if (prepareVideoRecorder()) {
						// Camera is available and unlocked, MediaRecorder is
						// prepared,
						// now you can start recording
 
						mMediaRecorder.start();
						// inform the user that recording has started
						setCaptureButtonText("Stop Recording");
 
						isRecording = true;
						
						timerMin = new Timer();
				        timerMin.schedule(new everyMin(), 60 * 1000, 60 * 1000);
				        
					} else {
						// prepare didn't work, release the camera
						releaseMediaRecorder();
 
					}
				}
			}
		});
	
		// Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
    	mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
        
        this.sr = new SubRip(this);
        
        map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        Marker hamburg = map.addMarker(new MarkerOptions()
        		.position(HAMBURG)
                .title("Hamburg"));
        
        Marker kiel = map.addMarker(new MarkerOptions()
                .position(KIEL)
                .title("Kiel")
                .snippet("Kiel is cool")
                .icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_launcher)));

            // Move the camera instantly to hamburg with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));

            // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        
	}
 
	private class everyMin extends TimerTask {
		@Override
		public void run() {
			new Thread(new Runnable() {
                public void run() {   
    					mMediaRecorder.stop();
    					sr.writeSubSD(nameOfFile, info);
    					info = "";
    					// stop recording and release camera
    					// stop the recording
    					releaseMediaRecorder(); // release the MediaRecorder object
    					mCamera.lock(); // take camera access back from
    									// MediaRecorder
    				
    					// initialize video camera
    					if (prepareVideoRecorder()) {
    						// Camera is available and unlocked, MediaRecorder is
    						// prepared,
    						// now you can start recording
    						mMediaRecorder.start();
    						
    					} else {
    						// prepare didn't work, release the camera
    						releaseMediaRecorder();
    					}
                }
              }).start();
		}
    	
    }

	public void setCaptureButtonText(String s) {
		captureButton.setText(s);
	}
 
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
 
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			Toast.makeText(this, "Device is not supported!", Toast.LENGTH_LONG)
					.show();
		}
	}
 
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}
 
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}
 
	private boolean prepareVideoRecorder() {
		// mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();
		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		// Step 2: Set sources
		// activate this for recording with sound
		// mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 
		mMediaRecorder.setVideoSize(getMaxSupportedVideoSize().width,
				getMaxSupportedVideoSize().height);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//
		// activate this for recording with sound
 
		// Step 4: Set output file
		mMediaRecorder.setOutputFile(getOutputMediaFile("movie"));
		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			Toast.makeText(this, "Device is not supported!", Toast.LENGTH_LONG)
					.show();
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			Toast.makeText(this, "Device is not supported!", Toast.LENGTH_LONG)
					.show();
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
 
	@Override
	public void onClick(View v) {
		/*
		 * Log.i("onClick", "BEGIN"); if(!recording) { recording =
		 * startRecording(); } else { stopRecording(); recording = false; }
		 * Log.i("onClick", "END");
		 */
	}
 

	@Override
	protected void onPause() {
		super.onPause();
		if (isRecording) {
			mMediaRecorder.stop();
			sr.writeSubSD(nameOfFile, info);
			info = "";
			releaseMediaRecorder();
			mCamera.lock();
			setCaptureButtonText("START RECORD");
			isRecording = false;
		} else {
			releaseMediaRecorder();
		}
		// releaseCamera();
	}
		
 
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isRecording) {
			mMediaRecorder.stop();
			releaseMediaRecorder();
			mCamera.lock();
			setCaptureButtonText("RECORD");
			isRecording = false;
		} else {
			releaseMediaRecorder();
			releaseCamera();
		}
		// releaseCamera();
 
		// If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();
	}
 
	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}
 
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}
 
	private Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
			// c = this.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}
 
	public Camera open() {
		return Camera.open();
	}
 
	@Override
	public void onError(int error, Camera camera) {
		// TODO Auto-generated method stub
		if (error == Camera.CAMERA_ERROR_SERVER_DIED
				|| error == Camera.CAMERA_ERROR_UNKNOWN) {
			releaseCamera();
			finish();
			Toast.makeText(this, "Camera has died", Toast.LENGTH_LONG).show();
		}
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
 
	private static String getOutputMediaFile(String sufix) {
 
		String mediaFile;
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "/VideoLogger");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("VideoLogger", "failed to create directory");
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date());
		if (!sufix.equals("movie")) {
			mediaFile = mediaStorageDir.getPath() + File.separator + "output_"
					+ timeStamp + "_" + sufix + ".txt";
		} else {
			mediaFile = mediaStorageDir.getPath() + File.separator// + "output_"
					+ timeStamp + ".mp4";
			nameOfFile = timeStamp;
 
		}
 
		return mediaFile;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		startPeriodicUpdates();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		if(location != null) {
			Toast.makeText(this, "координаты обновлены +\n" + LocationUtils.getLatLng(this, location) + " and acuracy is " + location.getAccuracy() + " provider = " + location.getProvider(), 1000).show();
			String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
						.format(new Date());
			info += "Time: " + timeStamp + " Position: " + location.getLatitude() + "," + location.getLongitude() + "\n";
			
			LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
			
			map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
		}
		else {
			Toast.makeText(this, "no location", Toast.LENGTH_LONG).show();
		}
		this.loc = location;
	}

//====================================
    
    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        Toast.makeText(this, "остановка получения координат", Toast.LENGTH_SHORT).show();
    }
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        Toast.makeText(this, "начало получения координат", Toast.LENGTH_SHORT).show();
    }
	
}