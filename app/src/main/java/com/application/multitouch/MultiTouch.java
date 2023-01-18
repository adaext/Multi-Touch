package com.application.multitouch;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class MultiTouch extends Activity implements OnTouchListener {
    // These matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    enum Mode {
        NONE,
        DRAG,
        ZOOM,
        ROTATE
    }

    private Mode mode = Mode.NONE;

    private PointF originMidPoint = new PointF();
    private PointF currentMidPoint = new PointF();

    private float originRotateAngle = 0f;
    private float previousRotateAngle = 0f;
    private float currentRotateAngle = 0f;

    private float originDist = 0f;
    private float currentDist = 0f;

    boolean multiTouch = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView view = (ImageView) findViewById(R.id.imageView);
        view.setOnTouchListener(this);
    }

    public boolean onTouch(View v, MotionEvent event) {
        // Handle touch events
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                multiTouch = true;
                savedMatrix.set(matrix);
                setMidPoint(originMidPoint, event);
                originDist = spacing(event);
                originRotateAngle = rotation(event);
                previousRotateAngle = originRotateAngle;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = Mode.NONE;
                multiTouch = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!multiTouch) {
                    break;
                }
                currentRotateAngle = rotation(event);
                currentDist = spacing(event);
                setMidPoint(currentMidPoint, event);

                if (mode == Mode.NONE) {
                    // Choose mode.
                    if (Math.abs(originRotateAngle - currentRotateAngle) >= 8.0) {
                        mode = Mode.ROTATE;
                    } else if (Math.abs(originRotateAngle - currentRotateAngle) <= 5.0 && Math.abs(currentDist - originDist) > 100f) {
                        mode = Mode.ZOOM;
                    } else if (Math.abs(originRotateAngle - currentRotateAngle) <= 5.0 && Math.abs(currentDist - originDist) <= 80f && calcDistance(currentMidPoint, originMidPoint) >= 50f) {
                        mode = Mode.DRAG;
                    }
                }

                if (mode == Mode.ROTATE) {
                    float deltaAngle = currentRotateAngle - previousRotateAngle;
                    previousRotateAngle = currentRotateAngle;
                    matrix.postRotate(deltaAngle, originMidPoint.x, originMidPoint.y);
                } else if (mode == Mode.ZOOM) {
                    matrix.set(savedMatrix);
                    float scale = (currentDist / originDist);
                    matrix.postScale(scale, scale, currentMidPoint.x, currentMidPoint.y);
                } else if (mode == Mode.DRAG) {
                    matrix.set(savedMatrix);
                    float dx = currentMidPoint.x - originMidPoint.x;
                    float dy = currentMidPoint.y - originMidPoint.y;
                    matrix.postTranslate(dx, dy);
                }

                break;
        }

        view.setImageMatrix(matrix);
        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float calcDistance(PointF a, PointF b) {
        float x = a.x - b.x;
        float y = a.y - b.y;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void setMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}