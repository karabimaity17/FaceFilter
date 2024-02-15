package com.face.filterapp.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.face.filterapp.R;
import com.google.android.gms.vision.face.Face;

public class GlassesGraphic extends GraphicOverlay.Graphic {
    private static final float GLASSES_SCALE_FACTOR = 2.5f;
    private Context context;
    private volatile Face mFace;
    private Bitmap glassesBitmap;

    public GlassesGraphic(Context context, GraphicOverlay overlay) {
        super(overlay);
        this.context = context;
        glassesBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.glasses2);
        glassesBitmap = Bitmap.createScaledBitmap(glassesBitmap,
                (int) (glassesBitmap.getWidth() * GLASSES_SCALE_FACTOR),
                (int) (glassesBitmap.getHeight() * GLASSES_SCALE_FACTOR), true);
    }

    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        // Draw the glasses overlay on the face
        if (glassesBitmap != null) {
            float x = translateX(face.getPosition().x + face.getWidth() / 2) - glassesBitmap.getWidth() / 2;
            float y = translateY(face.getPosition().y + face.getHeight() / 2) - glassesBitmap.getHeight() / 2;
            canvas.drawBitmap(glassesBitmap, x, y, null);
        }
    }

    public void updateFace(Face face) {
        mFace = face;
        glassesBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.glasses2);
        glassesBitmap = Bitmap.createScaledBitmap(glassesBitmap,
                (int) (glassesBitmap.getWidth() * GLASSES_SCALE_FACTOR),
                (int) (glassesBitmap.getHeight() * GLASSES_SCALE_FACTOR), true);

        postInvalidate();
    }
}
