package com.face.filterapp;

import static android.view.View.GONE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.face.filterapp.camera.CameraSourcePreview2;
import com.face.filterapp.camera.FaceGraphic;
import com.face.filterapp.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PreviewActivity extends AppCompatActivity {

    Dialog img_dialog;
    private static final String TAG = "FaceTracker";
    private static final int RC_HANDLE_GMS = 9001;

    private CameraSource mCameraSource = null;
    private int typeFace = 0;
    private int typeFlash = 0;
    private boolean flashmode = false;
    private Camera mCamera;
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
    private String videoFile;
    ParcelFileDescriptor fd;
    Bitmap videoBitmap = null;
    boolean recording = false;
    private MediaRecorder mediaRecorder;
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
    private CameraSourcePreview2 mPreview;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private GraphicOverlay mGraphicOverlay;
    // private ActivityCameraPreviewBinding binding;
    RelativeLayout CamView;
    boolean mute=true;
    Bitmap bmp1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (allPermissionGranted()) {
            if (hasStoragePermission()) {
                createCameraSource();
            }

        } else {
            requestPermission();
        }

        CamView = findViewById(R.id.rel);
        mPreview = (CameraSourcePreview2) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        circleView = findViewById(R.id.voiceView);
        cardView = findViewById(R.id.iv_square);
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
                if (recording){
                    stopVideo();
                    stopAnimationOfSquare();
                }else {
                    takeImage();
                }
            }
        });
        /*cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // binding.cardView.setEnabled(false);
//                if(recording) {
//                      stopVideo();
//                    stopAnimationOfSquare();
//
//                } else {

                    if(canRecordVideo(CamcorderProfile.QUALITY_720P)) {
                        startAnimationOfSquare();

                        takeVideo();
                    }
               // }

                return false;
            }
        });*/

       /*  cardView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAnimationOfSquare();
                    circleView.animateRadius(circleView.getmMaxRadius(), circleView.getmMinStroke());
                    handler.postDelayed(runnable, 80);
                    takeVideo();
                    return true;
                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_MOVE:
                    stopVideo();
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

    private void takeVideo(){
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
        mCamera=Camera.open(cameraId);
        mCamera.startPreview();
       if (mCamera != null) {

           mCameraSource.takePicture(new CameraSource.ShutterCallback() {
               @Override
               public void onShutter() {
                   recording = true;
                   //PREPARE MEDIA RECORDER
                   int cameraId = getIdForRequestedCamera(mCameraSource.getCameraFacing());
                   //Step 0. Disable Shutter Sound
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                       android.hardware.Camera.CameraInfo camInfo = new android.hardware.Camera.CameraInfo();
                       android.hardware.Camera.getCameraInfo(cameraId, camInfo);
                       if (camInfo.canDisableShutterSound) {
                           mCamera.enableShutterSound(false);
                       }
                   }
                   //Step 1. Unlock Camera
                   mCamera.unlock();
                   mediaRecorder = new MediaRecorder();
                   //Step 2. Create Camera Profile
                   CamcorderProfile profile;
                   try {
                       profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                   } catch (Exception e) {
                       //CAMERA QUALITY TOO LOW!!!!!!!
                       releaseMediaRecorder();
                       Toast.makeText(PreviewActivity.this, "Camera quality too LOW", Toast.LENGTH_SHORT).show();
                       return;
                   }

                   //Step 3. Set values in Profile except AUDIO settings
                   mediaRecorder.setCamera(mCamera);
                   mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                   mediaRecorder.setOutputFormat(profile.fileFormat);
                   mediaRecorder.setVideoEncoder(profile.videoCodec);
                   mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
                   mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
                   mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);

                   //Step 4. Set output file
                   String filenameAndExtension = formatter.format(new Date()) + ".mp4";
                   if (filenameAndExtension.length() > 0 && filenameAndExtension.endsWith(".mp4")) {
                       videoFile = Environment.getExternalStorageDirectory() + "/" + filenameAndExtension;
                   } else {
                       videoFile = Environment.getExternalStorageDirectory() + "/" + formatter.format(new Date()) + ".mp4";
                   }
                   mediaRecorder.setOutputFile(videoFile);
                   //Step 5. Set Duration
                   mediaRecorder.setMaxDuration(-1);
                   //Step 6. Audio

                   if (!mute) {
                       mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                       mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                       mediaRecorder.setAudioChannels(2);
                   }
                   try {
                       mediaRecorder.prepare();
                   } catch (IllegalStateException e) {
                       releaseMediaRecorder();
                       Toast.makeText(PreviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                       return;
                   } catch (IOException e) {
                       releaseMediaRecorder();
                       Toast.makeText(PreviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                       //  videoErrorCallback.onVideoError(e.getMessage());

                       return;
                   }
                   mediaRecorder.start();
               }
           }, null);
       }else {
           Toast.makeText(this, "No Camera", Toast.LENGTH_SHORT).show();
       }
    }

    public void stopVideo() {
        recording=false;
        releaseMediaRecorder();

    }
    public boolean canRecordVideo(int videoMode) {
        try {
            CamcorderProfile.get(getIdForRequestedCamera(mCameraSource.getCameraFacing()), videoMode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private static int getIdForRequestedCamera(int facing) {
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        for (int i = 0; i < android.hardware.Camera.getNumberOfCameras(); ++i) {
            android.hardware.Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    private void releaseMediaRecorder() {
        if(mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            mCamera.lock();
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
    private void takeImage() {
        try{
            //openCamera(CameraInfo.CAMERA_FACING_BACK);
            //releaseCameraSource();
            //releaseCamera();
            //openCamera(CameraInfo.CAMERA_FACING_BACK);
            //setUpCamera(camera);
            //Thread.sleep(1000);


            mCameraSource.takePicture(null, new CameraSource.PictureCallback() {

                private File imageFile;
                @Override
                public void onPictureTaken(byte[] bytes) {


                    try {
                        // convert byte array into bitmap
                        Bitmap loadedImage = null;
                        Bitmap rotatedBitmap = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);

//                        // rotate Image
//                        Matrix rotateMatrix = new Matrix();
//                        rotateMatrix.postRotate(getWindowManager().getDefaultDisplay().getRotation());
//                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
//                                loadedImage.getWidth(), loadedImage.getHeight(),
//                                null, true);

                        Matrix matrix = new Matrix();

                        matrix.preScale(-1.0f, 1.0f);

                        // return transformed image
                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(),
                                loadedImage.getHeight(), matrix, true);

                        String state = Environment.getExternalStorageState();
                        File folder = null;
                        if (state.contains(Environment.MEDIA_MOUNTED)) {
                            folder = new File(Environment
                                    .getExternalStorageDirectory()+IMAGE_DIRECTORY);
                        } else {
                            folder = new File(Environment
                                    .getExternalStorageDirectory() + IMAGE_DIRECTORY);
                        }

                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdirs();
                        }
                        if (success) {
                            java.util.Date date = new java.util.Date();
                            imageFile = new File(folder.getAbsolutePath()
                                    + File.separator
                                    //+ new Timestamp(date.getTime())
                                    + "image.jpg");

                            imageFile.createNewFile();
                            Toast.makeText(getBaseContext(), "Image saved",
                                    Toast.LENGTH_SHORT).show();
                            onPause();
                        } else {
                            Toast.makeText(getBaseContext(), "Image Not saved",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                        // save image into gallery
                       // rotatedBitmap = resize(rotatedBitmap, 800, 600);
                     //   rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

                        FileOutputStream fout = new FileOutputStream(imageFile);
                        fout.write(ostream.toByteArray());
                        fout.close();
                        ContentValues values = new ContentValues();

                        values.put(MediaStore.Images.Media.DATE_TAKEN,
                                System.currentTimeMillis());
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.MediaColumns.DATA,
                                imageFile.getAbsolutePath());

                        setResult(Activity.RESULT_OK); //add this

                        img_dialog = new Dialog(PreviewActivity.this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                        img_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        img_dialog.setContentView(R.layout.image_dialog);
                        Window window = img_dialog.getWindow();
                        WindowManager.LayoutParams wlp = window.getAttributes();

                        ImageView pro_image=img_dialog.findViewById(R.id.pro_img);
                        ImageView img_close=img_dialog.findViewById(R.id.close);

                        wlp.gravity = Gravity.CENTER;
                        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
                        window.setAttributes(wlp);
                        img_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        img_dialog.show();
                        Bitmap gr= getViewBitmap(mGraphicOverlay);
//                        Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                                R.drawable.snap);
                        Bitmap combined=combineImages(gr,rotatedBitmap);
                        pro_image.setImageBitmap(combined);

                        img_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                img_dialog.dismiss();
                            }
                        });
                        mPreview.start(mCameraSource, mGraphicOverlay);
                        ProgressUtils.cancelLoading();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ProgressUtils.cancelLoading();
                    }
//                    Bitmap cameraBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                    Bitmap cameraScaledBitmap = Bitmap.createScaledBitmap(cameraBitmap, 1280, 720, true);
//                    int wid = cameraScaledBitmap.getWidth();
//                    int hgt = cameraScaledBitmap.getHeight();
//                    Bitmap newImage = Bitmap.createBitmap(wid, hgt, Bitmap.Config.ARGB_8888);
//                    Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                                R.drawable.snap);
//                    Bitmap overlayScaledBitmap = Bitmap.createScaledBitmap(icon, wid, hgt, true);
//                    Canvas canvas = new Canvas(newImage);
//                    canvas.drawBitmap(cameraScaledBitmap , 0, 0, null);
//                    canvas.drawBitmap(overlayScaledBitmap , 0, 0, null);
//
//                    File storagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
//                    storagePath.mkdirs();
//                    String finalName = Long.toString(System.currentTimeMillis());
//                    File myImage = new File(storagePath, finalName + ".jpg");
//
//                    String photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/" + finalName + ".jpg";
//
//                    try {
//                        FileOutputStream fos = new FileOutputStream(myImage);
//                        newImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
//                        fos.close();
//                    } catch (IOException e) {
//                        Toast.makeText(PreviewActivity.this, "Pic not saved", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    Toast.makeText(PreviewActivity.this, "Pic saved in: " + photoPath, Toast.LENGTH_SHORT).show();
//
//                   // camera.startPreview();
//                    newImage.recycle();
//                    newImage = null;
//                    cameraBitmap.recycle();
//                    cameraBitmap = null;


                  //  TakeScreenshot();

                }


            });





        }catch (Exception ex){
            ProgressUtils.cancelLoading();
        }

    }
    Bitmap getViewBitmap(View view)
    {
        //Get the dimensions of the view so we can re-layout the view at its current size
        //and create a bitmap of the same size
        int width = view.getWidth();
        int height = view.getHeight();

        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        //Cause the view to re-layout
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create a bitmap backed Canvas to draw the view into
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        //Now that the view is laid out and we have a canvas, ask the view to draw itself into the canvas
        view.draw(c);

        return b;
    }
    public void TakeScreenshot(){    //THIS METHOD TAKES A SCREENSHOT AND SAVES IT AS .jpg
        Random num = new Random();
        int nu=num.nextInt(1000); //PRODUCING A RANDOM NUMBER FOR FILE NAME
        CamView.setDrawingCacheEnabled(true); //CamView OR THE NAME OF YOUR LAYOUR
        CamView.buildDrawingCache(true);
        Bitmap bmp = Bitmap.createBitmap(CamView.getDrawingCache());
        CamView.setDrawingCacheEnabled(false); // clear drawing cache
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

        String picId=String.valueOf(nu);
        String myfile="Ghost"+picId+".jpeg";

        File dir_image = new  File(Environment.getExternalStorageDirectory()+//<---
                File.separator+"Ultimate Entity Detector");          //<---
        dir_image.mkdirs();                                                  //<---
        //^IN THESE 3 LINES YOU SET THE FOLDER PATH/NAME . HERE I CHOOSE TO SAVE
        //THE FILE IN THE SD CARD IN THE FOLDER "Ultimate Entity Detector"

        try {
            File tmpFile = new File(dir_image,myfile);
            FileOutputStream fos = new FileOutputStream(tmpFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fis.close();
            fos.close();
            Toast.makeText(getApplicationContext(),
                    "The file is saved at :SD/Ultimate Entity Detector",Toast.LENGTH_LONG).show();
            bmp1 = null;
            //   camera_image.setImageBitmap(bmp1); //RESETING THE PREVIEW
            mPreview.start(mCameraSource, mGraphicOverlay);            //RESETING THE PREVIEW
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/faceFilter";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Bitmap combineImages(Bitmap frame, Bitmap image) {
        Bitmap cs = null;
        Bitmap rs = null;

        rs = Bitmap.createScaledBitmap(frame, image.getWidth() ,
                image.getHeight() , true);

        cs = Bitmap.createBitmap(rs.getWidth(), rs.getHeight(),
                Bitmap.Config.RGB_565);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(image, 0, 0, null);
        comboImage.drawBitmap(rs, 0, 0, null);
        if (rs != null) {
            rs.recycle();
            rs = null;
        }
        Runtime.getRuntime().gc();
        return cs;
    }
    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(PreviewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PreviewActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
            return;
        } else {
            if(hasStoragePermission()) {
                createCameraSource();
            }

        }
    }
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
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
        /*
        TextGraphic mTextGraphic = new TextGraphic(mGraphicOverlay);
        mGraphicOverlay.add(mTextGraphic);
        mTextGraphic.updateText(2);*/
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private class GraphicTextTrackerFactory implements MultiProcessor.Factory<String> {
        @Override
        public Tracker<String> create(String face) {
            return new GraphicTextTracker(mGraphicOverlay);
        }
    }

    private class GraphicTextTracker extends Tracker<String> {
        private GraphicOverlay mOverlay;
        private TextGraphic mTextGraphic ;

        GraphicTextTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mTextGraphic = new TextGraphic(overlay);
        }

        public void onUpdate() {
            mOverlay.add(mTextGraphic);
            mTextGraphic.updateText(3);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mTextGraphic);
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
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


    private void startRecord() {
//        File file_video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video_finger.mp4");
//        final int VIDEO_CAPTURE = 1;
//// StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//// StrictMode.setVmPolicy(builder.build());
//        Intent intent_record_video = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        intent_record_video.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
//        Uri fileUri = FileProvider.getUriForFile(PreviewActivity.this, "com.face.filterapp.provider", file_video);
//// List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(intent_record_video, PackageManager.MATCH_DEFAULT_ONLY);
//// for (ResolveInfo resolveInfo : resInfoList) {
////     String packageName = resolveInfo.activityInfo.packageName;
////     getApplicationContext().grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//// }
//        intent_record_video.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//        startActivityForResult(intent_record_video, VIDEO_CAPTURE);

        if (Build.VERSION.SDK_INT > 21) { //use this if Lollipop_Mr1 (API 22) or above
            Intent intent_record_video = new Intent();
            intent_record_video.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
            intent_record_video.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
            // We give some instruction to the intent to save the image
            File photoFile = null;

            try {
                // If the createImageFile will be successful, the photo file will have the address of the file
                photoFile = createImageFile();
                // Here we call the function that will try to catch the exception made by the throw function
            } catch (IOException e) {
                Logger.getAnonymousLogger().info("Exception error in generating the file");
                e.printStackTrace();
            }
            // Here we add an extra file to the intent to put the address on to. For this purpose we use the FileProvider, declared in the AndroidManifest.
//            Uri outputUri = FileProvider.getUriForFile(
//                    this,
//                    BuildConfig.APPLICATION_ID + ".provider",
//                    photoFile);
            Uri outputUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    "com.face.filterapp.provider", photoFile);
            intent_record_video.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

            // The following is a new line with a trying attempt
            intent_record_video.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.getAnonymousLogger().info("Calling the camera App by intent");

            // The following strings calls the camera app and wait for his file in return.
            startActivityForResult(intent_record_video, 1);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
            if (fileUri != null)
                selectedPath = fileUri.getPath();
            // set video quality
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
            // name

            // start the video capture Intent
            startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
        }
    }
    File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        // Here we create a "non-collision file name", alternatively said, "an unique filename" using the "timeStamp" functionality
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "VID_" + timeStamp;
        // Here we specify the environment location and the exact path where we want to save the so-created file
        //  File storageDirectory = new File(app_folder);
        File storageDirectory = new File(getFilesDir(), "Videos");
//Exception in below line
        //  Uri contentUri = FileProvider.getUriForFile(this,"com.wow.fileprovider", newFile);
        Logger.getAnonymousLogger().info("Storage directory set");



        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        // Here we create the file using a prefix, a suffix and a directory
        File image = new File(storageDirectory.getPath(),
                imageFileName + ".mp4");
        // File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        // Here the location is saved into the string selectedPath
        Logger.getAnonymousLogger().info("File name and path set");

        selectedPath = image.getAbsolutePath();
        fileUri = Uri.fromFile(image);
        // The file is returned to the previous intent across the camera application
        return image;
    }
    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }
    /**
     * returning image / video
     */
    private File getOutputMediaFile(int type) {

        // External sdcard location
        // File mediaStorageDir = new File(app_folder);
        File mediaStorageDir = new File(getFilesDir(), "Videos");
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(PreviewActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "Oops! Failed create "
//                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        /*if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else */if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath()
                    , "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;

    }
    /**
     * Receiving activity result method will be called after closing the camera
     * */
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            if (requestCode == 1) {
//                //  System.out.println("SELECT_VIDEO");
//
//
//            //    selectedPath = getFilePathFromURI(ProfileActivity.this,selectedImageUri);
//                File file = new File(selectedPath);
//                Uri selectedImageUri = Uri.fromFile(file);
//                String filename=file.getName();
//                //videoFile = getFilePathFromURI1(ProfileActivity.this,selectedImageUri);
//                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + IMAGE_DIRECTORY);
//
//                folder.mkdirs();
//                videoFile = new File(folder,filename);
//                try {
//                    videoFile.createNewFile();
//                    fd =getContentResolver().openFileDescriptor(selectedImageUri, "r");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                MediaMetadataRetriever mediaMetadataRetriever = null;
//                try {
//                    mediaMetadataRetriever = new MediaMetadataRetriever();
//                    mediaMetadataRetriever.setDataSource(PreviewActivity.this, selectedImageUri);
//                    videoBitmap = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//                    long duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//                    long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
//                    int width = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
//                    int height = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
//
//                } catch ( Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (mediaMetadataRetriever != null) {
//                        try {
//                            mediaMetadataRetriever.release();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                video_dialog = new Dialog(PreviewActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
//                video_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                video_dialog.setContentView(R.layout.dialog_full_video);
//                Window window = video_dialog.getWindow();
//                WindowManager.LayoutParams wlp = window.getAttributes();
//
//                video_play3 = video_dialog.findViewById(R.id.video_play3);
//                video_close=video_dialog.findViewById(R.id.close);
//
//                wlp.gravity = Gravity.CENTER;
//                wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
//                window.setAttributes(wlp);
//                video_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//                video_play3.setVideoPath(selectedPath);
//
//                // starts the video
//
//                video_play3.seekTo(1);
//                video_play3.pause();
//                play_button.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        mediaController = new MediaController(PreviewActivity.this);
//
//                        video_dialog.show();
//
//                        video_play3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                            @Override
//                            public void onPrepared(MediaPlayer mp) {
//
//                                // mediaController = new MediaController(PreviewActivity.this);
//                                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
//                                    @Override
//                                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//
//
//                                        FrameLayout f = (FrameLayout) mediaController.getParent();
//                                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                                        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, video_play3.getId());
//
//                                        ((LinearLayout) f.getParent()).removeView(f);
//                                        ((RelativeLayout) video_play3.getParent()).addView(f, lp);
//                                        mp.start();
//
//                                    }
//                                });
//                                // mediaController.hide();
//
//                            }
//                        });
//                        // mediaController.show(0);
//                        mediaController.show();
//                        mediaController.setAnchorView(video_play3);
//                        video_play3.setMediaController(mediaController);
//                        video_play3.start();
//
//
//
//
//
//
//                    }
//                });
//                video_close.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        video_dialog.dismiss();
//                    }
//                });
//                video_dialog.show();
//
//
//                try{
//                    video_play3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//
//                            // mediaController = new MediaController(PreviewActivity.this);
//                            mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
//                                @Override
//                                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//
//
//
//
//                                    FrameLayout f = (FrameLayout) mediaController.getParent();
//                                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, video_play3.getId());
//
//                                    ((LinearLayout) f.getParent()).removeView(f);
//                                    ((RelativeLayout) video_play3.getParent()).addView(f, lp);
//                                    mp.start();
//
//                                }
//                            });
//                            // mediaController.hide();
//
//                        }
//                    });
//                    // mediaController.show(0);
//                    mediaController.show();
//                    mediaController.setAnchorView(video_play3);
//                    video_play3.setMediaController(mediaController);
//                    video_play3.start();
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }
//
//
//    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_USE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(PreviewActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (hasStoragePermission()) {
                        createCameraSource();
                    }
                }
            } else {
                Toast.makeText(PreviewActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();

            }
        }else if (requestCode == 300) {
            if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // check whether storage permission granted or not.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do what you want;
                }
            }

        }
    }

    private boolean allPermissionGranted() {

        if (ActivityCompat.checkSelfPermission(PreviewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PreviewActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
            return true;
        } else {
            Toast.makeText(this, "Permission has already granted", Toast.LENGTH_SHORT).show();
            return true;

        }

    }

}



