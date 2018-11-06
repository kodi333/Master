package jetsetapp.paint;

import android.view.ScaleGestureDetector;
import android.widget.ImageView;

class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    static float mScaleFactor = 1.0f;
    private final ImageView mImageView;

    ScaleListener(ImageView mImageView) {
        this.mImageView = mImageView;
    }

//    protected void onCreate(Bundle savedInstanceState) {
//        View view =   inflater.inflate(R.layout.activity_main,);
//        btn = (Button)view.findViewById(R.id.button);
//        canvasView = findViewById(R.id.canvas);
//    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        mScaleFactor *= scaleGestureDetector.getScaleFactor();
        mScaleFactor = Math.max(1f,
                Math.min(mScaleFactor, 4f));
        if (mScaleFactor < 1.08f)
            mScaleFactor = 1f; //1.08f is ok as the Flood fill is turned on and stroking turn off on 1.05f>
//        so there is some buffer left to auto scale to 1f too avoid bugs
        mImageView.setScaleX(mScaleFactor);
        mImageView.setScaleY(mScaleFactor);

        return true;
    }
}
