package com.dsvoronin.grindfm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;
import com.dsvoronin.R;

public class GesturableViewFlipper extends ViewFlipper implements GestureDetector.OnGestureListener {

    private static final String TAG = GesturableViewFlipper.class.getSimpleName();

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private GestureDetector gestureScanner;

    private OnSwitchListener onSwitchListener;

    public void setOnSwitchListener(OnSwitchListener onSwitchListener) {
        this.onSwitchListener = onSwitchListener;
    }

    public GesturableViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureScanner = new GestureDetector(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return gestureScanner.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                this.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.out_to_left));
                this.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.in_from_left));
                this.showNext();
                onSwitchListener.onSwitch(getDisplayedChild());
                return true;

            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                this.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.out_to_right));
                this.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.in_from_right));
                this.showPrevious();
                onSwitchListener.onSwitch(getDisplayedChild());
                return true;

            } else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                return true;

            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                return true;

            }

        } catch (Exception e) {
            Log.e(TAG, "onFling error", e);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }


    public interface OnSwitchListener {
        void onSwitch(int childId);
    }
}
