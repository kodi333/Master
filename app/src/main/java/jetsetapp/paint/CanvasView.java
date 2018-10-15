package jetsetapp.paint;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.measurement.zzwu.init;


public class CanvasView extends android.support.v7.widget.AppCompatImageView {

    private static final float TOLERANCE = 5;
    final Point p1 = new Point();
    protected Paint paint = new Paint();
    protected Paint _paintBlur = new Paint();
    Context context;
    FileOutputStream fos = null;
    View undoButton;
    ProgressDialog pd;
    private List<Path> paths = new ArrayList<Path>();
    private List<Integer> colors = new ArrayList<Integer>();
    private List<Float> strokes = new ArrayList<Float>();
    private List<Point> points = new ArrayList<Point>();
    private List<Point> undonePoints = new ArrayList<Point>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Integer> undoneColors = new ArrayList<Integer>();
    private ArrayList<Float> undoneStrokes = new ArrayList<Float>();
    private int currentColor = Color.parseColor("#E6B0AA"); // lime
    private float currentStroke = 10F;
    private Bitmap mBitmap;

    private Bitmap newBitmap;
    private Canvas canvas;
    private Path path = new Path();
    private float mX;
    private float mY;
    private ViewStub view;
    private int cx;
    private int cy;
    private final static int NONE = 0;
    private Integer temporaryColor;
    private List<Integer> sourceFillColors = new ArrayList<Integer>();
    private List<Integer> undoneFillColors = new ArrayList<Integer>();
    private List<Integer> targetFillColors = new ArrayList<Integer>();
    private int fillSourceColor;
    private Bitmap fillBitmap;
    private List<Integer> undoneTargetFillColors = new ArrayList<Integer>();
    private final static int PAN = 1;
    private final static int ZOOM = 2;
    public Rect imageRect;
    boolean onePointer = true;

    public CanvasView(Context context) {
        super(context);
        init(context);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        setFocusable(true);
        setFocusableInTouchMode(true);

        paint.setDither(true);
        paint.setColor(Color.parseColor("#E6B0AA")); // Lime
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(currentStroke);


    }

    private float mStartScrollX;
    private float mStartScrollY;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mBitmap);
        imageRect = new Rect(0, 0, w, h);


    }

    public void changeColor(int color) {
        currentColor = color;
        path = new Path();
    }

    public int getColor() {
        int color = currentColor;
       return color;
    }

    public void changeStroke(float size) {
        currentStroke = size;
        path = new Path();
    }

    private float mTranslateScrollX;

    private void startTouch(float x, float y) {
        path.moveTo(x, y);
        mX = x;
        mY = y;

    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void upTouch() {
        path.lineTo(mX, mY);
        canvas = new Canvas();
        path = new Path();


    }

    public void undoLastDraw(){
        if (points.size() > 0) {
            if (points.get(points.size() - 1).x > 0 && points.get(points.size() - 1).y > 0) {
                int targetColor = sourceFillColors.size() - 1 < 0 ? Color.WHITE : sourceFillColors.get(sourceFillColors.size() - 1);
                FloodFill fill = new FloodFill(newBitmap, newBitmap.getPixel(points.get(points.size() - 1).x, points.get(points.size() - 1).y), targetColor);
                // Above I NEED TO PUT TARGET COLOR FROM Target Color Array
                fill.floodFill(points.get(points.size() - 1).x, points.get(points.size() - 1).y);
                undonePoints.add(points.remove(points.size() - 1));
                undoneFillColors.add(sourceFillColors.remove(sourceFillColors.size() - 1));
                undoneTargetFillColors.add(targetFillColors.remove(targetFillColors.size() - 1));
            } else {
                undonePaths.add(paths.remove(paths.size() - 1));
                undoneColors.add(colors.remove(colors.size() - 1));
                undoneStrokes.add(strokes.remove(strokes.size() - 1));
                undonePoints.add(points.remove(points.size() - 1));
            }

            if (points.size() <= 0) {
                MainActivity.undoButton.setVisibility(View.INVISIBLE);
            }

            if (undonePoints.size() > 0) {
                MainActivity.redoButton.setVisibility(View.VISIBLE);
            }
            invalidate();

        }
    }

    public void redoLastDraw(){
        if (undonePoints.size() > 0) {
            Log.v("TAG", "undonePaths.size>0");
            if (undonePoints.get(undonePoints.size() - 1).x > 0 && undonePoints.get(undonePoints.size() - 1).y > 0) {
                Log.v("TAG", "undonePaths.size.x>0");
                int targetColor = undoneTargetFillColors.get(undoneTargetFillColors.size() - 1);// < 0 ? Color.WHITE : undoneFillColors.get(undoneFillColors.size() - 1);
//                FloodFill fill = new FloodFill(newBitmap, newBitmap.getPixel(undonePoints.get(undonePoints.size() - 1).x, undonePoints.get(undonePoints.size() - 1).y), targetColor);
                FloodFill fill = new FloodFill(newBitmap, newBitmap.getPixel(undonePoints.get(undonePoints.size() - 1).x, undonePoints.get(undonePoints.size() - 1).y), targetColor);
                fill.floodFill(undonePoints.get(undonePoints.size() - 1).x, undonePoints.get(undonePoints.size() - 1).y);
                points.add(undonePoints.remove(undonePoints.size() - 1));
                sourceFillColors.add(undoneFillColors.remove(undoneFillColors.size() - 1));
                targetFillColors.add(undoneTargetFillColors.remove(undoneTargetFillColors.size() - 1));
            } else {
                paths.add(undonePaths.remove(undonePaths.size() - 1));
                colors.add(undoneColors.remove(undoneColors.size() - 1));
                strokes.add(undoneStrokes.remove(undoneStrokes.size() - 1));
                points.add(undonePoints.remove(undonePoints.size() - 1));
            }
            if (undonePoints.size() <= 0) {
                MainActivity.redoButton.setVisibility(View.INVISIBLE);
            }

            if (undonePoints.size() > 0) {
                MainActivity.redoButton.setVisibility(View.VISIBLE);
            }

            if (points.size() > 0) {
                MainActivity.undoButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private float mTranslateScrollY;

    public void clearCanvas() {

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptView = layoutInflater.inflate(R.layout.about, null);
        final AlertDialog.Builder alertD = new AlertDialog.Builder(context);
        alertD.setView(promptView);
        final AlertDialog ad = alertD.show();
        ImageButton ok_button = promptView.findViewById(R.id.ok_button);
        ImageButton no_buton = promptView.findViewById(R.id.no_button);


        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path.reset();
                        paths.clear();
                        undonePaths.clear();
                        sourceFillColors.clear();
                        undoneFillColors.clear();
                        points.clear();
                        undonePoints.clear();
                        colors.clear();
                        undoneColors.clear();
                        strokes.clear();
                        undoneStrokes.clear();
                        targetFillColors.clear();
                        undoneTargetFillColors.clear();

                if (imageRect == null) {
                    imageRect = new Rect(0, 0, getWidth(), getHeight());
                }

                newBitmap = MainActivity.getNewBitmap();
                if (newBitmap != null) canvas.drawBitmap(newBitmap, null, imageRect, paint);
                path = new Path();


                if (points.size() <= 0) {
                    MainActivity.undoButton.setVisibility(View.INVISIBLE);
                    MainActivity.clearButton.setVisibility(View.INVISIBLE);
                }
//
                if (undonePoints.size() <= 0) {
                    MainActivity.redoButton.setVisibility(View.INVISIBLE);
                }
                invalidate();
                ad.dismiss();

            }
        });
        no_buton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();

            }
        });


    }

    public static final String TAG = CanvasView.class.getName();
    private int mEventState;
    int counter = 1;
    private float mStartX = 0f;
    private float mStartY = 0f;
    private float mTranslateX = 0f;
    private float mTranslateY = 0f;
    private float mPreviousTranslateX = 0f;
    private float mPreviousTranslateY = 0f;

    public static boolean isZoomed() {
        return ScaleListener.mScaleFactor > 1.05f;
    }

    public void setNewBitmap(Bitmap newBitmap) {
        this.newBitmap = newBitmap;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (imageRect == null) { // I think it is always false as we are initializing it onSizeChanged
            imageRect = new Rect(0, 0, getWidth(), getHeight());
        }

//        int dragLimit = (int) (imageRect.height()*0.5);

        if (newBitmap != null) {
            newBitmap = Bitmap.createScaledBitmap(newBitmap, getWidth(), getHeight(), false);
            if (MainActivity.mImageView.getScaleX() > 1 || MainActivity.mImageView.getScaleY() > 1) {
//                canvas.translate(-mTranslateX, -mTranslateY);
//                mTranslateX = mTranslateX >150 ? 0: mTranslateX;
//                mTranslateY = mTranslateY >150 ? 0: mTranslateY;
//
//                mTranslateX = Math.min(Math.max(-500,mTranslateX),500);
//                mTranslateY = Math.min(Math.max(-500,mTranslateY),500);

                imageRect = new Rect((int) ((mTranslateX / ScaleListener.mScaleFactor)), (int) ((mTranslateY / ScaleListener.mScaleFactor)),
                        getWidth() + (int) ((mTranslateX / ScaleListener.mScaleFactor)), getHeight() + (int) (mTranslateY / ScaleListener.mScaleFactor));
            }
            canvas.drawBitmap(newBitmap, null, imageRect, paint);
//            newBitmap = Bitmap.createScaledBitmap(newBitmap, getWidth(), getHeight(), false);
//            canvas.translate(mTranslateX,mTranslateY);
        }

        for (int x = 0; x < paths.size(); x++) {
            paint.setColor(colors.get(x));
            paint.setStrokeWidth(strokes.get(x));
            canvas.drawPath(paths.get(x), paint);
        }
        paint.setColor(currentColor);
        paint.setStrokeWidth(currentStroke);
        canvas.drawPath(path, paint);


    }

    public void zoomOut() {
        MainActivity.mImageView.setScaleX(1);
        MainActivity.mImageView.setScaleY(1);
        imageRect.set(0, 0, newBitmap.getWidth(), newBitmap.getHeight());
        MainActivity.zoomOutButton.setVisibility(View.INVISIBLE);
        Log.i("zoomout", "zoomout");
    }
//    private float dragStartX = 0f;
//    private float dragStartY = 0f;
//    private float dragTranslateX = 0f;
//    private float dragTranslateY = 0f;
//    private float dragPreviousTranslateX = 0f;
//    private float dragPreviousTranslateY = 0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        //to avoid nulpPointer
        float x = 0f;
        float y = 0f;
        x = event.getX();
        y = event.getY();
        Log.i(TAG, String.valueOf(event.getPointerCount()));
//        Log.i(TAG, String.valueOf(MainActivity.mScaleGestureDetector.isInProgress()));
//        Log.i(TAG, String.valueOf(MainActivity.mScaleGestureDetector.getTimeDelta()));
//        Log.i(TAG, String.valueOf(MainActivity.mScaleGestureDetector.getScaleFactor()));
//        Log.i(TAG, String.valueOf(MainActivity.mScaleGestureDetector.getScaleFactor()));
//        Log.i(TAG, String.valueOf("pinch time: " + MainActivity.mScaleGestureDetector.getEventTime()));
//        Log.i(TAG, String.valueOf("event time: " + event.getEventTime()));


//        onePointer = event.getPointerCount()>=2?true:onePointer;
        Log.i(TAG, String.valueOf("twoPointerBefore" + onePointer));
        if (event.getPointerCount() >= 2) {
            onePointer = false;
        }
        Log.i(TAG, String.valueOf("onePointer" + onePointer));
        //if not scaling in progress
//        && !MainActivity.mScaleGestureDetector.isInProgress() && event.getPointerCount() < 2 && MainActivity.mScaleGestureDetector.getTimeDelta()!=0
//        if ((event.getEventTime() - MainActivity.mScaleGestureDetector.getEventTime())>=10 ) {
        if (onePointer) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (!MainActivity.isFillFloodSelected() && !isZoomed()) {
                        p1.x = 0;
                        p1.y = 0;
                        startTouch(x, y);
                    }


                    if (!isZoomed()) {
                        mTranslateX = 0;
                        mTranslateY = 0;
                        mPreviousTranslateX = 0;
                        mPreviousTranslateY = 0;
                        mStartX = event.getX();
                        mStartY = event.getY();
                        mTranslateScrollX = 0;
                        mTranslateScrollY = 0;

                        //center image
//                        imageRect.set(0,0,newBitmap.getWidth(),newBitmap.getHeight());
                        zoomOut();

                    } else {

                        mEventState = PAN;
                        mStartX = event.getX() - mPreviousTranslateX;
                        mStartY = event.getY() - mPreviousTranslateY;

                        mStartScrollX = event.getX();
                        mStartScrollY = event.getY();

//                        }
                    }

                    invalidate();
                    break;


                case MotionEvent.ACTION_MOVE:
                    if (!MainActivity.isFillFloodSelected()) {
                        moveTouch(x, y);
                    }


                    if (event.getPointerCount() == 1 && isZoomed()) {
                        mEventState = NONE;

                        mTranslateX = event.getX() - mStartX;
                        mTranslateY = event.getY() - mStartY;

                        mTranslateScrollX = event.getX() - mStartScrollX;
                        mTranslateScrollY = event.getY() - mStartScrollY;

                        MainActivity.fillFloodSelected = true;
                        MainActivity.canvasView.changeStroke(0);

                        Log.i("transX", String.valueOf(mTranslateX));
                        Log.i("transY", String.valueOf(mTranslateY));
                        Log.i("mStartX", String.valueOf(mStartX));
                        Log.i("mStartY", String.valueOf(mStartY));
                        Log.i("eventX", String.valueOf(event.getX()));
                        Log.i("eventY", String.valueOf(event.getY()));
                        Log.i("previousX", String.valueOf(mPreviousTranslateX));
                        Log.i("previousY", String.valueOf(mPreviousTranslateY));
                        Log.i("mTranslateScrollX", String.valueOf(mTranslateScrollX));
                        Log.i("mTranslateScrollY", String.valueOf(mTranslateScrollY));


                    }

                    invalidate();
                    break;


                case MotionEvent.ACTION_UP:

                    mStartScrollX = 0;
                    mStartScrollY = 0;

                    if (!MainActivity.isFillFloodSelected()) {
                        points.add(new Point(p1.x, p1.y));
                        strokes.add(currentStroke);
                        paths.add(path);
                        colors.add(currentColor);
                        upTouch();
                    } else {

                        // below is used to fill the color coords calculation, dont change it
                        if (ScaleListener.mScaleFactor > 1.00f) {

                            x = Math.max(0, Math.min((x - mTranslateX / ScaleListener.mScaleFactor), (newBitmap.getWidth() - 5)));
                            y = Math.max(0, Math.min((y - mTranslateY / ScaleListener.mScaleFactor), (newBitmap.getHeight() - 5)));
                        }

                        int moveTolerance = 10;

                        //fillFlood only when there is no scroll movement nor there is zoom
                        if (Math.abs(mTranslateScrollX) < moveTolerance
                                && Math.abs(mTranslateScrollY) < moveTolerance) {
                            p1.x = (int) x;
                            p1.y = (int) y;
                            //                        newBitmap = Bitmap.createScaledBitmap(newBitmap, getWidth(), getHeight(), false);
                            fillSourceColor = newBitmap.getPixel((int) x, (int) y);

                            final int targetColor = currentColor;


                            try {
                                FloodFill fill = new FloodFill(newBitmap, fillSourceColor, targetColor);
                                fill.floodFill(p1.x, p1.y);
                            } catch (Exception e) {
                                e.getStackTrace();
                            }


                            points.add(new Point(p1.x, p1.y));
                            sourceFillColors.add(fillSourceColor);
                            targetFillColors.add(currentColor);
                        }
                    }

                    //         Show undo redo buttons
                    if (points.size() > 0) {
                        MainActivity.undoButton.setVisibility(View.VISIBLE);
                        MainActivity.clearButton.setVisibility(View.VISIBLE);
                    }


                    if (event.getPointerCount() == 1 && isZoomed()) {
                        mPreviousTranslateX = mTranslateX;
                        mPreviousTranslateY = mTranslateY;
                    }

                    invalidate();


                    break;


            }
        } else {
            counter = counter >= 4 ? 1 : counter;

            MainActivity.mScaleGestureDetector.onTouchEvent(event);
            if (event.getPointerCount() == 1 && counter == 3) {

                onePointer = true;
            }
            counter++;

        }

        if (isZoomed()) {
            MainActivity.zoomOutButton.setVisibility(View.VISIBLE);
        }
        return true;
    }

}
