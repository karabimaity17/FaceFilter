package com.face.filterapp;

import static android.view.View.GONE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import com.face.filterapp.camera.CameraSourcePreview;

import com.face.filterapp.camera.FaceGraphic;
import com.face.filterapp.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;


import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.face.filterapp.databinding.ActivityVideoBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private AppBarConfiguration appBarConfiguration;
    private ActivityVideoBinding binding;
    private int typeFace = 0;
    private int typeFlash = 0;
    private boolean flashmode = false;
    private Camera mCamera;
    private boolean mPreviewRunning = false;
    private boolean mCaptureFrame = false;
    private SurfaceView cameraSurfaceView = null;
    private SurfaceHolder cameraSurfaceHolder = null;
    private boolean previewing = false;
    private GraphicOverlay mGraphicOverlay;
    RelativeLayout CamView;
    Bitmap bmp1;
    private CircleView circleView;
    CardView cardView;
    private Handler handler;
    private Runnable runnable;
    boolean goneFlag = false;
    private int i = 0;
    Dialog video_dialog;
    VideoView video_play3;
    ImageView play_button, video_close;
    MediaController mediaController;
    private final ArrayList<Integer> al = new ArrayList<>();
    private final ArrayList<Integer> al2 = new ArrayList<>();

    private final int MY_PERMISSIONS_REQUEST_USE_CAMERA = 0x00AF;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private Uri fileUri; // file url to store image/video
    public static final int MEDIA_TYPE_VIDEO = 2;
    private String selectedPath = "";
    private static final String IMAGE_DIRECTORY = "/faceFilter";
    File videoFile;
    ParcelFileDescriptor fd;
    boolean isRecording = false;
    Bitmap videoBitmap = null;
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;
    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;
    private CameraSourcePreview mPreview;
    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            //createCameraSource();
            mCameraOpenCloseLock.release();
            if (null != mPreview) {
              //  configureTransform(mPreview.getWidth(), mPreview.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;

            if (null != this) {
                finish();
            }
        }

    };
    private static final int MASK[] = {
            R.id.no_filter,
            R.id.hair,
            R.id.op,
            R.id.snap,
            R.id.glasses2,
            R.id.glasses3,
            R.id.glasses4,
            R.id.glasses5,
            R.id.mask,
            R.id.mask2,
            R.id.mask3,
            R.id.dog,
            R.id.cat2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoBinding.inflate(getLayoutInflater());
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (allPermissionGranted()) {
            if (hasStoragePermission()) {
               // createCameraSource();
            }

        } else {
            requestPermission();
        }

        CamView = findViewById(R.id.rel);
        CamView.setDrawingCacheEnabled(true);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
//        cameraSurfaceView = (SurfaceView)
//                findViewById(R.id.surfaceView1);
        //  cameraSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(640, 480));
//        cameraSurfaceHolder = cameraSurfaceView.getHolder();
//        cameraSurfaceHolder.addCallback(VideoActivity.this);
//        cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        circleView = findViewById(R.id.voiceView);
        cardView = findViewById(R.id.iv_square);

//        mRecorder = new MediaRecorder();
//        try {
//            mRecorder.prepare();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        ImageButton face = (ImageButton) findViewById(R.id.face);
        face.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (findViewById(R.id.scrollView).getVisibility() == GONE) {
                    findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                    ((ImageButton) findViewById(R.id.face)).setImageResource(R.drawable.face_select);
                } else {
                    findViewById(R.id.scrollView).setVisibility(GONE);
                    ((ImageButton) findViewById(R.id.face)).setImageResource(R.drawable.face);
                }
            }
        });

        ImageButton no_filter = (ImageButton) findViewById(R.id.no_filter);
        no_filter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton hair = (ImageButton) findViewById(R.id.hair);
        hair.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 1;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton op = (ImageButton) findViewById(R.id.op);
        op.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 2;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton snap = (ImageButton) findViewById(R.id.snap);
        snap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 3;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses2 = (ImageButton) findViewById(R.id.glasses2);
        glasses2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 4;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses3 = (ImageButton) findViewById(R.id.glasses3);
        glasses3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 5;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses4 = (ImageButton) findViewById(R.id.glasses4);
        glasses4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 6;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses5 = (ImageButton) findViewById(R.id.glasses5);
        glasses5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 7;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton mask = (ImageButton) findViewById(R.id.mask);
        mask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 8;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton mask2 = (ImageButton) findViewById(R.id.mask2);
        mask2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 9;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton mask3 = (ImageButton) findViewById(R.id.mask3);
        mask3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 10;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton dog = (ImageButton) findViewById(R.id.dog);
        dog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 11;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton cat2 = (ImageButton) findViewById(R.id.cat2);
        cat2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 12;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton button = (ImageButton) findViewById(R.id.change);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        final ImageButton flash = (ImageButton) findViewById(R.id.flash);
        flash.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int blue;
                if (flashmode == false) {
                    flashmode = true;
                    blue = 0;
                } else {
                    flashmode = false;
                    blue = 255;
                }
                flash.setColorFilter(Color.argb(255, 255, 255, blue));
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsRecordingVideo) {
                   // stopRecordingVideo();
                } else {
                    //startRecordingVideo();
                }
            }
        });

       /* cardView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAnimationOfSquare();
                    circleView.animateRadius(circleView.getmMaxRadius(), circleView.getmMinStroke());
                    handler.postDelayed(runnable, 80);
                    startRecording();
                    return true;
                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_MOVE:
                    stopRecording();
                    circleView.animateRadius(circleView.getmMinRadius(), circleView.getmMinStroke());
                    stopAnimationOfSquare();
                    handler.removeCallbacks(runnable);
                    resetAnimation();
                    goneFlag = false;
                    return true;
            }
            return true;
        });*/
        resetAnimation();
        handler = new Handler();
        runnable = () -> {
            //to make smooth stroke width animation I increase and decrease value step by step
            int random;
            if (!al.isEmpty()) {
                random = al.get(i++);

                if (i >= al.size()) {
                    for (int j = al.size() - 1; j >= 0; j--) {
                        al2.add(al.get(j));
                    }
                    al.clear();
                    i = 0;
                }
            } else {
                random = al2.get(i++);

                if (i >= al2.size()) {
                    for (int j = al2.size() - 1; j >= 0; j--) {
                        al.add(al2.get(j));
                    }
                    al2.clear();
                    i = 0;
                }
            }
            goneFlag = true;
            circleView.animateRadius(circleView.getmMaxRadius(), random);

            handler.postDelayed(runnable, 130);
        };

    }

/*    private void startRecordingVideo() {
        if (null == mCameraDevice || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mPreview.getSurface();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            mButtonVideo.setText("stop");
                            mIsRecordingVideo = true;

                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    if (null != this) {
                        Toast.makeText(NewVideoActiivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            },
                    mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        mButtonVideo.setText("record");
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();


        if (null != this) {
            Toast.makeText(NewVideoActiivity.this, "Video saved: " + mNextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }
        mNextVideoAbsolutePath = null;
        startPreview();
    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new PreviewActivity.GraphicFaceTrackerFactory())
                        .build());

        //new MultiProcessor.Builder<>(new GraphicTextTrackerFactory()).build();

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }


        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1440, 440)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(10.0f)
                .build();
        //observer.start();
        *//*
        TextGraphic mTextGraphic = new TextGraphic(mGraphicOverlay);
        mGraphicOverlay.add(mTextGraphic);
        mTextGraphic.updateText(2);*//*
    }*/
























    public void startRecording() {
//        Log.e("", "Begin StartRecording");
//        mCaptureFrame = true;
//        try {
////            mRecorder.prepare();
////            mRecorder.start();
//            isRecording = true;
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public void stopRecording() {
        Log.e("", "Begin StopChange");
        if(isRecording){
           // mRecorder.stop();
        }
       // mRecorder.reset();   // You can reuse the object by going back to setAudioSource() step
      //  mRecorder.release(); // Now the object cannot be reused
        isRecording = false;
    }

    public void onResume() {
        super.onResume();
        int cameraId = -1;
        for(int i=0;i<Camera.getNumberOfCameras();i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i,info);
            if(info.facing== Camera.CameraInfo.CAMERA_FACING_FRONT){
                cameraId = i;
                break;
            }
        }
        mCamera=Camera.open(cameraId);
        mCamera.startPreview();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("", "Begin surfaceDestroy");

//        mCamera.setDisplayOrientation(90);
//        if (Camera.getNumberOfCameras() >= 2) {
//
//            //if you want to open front facing camera use this line
//            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//
//            //if you want to use the back facing camera
//           // mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//        }
//        try {
//            mCamera.setPreviewDisplay(cameraSurfaceHolder);
//            mCamera.startPreview();
//        } catch (Exception e) {
//            mCamera.release();
//        }
    }
  @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();

//        mRecorder.reset();
//        mRecorder.release();
    }

    /*
     * PreviewCallback()
     *
     * this callback captures the preview at every frame and puts it in a byte
     * buffer. we will evaluate if this is a frame that we want to process, and
     * if so, we will send it to an asynchronous thread that will process it to
     * an ARGB Bitmap and POST it to the server
     */
    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.e("", "onPreviewFrame pass");
            if (mCaptureFrame) {
                mCaptureFrame = false;
                // new FrameHandler().execute(data);
            }
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.e("", "Begin SurfaceChange");

//        mRecorder.reset();
//        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//        mRecorder.setOutputFile("/sdcard/videotest2.mp4");
//        mRecorder.setVideoFrameRate(30);
//        //mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//
//        mRecorder.setPreviewDisplay(cameraSurfaceHolder.getSurface());
//        try {
//            mRecorder.prepare();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        Camera.Parameters p = mCamera.getParameters();
         p.setPreviewSize(720, 480);
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                mCamera.setDisplayOrientation(90);

            }
        mCamera.setParameters(p);

        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.setPreviewCallback(previewCallback);

        mCamera.startPreview();
        mPreviewRunning = true;

//        if(previewing)
//        {
//            mCamera.stopPreview();
//            previewing = false;
//        }
//        try
//        {
//            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setPreviewSize(720, 480);
//            parameters.setPictureSize(640, 480);
//            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
//                mCamera.setDisplayOrientation(90);
//
//            }
//
//            // parameters.setRotation(90);
//            mCamera.setParameters(parameters);
//
//            mCamera.setPreviewDisplay(cameraSurfaceHolder);
//            mCamera.startPreview();
//            previewing = true;
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

    }

/*
    private void startrecording(){

        mCamera = Camera.open();
        recorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        recorder.setCamera(mCamera);

        // Step 2: Set sources
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        recorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        recorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();

        }
        recorder.start();
    }*/
    private void resetAnimation() {
        i = 0;
        al.clear();
        al2.clear();
        al.add(25);
        al.add(30);
        al.add(35);
        al.add(40);
        al.add(45);
//        al.add(50);
//        al.add(55);
//        al.add(60);

        circleView.endAnimation();
    }

    public int dpToPx(float valueInDp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    private AnimatorSet currentAnimator;
    private int settingPopupVisibilityDuration;

    private void startAnimationOfSquare() {
        settingPopupVisibilityDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        circleView.getGlobalVisibleRect(finalBounds, globalOffset);


        TransitionManager.beginDelayedTransition(cardView, new TransitionSet()
                .addTransition(new ChangeBounds()).setDuration(settingPopupVisibilityDuration));

        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        params.height = dpToPx(40);
        params.width = dpToPx(40);

        cardView.setLayoutParams(params);
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        layoutParams.setMargins(0, dpToPx(60), 0, 0);
        cardView.requestLayout();

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(cardView, "radius", dpToPx(8)));
        set.setDuration(settingPopupVisibilityDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finishAnimation();
            }

            private void finishAnimation() {
                currentAnimator = null;
            }

        });
        set.start();
        currentAnimator = set;
    }

    public void stopAnimationOfSquare() {

        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        TransitionManager.beginDelayedTransition(cardView, new TransitionSet()
                .addTransition(new ChangeBounds()).setDuration(settingPopupVisibilityDuration));

        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        params.width = dpToPx(80);
        params.height = dpToPx(80);
        cardView.setLayoutParams(params);
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        layoutParams.setMargins(0, dpToPx(35), 0, 0);
        cardView.requestLayout();
        AnimatorSet set1 = new AnimatorSet();
        set1.play(ObjectAnimator.ofFloat(cardView, "radius", dpToPx(40)));//radius = height/2 to make it round
        set1.setDuration(settingPopupVisibilityDuration);
        set1.setInterpolator(new DecelerateInterpolator());
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finishAnimation();
            }

            private void finishAnimation() {
                currentAnimator = null;
            }
        });
        set1.start();
        currentAnimator = set1;
    }


    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay,typeFace);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face,typeFace);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
            return;
        } else {
            if(hasStoragePermission()) {
               // createCameraSource();
            }

        }
    }

    private boolean allPermissionGranted() {

        if (ActivityCompat.checkSelfPermission(VideoActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
            return true;
        } else {
            Toast.makeText(this, "Permission has already granted", Toast.LENGTH_SHORT).show();
            return true;

        }

    }
    private boolean hasStoragePermission() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),300);
            return false;
        }
        return true;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
//                return false;
//            } else {
//                return true;
//            }
//        } else {
//            return true;
//        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_USE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (hasStoragePermission()) {
                      //  createCameraSource();
                    }
                }
            } else {
                Toast.makeText(VideoActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();

            }
        }else if (requestCode == 300) {
            Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("TAG", "sms & location services permission granted");
                        // process the normal flow

                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("TAG", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            showDialogOK("Record Audio & Write External Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    hasStoragePermission();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }

        }


    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

}