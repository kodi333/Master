package jetsetapp.paint;

import android.app.AlertDialog;
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
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;


public class CanvasView extends android.support.v7.widget.AppCompatImageView {

    private static final float TOLERANCE = 5;
    final Point p1 = new Point();
    //    private final static int PAN = 1;
    public Rect imageRect;
    protected Paint paint = new Paint();
    Context context;
    private CanvasView mImageView;
    private List<Path> paths = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private List<Float> strokes = new ArrayList<>();
    private List<Point> points = new ArrayList<>();
    private List<Point> undonePoints = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Integer> undoneColors = new ArrayList<>();
    private int currentColor = Color.parseColor("#E6B0AA"); // lime
    private float currentStroke = 10F;

    private Bitmap newBitmap;
    private Canvas canvas;
    private Path path = new Path();
    private float mX;
    private float mY;
    private ArrayList<Float> undoneStrokes = new ArrayList<>();
    //    private final static int NONE = 0;
    private List<Integer> sourceFillColors = new ArrayList<>();
    private List<Integer> undoneFillColors = new ArrayList<>();
    private List<Integer> targetFillColors = new ArrayList<>();
    private List<Integer> undoneTargetFillColors = new ArrayList<>();
    boolean onePointer = true;
    private float mTranslateScrollY;

    public CanvasView(Context context) {
        super(context);
        init(context);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        setFocusable(true);
        setFocusableInTouchMode(true);

        mImageView = findViewById(R.id.canvas);

        paint.setDither(true);
        paint.setColor(Color.parseColor("#E6B0AA")); // Lime
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(currentStroke);
        Path path = new Path();
        Canvas canvas = new Canvas();
    }

    private float mStartScrollX;
    private float mStartScrollY;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Bitmap mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mBitmap);
        imageRect = new Rect(0, 0, w, h);
    }

    public void changeColor(int color) {
        currentColor = color;
        path = new Path();
    }

    public int getColor() {
//        int color = currentColor;
        return currentColor;
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

                Log.i("undoStrokes", "undoStrokes");
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
            if (undonePoints.get(undonePoints.size() - 1).x > 0 && undonePoints.get(undonePoints.size() - 1).y > 0) {
                int targetColor = undoneTargetFillColors.get(undoneTargetFillColors.size() - 1);// < 0 ? Color.WHITE : undoneFillColors.get(undoneFillColors.size() - 1);
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

    public void clearCanvas() {

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptView = layoutInflater.inflate(R.layout.about, null); // passing null as parameter should be ok since this
//        is AlertDialog and "  we do not have access to the eventual parent of the layout, so we cannot use it for inflation."
        final AlertDialog.Builder alertD = new AlertDialog.Builder(context);
        alertD.setView(promptView);
        final AlertDialog ad = alertD.show();
        ImageButton ok_button = promptView.findViewById(R.id.ok_button);
        ImageButton no_button = promptView.findViewById(R.id.no_button);


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

                if (undonePoints.size() <= 0) {
                    MainActivity.redoButton.setVisibility(View.INVISIBLE);
                }
                invalidate();
                ad.dismiss();

            }
        });

        no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();

            }
        });


    }

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

        if (newBitmap != null && mImageView != null) {
            newBitmap = Bitmap.createScaledBitmap(newBitmap, getWidth(), getHeight(), false);
            if (mImageView.getScaleX() > 1 || mImageView.getScaleY() > 1) {

//              to limit the scrolling
                int scrollLimitY = imageRect.height() * (int) ScaleListener.mScaleFactor;
                int scrollLimitX = imageRect.width() * (int) ScaleListener.mScaleFactor;

                mTranslateX = Math.min(Math.max(-scrollLimitX, mTranslateX), scrollLimitX);
                mTranslateY = Math.min(Math.max(-scrollLimitY, mTranslateY), scrollLimitY);

                imageRect.set((int) ((mTranslateX)), (int) ((mTranslateY)),
                        getWidth() + (int) ((mTranslateX)),
                        getHeight() + (int) (mTranslateY));
            }
            canvas.drawBitmap(newBitmap, null, imageRect, paint);

        }

        //this where it paints all the paths together
        for (int x = 0; x < paths.size(); x++) {
            if (isZoomed()) {
                paint.setColor(Color.TRANSPARENT);
            } else {
                paint.setColor(colors.get(x));
            }
            paint.setStrokeWidth(strokes.get(x));
            canvas.drawPath(paths.get(x), paint);
        }

        //this is where the path is painted in real time
        paint.setColor(currentColor);
        paint.setStrokeWidth(currentStroke);
        canvas.drawPath(path, paint);


    }

    public void zoomOut() {
        ScaleListener.mScaleFactor = 1;
        mImageView.setScaleX(1);
        mImageView.setScaleY(1);
        if (imageRect != null && newBitmap != null) {
            imageRect.set(0, 0, newBitmap.getWidth(), newBitmap.getHeight());
        }
        MainActivity.zoomOutButton.setVisibility(View.INVISIBLE);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //to avoid null Pointer
        float x;
        float y;
        x = event.getX();
        y = event.getY();


        if (event.getPointerCount() >= 2) {
            onePointer = false;
        }

        if (onePointer) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (!MainActivity.isFillFloodSelected() && !isZoomed()) {

                        // this zeroes tell the code that the point is actually a stroke not a fill
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

                        // /center image
                        zoomOut();

                    } else {
                        mStartX = event.getX() - mPreviousTranslateX;
                        mStartY = event.getY() - mPreviousTranslateY;

                        mStartScrollX = event.getX();
                        mStartScrollY = event.getY();
                    }

                    invalidate();
                    break;


                case MotionEvent.ACTION_MOVE:
                    if (!MainActivity.isFillFloodSelected()) {
                        moveTouch(x, y);
                    }

                    if (event.getPointerCount() == 1 && isZoomed()) {

                        mTranslateX = (event.getX() - mStartX);
                        mTranslateY = (event.getY() - mStartY);

                        mTranslateScrollX = (event.getX() - mStartScrollX);
                        mTranslateScrollY = (event.getY() - mStartScrollY);

                        MainActivity.fillFloodSelected = true;
                        mImageView.changeStroke(0);
//
//                        Log.i("transX", String.valueOf(mTranslateX));
//                        Log.i("transY", String.valueOf(mTranslateY));
//                        Log.i("mStartX", String.valueOf(mStartX));
//                        Log.i("mStartY", String.valueOf(mStartY));
//                        Log.i("mStartScrollX", String.valueOf(mStartScrollX));
//                        Log.i("mStartScrollY", String.valueOf(mStartScrollY));
//                        Log.i("eventX", String.valueOf(event.getX()));
//                        Log.i("eventY", String.valueOf(event.getY()));
//                        Log.i("previousX", String.valueOf(mPreviousTranslateX));
//                        Log.i("previousY", String.valueOf(mPreviousTranslateY));
//                        Log.i("mTranslateScrollX", String.valueOf(mTranslateScrollX));
//                        Log.i("mTranslateScrollY", String.valueOf(mTranslateScrollY));

                    }

                    invalidate();  // this invalidate needs to be here, otherwise the picture doesn't scroll
                    break;


                case MotionEvent.ACTION_UP:

                    mStartScrollX = 0;
                    mStartScrollY = 0;

                    // below is used to fill the color coords calculation, don't change it,
                    // it needs to be more then one due to many >0 conditions in the code
                    // basically zero means it has been stroked and >0 means it is fill
                    if (newBitmap != null) {
                        x = Math.max(1, Math.min((x - mTranslateX), (newBitmap.getWidth() - 5)));
                        y = Math.max(1, Math.min((y - mTranslateY), (newBitmap.getHeight() - 5)));
                    }
                    if (!MainActivity.isFillFloodSelected()) {

                        points.add(new Point(p1.x, p1.y));
                        strokes.add(currentStroke);
                        paths.add(path);
                        colors.add(currentColor);
                        upTouch();

                    } else {

                        int moveTolerance = 10;

                        //fillFlood only when there is no scroll movement nor there is zoom
                        if (Math.abs(mTranslateScrollX) < moveTolerance
                                && Math.abs(mTranslateScrollY) < moveTolerance) {
                            p1.x = (int) x;
                            p1.y = (int) y;
                            //                        newBitmap = Bitmap.createScaledBitmap(newBitmap, getWidth(), getHeight(), false);
                            int fillSourceColor = newBitmap.getPixel((int) x, (int) y);

                            final int targetColor = currentColor;

                            if (onePointer) {
                                try {

                                    FloodFill fill = new FloodFill(newBitmap, fillSourceColor, targetColor);
                                    fill.floodFill(p1.x, p1.y);
                                } catch (Exception e) {
                                    e.getStackTrace();
                                }
                            }

                            Log.i("pointX", String.valueOf(p1.x));
                            Log.i("pointY", String.valueOf(p1.y));
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

                    mTranslateScrollX = 0;
                    mTranslateScrollY = 0;

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
