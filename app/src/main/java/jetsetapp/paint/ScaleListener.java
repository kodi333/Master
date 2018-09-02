package jetsetapp.paint;

import android.view.ScaleGestureDetector;
import android.widget.ImageView;

class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    ImageView mImageView = MainActivity.canvasView;
    private float mScaleFactor = 1.0f;

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        mScaleFactor *= scaleGestureDetector.getScaleFactor();
        mScaleFactor = Math.max(0.1f,
                Math.min(mScaleFactor, 10.0f));
        mImageView.setScaleX(mScaleFactor);
        mImageView.setScaleY(mScaleFactor);
        return true;
    }
}
