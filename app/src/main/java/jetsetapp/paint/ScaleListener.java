package jetsetapp.paint;

import android.view.ScaleGestureDetector;

class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    static float mScaleFactor = 1.0f;

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        mScaleFactor *= scaleGestureDetector.getScaleFactor();
        mScaleFactor = Math.max(1f,
                Math.min(mScaleFactor, 2.0f));
        if (mScaleFactor < 1.08f) mScaleFactor = 1f;
        MainActivity.mImageView.setScaleX(mScaleFactor);
        MainActivity.mImageView.setScaleY(mScaleFactor);

        return true;
    }
}
