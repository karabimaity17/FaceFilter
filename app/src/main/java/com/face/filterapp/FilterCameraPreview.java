package com.face.filterapp;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import com.face.filterapp.camera.Camera2Source;
import com.face.filterapp.camera.CameraSource;
import com.face.filterapp.camera.CameraSourcePreview;
import com.face.filterapp.camera.FaceGraphic;
import com.face.filterapp.camera.GlassesGraphic;
import com.face.filterapp.camera.GraphicOverlay;
import com.face.filterapp.databinding.ActivityFilterCameraPreviewBinding;
import com.face.filterapp.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FilterCameraPreview extends AppCompatActivity {
    private static final String TAG = "Camera2 Vision";
    private Context context;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;

    // CAMERA VERSION ONE DECLARATIONS
    private CameraSource mCameraSource = null;

    // CAMERA VERSION TWO DECLARATIONS
    private Camera2Source mCamera2Source = null;

    // COMMON TO BOTH CAMERAS
    private FaceDetector previewFaceDetector = null;
 //   private FaceGraphic mFaceGraphic;
    private boolean wasActivityResumed = false;
    private boolean isRecordingVideo = false;
    private boolean flashEnabled = false;

    // DEFAULT CAMERA BEING OPENED
    private boolean usingFrontCamera = true;
    GraphicOverlay mGraphicOverlay;

    // MUST BE CAREFUL USING THIS VARIABLE.
    // ANY ATTEMPT TO START CAMERA2 ON API < 21 WILL CRASH.
    private boolean useCamera2 = false;
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
    private int typeFace = 0;
    private final ArrayList<Integer> al = new ArrayList<>();
    private final ArrayList<Integer> al2 = new ArrayList<>();
    private Handler handler;
    private Runnable runnable;
    boolean goneFlag = false;
    private int i = 0;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private ActivityFilterCameraPreviewBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilterCameraPreviewBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());
        context = getApplicationContext();
        mGraphicOverlay= binding.faceOverlay;

        if(checkGooglePlayAvailability()) {
            requestPermissionThenOpenCamera();

//            binding.switchButton.setOnClickListener(v -> {
//                if(usingFrontCamera) {
//                    stopCameraSource();
//                    createCameraSourceBack();
//                    usingFrontCamera = false;
//                } else {
//                    stopCameraSource();
//                    createCameraSourceFront();
//                    usingFrontCamera = true;
//                }
//            });
//
//            binding.flashButton.setOnClickListener(v -> {
//                if(useCamera2) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        if(flashEnabled) {
//                            mCamera2Source.setFlashMode(Camera2Source.CAMERA_FLASH_OFF);
//                            flashEnabled = false;
//                            Toast.makeText(context, "FLASH OFF", Toast.LENGTH_SHORT).show();
//                        } else {
//                            mCamera2Source.setFlashMode(Camera2Source.CAMERA_FLASH_ON);
//                            flashEnabled = true;
//                            Toast.makeText(context, "FLASH ON", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                } else {
//                    if(flashEnabled) {
//                        mCameraSource.setFlashMode(CameraSource.CAMERA_FLASH_OFF);
//                        flashEnabled = false;
//                        Toast.makeText(context, "FLASH OFF", Toast.LENGTH_SHORT).show();
//                    } else {
//                        mCameraSource.setFlashMode(CameraSource.CAMERA_FLASH_ON);
//                        flashEnabled = true;
//                        Toast.makeText(context, "FLASH ON", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });


            binding.face.setOnClickListener(new View.OnClickListener() {
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
            binding.noFilter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 0;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.hair.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 1;

                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });
            binding. op.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 2;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });


            binding.snap.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 3;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.glasses2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 4;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.glasses3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 5;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.glasses4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 6;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.glasses5.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 7;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.mask.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 8;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.mask2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 9;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.mask3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 10;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.dog.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 11;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });

            binding.cat2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                    typeFace = 12;
                    findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                }
            });
            binding.cardView.setOnClickListener(v -> {

                if(isRecordingVideo) {
                    if(useCamera2) {
                        if(mCamera2Source != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mCamera2Source.stopVideo();
                        }
                    } else {
                        if(mCameraSource != null)
                            mCameraSource.stopVideo();
                        stopAnimationOfSquare();

                    }
                } else {
                    binding.cardView.setEnabled(false);
                    if(useCamera2) {
                        if(mCamera2Source != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mCamera2Source.takePicture(camera2SourceShutterCallback, camera2SourcePictureCallback);
                        }
                    } else {
                        if(mCameraSource != null)mCameraSource.takePicture(cameraSourceShutterCallback, cameraSourcePictureCallback,typeFace);
                    }
                }


            });

            binding.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                   // binding.cardView.setEnabled(false);
//                    if(isRecordingVideo) {
//                        if(useCamera2) {
//                            if(mCamera2Source != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                mCamera2Source.stopVideo();
//                            }
//                        } else {
//                            if(mCameraSource != null)
//                                mCameraSource.stopVideo();
//                            stopAnimationOfSquare();
//
//                        }
//                    } else {
                        if(useCamera2){
                            if(mCamera2Source != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mCamera2Source.recordVideo(camera2SourceVideoStartCallback, camera2SourceVideoStopCallback, camera2SourceVideoErrorCallback, formatter.format(new Date())+".mp4", true);
                            }
                        } else {
                            if(mCameraSource != null) {
                                if(mCameraSource.canRecordVideo(CamcorderProfile.QUALITY_720P)) {
                                    startAnimationOfSquare();
                                    mCameraSource.recordVideo(cameraSourceVideoStartCallback, cameraSourceVideoStopCallback, cameraSourceVideoErrorCallback, formatter.format(new Date())+".mp4", true);
                                }
                            }
                        }
                   // }
                    return false;
                }
            });


            binding.preview.setOnTouchListener(CameraPreviewTouchListener);

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
                binding.circleView.animateRadius(binding.circleView.getmMaxRadius(), random);

                handler.postDelayed(runnable, 130);
            };
        }
    }

    private void createCameraSourceFront() {
        previewFaceDetector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                //.setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();
        previewFaceDetector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(mGraphicOverlay))
                        .build());

//        if(previewFaceDetector.isOperational()) {
//            previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
//        } else {
//            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
//            binding.status.setText("face detector not available");
//        }

        if(useCamera2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCamera2Source = new Camera2Source.Builder(context, previewFaceDetector)
                        .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                        .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                        .setFacing(Camera2Source.CAMERA_FACING_FRONT)
                        .setOverlay(mGraphicOverlay)
                        .build();

                //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
                //WE WILL USE CAMERA1.
                if(mCamera2Source.isCamera2Native()) {
                    startCameraSource();
                } else {
                    useCamera2 = false;
                    if(usingFrontCamera) createCameraSourceFront(); else createCameraSourceBack();
                }
            }
        } else {
            mCameraSource = new CameraSource.Builder(context, previewFaceDetector)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setFlashMode(CameraSource.CAMERA_FLASH_AUTO)
                    .setFocusMode(CameraSource.CAMERA_FOCUS_MODE_AUTO)
                    .setRequestedFps(10.0f)
                    .build();

            startCameraSource();
        }
    }
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

        binding.circleView.endAnimation();
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

        binding.circleView.getGlobalVisibleRect(finalBounds, globalOffset);


        TransitionManager.beginDelayedTransition(binding.cardView, new TransitionSet()
                .addTransition(new ChangeBounds()).setDuration(settingPopupVisibilityDuration));

        ViewGroup.LayoutParams params = binding.cardView.getLayoutParams();
        params.height = dpToPx(40);
        params.width = dpToPx(40);

        binding.cardView.setLayoutParams(params);
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) binding.cardView.getLayoutParams();
        layoutParams.setMargins(0, dpToPx(60), 0, 0);
        binding.cardView.requestLayout();

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(binding.cardView, "radius", dpToPx(8)));
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

        TransitionManager.beginDelayedTransition(binding.cardView, new TransitionSet()
                .addTransition(new ChangeBounds()).setDuration(settingPopupVisibilityDuration));

        ViewGroup.LayoutParams params = binding.cardView.getLayoutParams();
        params.width = dpToPx(80);
        params.height = dpToPx(80);
        binding.cardView.setLayoutParams(params);
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) binding.cardView.getLayoutParams();
        layoutParams.setMargins(0, dpToPx(35), 0, 0);
        binding.cardView.requestLayout();
        AnimatorSet set1 = new AnimatorSet();
        set1.play(ObjectAnimator.ofFloat(binding.cardView, "radius", dpToPx(40)));//radius = height/2 to make it round
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
    private void createCameraSourceBack() {
        previewFaceDetector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
//                .setProminentFaceOnly(true)
//                .setTrackingEnabled(true)
                .build();
        previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(mGraphicOverlay)).build());

//        if(previewFaceDetector.isOperational()) {
//            previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
//        } else {
//            binding.status.setText("face detector not available");
//            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
//        }

        if(useCamera2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCamera2Source = new Camera2Source.Builder(context, previewFaceDetector)
                        .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                        .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                        .setFacing(Camera2Source.CAMERA_FACING_BACK)
                        .build();

                //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
                //WE WILL USE CAMERA1.
                if(mCamera2Source.isCamera2Native()) {
                    startCameraSource();
                } else {
                    useCamera2 = false;
                    if(usingFrontCamera) createCameraSourceFront(); else createCameraSourceBack();
                }
            }
        } else {
            mCameraSource = new CameraSource.Builder(context, previewFaceDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setFocusMode(CameraSource.CAMERA_FOCUS_MODE_CONTINUOUS_PICTURE)
                    .setFlashMode(CameraSource.CAMERA_FLASH_AUTO)
                    .setRequestedFps(30.0f)
                    .build();

            startCameraSource();
        }
    }

    private void startCameraSource() {
        if(useCamera2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mCamera2Source != null) {
                    binding.cameraVersion.setText("cameraTwo");
                    binding.preview.start(mCamera2Source, mGraphicOverlay, camera2SourceErrorCallback);
                }
            }
        } else {
            if (mCameraSource != null) {
                binding.cameraVersion.setText("cameraOne");
                binding.preview.start(mCameraSource, mGraphicOverlay);
            }
        }
    }

    private void stopCameraSource() {
        binding.preview.stop();
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        private GraphicOverlay mOverlay;
        GraphicFaceTrackerFactory(GraphicOverlay overlay) {
            mOverlay = overlay;
        }
        @NonNull
        @Override
        public Tracker<Face> create(@NonNull Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private  GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private GlassesGraphic glassesGraphic;
        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
          //  mFaceGraphic = new FaceGraphic(overlay, typeFace);
            this.glassesGraphic = new GlassesGraphic(overlay.getContext(), overlay);

        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, @NonNull Face item) {
           // mFaceGraphic.setId(faceId);
            Log.d(TAG, "NEW FACE ID: "+faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(@NonNull FaceDetector.Detections<Face> detectionResults,
                             @NonNull Face face) {
//            mOverlay.add(mFaceGraphic);
//            mFaceGraphic.updateFace(face,typeFace);
//            Log.d(TAG, "NEW KNOWN FACE UPDATE: "+face.getId());

            mOverlay.add(glassesGraphic);
            glassesGraphic.updateFace(face);
            mOverlay.postInvalidate();
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(@NonNull FaceDetector.Detections<Face> detectionResults) {
        //    mFaceGraphic.goneFace();
            mOverlay.remove(glassesGraphic);
            mOverlay.postInvalidate();
            Log.d(TAG, "FACE MISSING");
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            //mFaceGraphic.goneFace();
            mOverlay.remove(glassesGraphic);
            mOverlay.postInvalidate();
            mOverlay.clear();
            Log.d(TAG, "FACE GONE");
        }
    }

    private final CameraSourcePreview.OnTouchListener CameraPreviewTouchListener =
            new CameraSourcePreview.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent pEvent) {
            v.onTouchEvent(pEvent);
            if (pEvent.getAction() == MotionEvent.ACTION_DOWN) {
                int autoFocusX = (int) (pEvent.getX() - Utils.dpToPx(60)/2);
                int autoFocusY = (int) (pEvent.getY() - Utils.dpToPx(60)/2);
                binding.ivAutoFocus.setTranslationX(autoFocusX);
                binding.ivAutoFocus.setTranslationY(autoFocusY);
                binding.ivAutoFocus.setVisibility(View.VISIBLE);
                binding.ivAutoFocus.bringToFront();
                binding.status.setText("focusing...");
                if(useCamera2) {
                    if(mCamera2Source != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            //needs to know in which zone of the screen is auto focus requested
                            // some Camera2 devices support multi-zone focusing.
                            mCamera2Source.autoFocus(success -> runOnUiThread(() -> {
                                binding.ivAutoFocus.setVisibility(View.GONE);
                                binding.status.setText("focus OK");
                            }), pEvent, v.getWidth(), v.getHeight());
                        }
                    } else {
                        binding.ivAutoFocus.setVisibility(View.GONE);
                    }
                } else {
                    if(mCameraSource != null) {
                        mCameraSource.autoFocus(success -> runOnUiThread(() -> {
                            binding.ivAutoFocus.setVisibility(View.GONE);
                            binding.status.setText("focus OK");
                        }));
                    } else {
                        binding.ivAutoFocus.setVisibility(View.GONE);
                    }
                }
            }
            if(pEvent.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                return true;
            }
            return false;
        }
    };

    final CameraSource.ShutterCallback cameraSourceShutterCallback = () -> {
        //you can implement here your own shutter triggered animation
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.status.setText("shutter event triggered");
                Log.d(TAG, "Shutter Callback!");
            }
        });
    };
    final CameraSource.PictureCallback cameraSourcePictureCallback =
            new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(Bitmap picture) {
            Log.d(TAG, "Taken picture is ready!");
            runOnUiThread(() -> {
                binding.status.setText("picture taken");
                binding.cardView.setEnabled(true);
            });
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "/camera_picture.png"));
                picture.compress(Bitmap.CompressFormat.JPEG, 95, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    final CameraSource.VideoStartCallback cameraSourceVideoStartCallback =
            new CameraSource.VideoStartCallback() {
        @Override
        public void onVideoStart() {
            isRecordingVideo = true;
            runOnUiThread(() -> {
                binding.status.setText("video recording started");

            });
            Toast.makeText(context, "Video STARTED!", Toast.LENGTH_SHORT).show();
        }
    };
    final CameraSource.VideoStopCallback cameraSourceVideoStopCallback =
            new CameraSource.VideoStopCallback() {
        @Override
        public void onVideoStop(String videoFile) {
            isRecordingVideo = false;
            runOnUiThread(() -> {
                binding.status.setText("video recording stopped");
                binding.cardView.setEnabled(true);

            });
            Toast.makeText(context, "Video STOPPED!", Toast.LENGTH_SHORT).show();
        }
    };
    final CameraSource.VideoErrorCallback cameraSourceVideoErrorCallback =
            new CameraSource.VideoErrorCallback() {
        @Override
        public void onVideoError(String error) {
            isRecordingVideo = false;
            runOnUiThread(() -> {
                binding.status.setText("video recording error");
                binding.cardView.setEnabled(true);
            });
            Toast.makeText(context, "Video Error: "+error, Toast.LENGTH_LONG).show();
        }
    };
    final Camera2Source.VideoStartCallback camera2SourceVideoStartCallback =
            new Camera2Source.VideoStartCallback() {
        @Override
        public void onVideoStart() {
            isRecordingVideo = true;
            runOnUiThread(() -> {
                binding.status.setText("video recording started");

            });
            Toast.makeText(context, "Video STARTED!", Toast.LENGTH_SHORT).show();
        }
    };
    final Camera2Source.VideoStopCallback camera2SourceVideoStopCallback =
            new Camera2Source.VideoStopCallback() {
        @Override
        public void onVideoStop(String videoFile) {
            isRecordingVideo = false;
            runOnUiThread(() -> {
                binding.status.setText("video recording stopped");

                binding.cardView.setEnabled(true);
            });
            Toast.makeText(context, "Video STOPPED!", Toast.LENGTH_SHORT).show();
        }
    };
    final Camera2Source.VideoErrorCallback camera2SourceVideoErrorCallback =
            new Camera2Source.VideoErrorCallback() {
        @Override
        public void onVideoError(String error) {
            isRecordingVideo = false;
            runOnUiThread(() -> {
                binding.status.setText("video recording error");

                binding.cardView.setEnabled(true);

            });
            Toast.makeText(context, "Video Error: "+error, Toast.LENGTH_LONG).show();
        }
    };
    final Camera2Source.ShutterCallback camera2SourceShutterCallback = () -> {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.status.setText("shutter event triggered");
                Log.d(TAG, "Shutter Callback for CAMERA2");
            }
        });
    };
    final Camera2Source.PictureCallback camera2SourcePictureCallback =
            new Camera2Source.PictureCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPictureTaken(Bitmap image) {
            Log.d(TAG, "Taken picture is ready!");
            runOnUiThread(() -> {
                binding.status.setText("picture taken");

                binding.cardView.setEnabled(true);
            });
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "/camera2_picture.png"));
                image.compress(Bitmap.CompressFormat.JPEG, 95, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    final Camera2Source.CameraError camera2SourceErrorCallback = new Camera2Source.CameraError() {
        @Override
        public void onCameraOpened() {
            runOnUiThread(() -> binding.status.setText("camera2 open success"));
        }
        @Override
        public void onCameraDisconnected() {}
        @Override
        public void onCameraError(int errorCode) {
            runOnUiThread(() -> {
                binding.status.setText("Error "+ errorCode);
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterCameraPreview.this);
                builder.setCancelable(false);
                builder.setTitle("cameraError");
                builder.setMessage("Error "+errorCode);
                builder.setPositiveButton("ok", (dialog, which) -> {
                    binding.cardView.setEnabled(false);
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            });
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(FilterCameraPreview.this, "CAMERA PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(FilterCameraPreview.this, "STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
            }
        }
    }


    private boolean checkGooglePlayAvailability() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if(resultCode == ConnectionResult.SUCCESS) {
            binding.status.setText("google play is available");
            return true;
        } else {
            if(googleApiAvailability.isUserResolvableError(resultCode)) {
                Objects.requireNonNull(googleApiAvailability.getErrorDialog(FilterCameraPreview.this, resultCode, 2404)).show();
            }
        }
        return false;
    }

    private void requestPermissionThenOpenCamera() {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                useCamera2 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
                createCameraSourceFront();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        } else {
            binding.status.setText("requesting camera permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(wasActivityResumed)
            //If the CAMERA2 is paused then resumed, it won't start again unless creating the whole camera again.
            if(useCamera2) {
                if(usingFrontCamera) {
                    createCameraSourceFront();
                } else {
                    createCameraSourceBack();
                }
            } else {
                startCameraSource();
            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasActivityResumed = true;
        stopCameraSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraSource();
//        if(previewFaceDetector != null) {
//            previewFaceDetector.release();
//        }
    }
}