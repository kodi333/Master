package jetsetapp.paint;

import android.view.ScaleGestureDetector;

class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private float mScaleFactor = 1.0f;

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        mScaleFactor *= scaleGestureDetector.getScaleFactor();
        mScaleFactor = Math.max(1f,
                Math.min(mScaleFactor, 2.0f));
        MainActivity.mImageView.setScaleX(mScaleFactor);
        MainActivity.mImageView.setScaleY(mScaleFactor);

        return true;
    }
}
