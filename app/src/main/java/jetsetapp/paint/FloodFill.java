package jetsetapp.paint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Ja on 2017-12-23.
 */

public class FloodFill {

    protected Bitmap image = null;
    protected int[] tolerance = new int[]{20, 20, 20}; // WAS 20
    protected int width = 0;
    protected int height = 0;
    protected int[] pixels = null;
    protected int fillColor = 0;
    protected int[] startColor = new int[]{0, 0, 0};
    protected boolean[] pixelsChecked;
    protected Queue<FloodFillRange> ranges;
    private boolean skipFill;
    protected String[] listColors = new String[]{"#E6B0AA", "#FFEA00", "#40AFFF", "#FF4043",
            "#FF00FF", "#99FF40", "#800000", "#001A00", "#FFFFFF", "#C0C0C0", "#FFBFBF",
            "#FF7373", "#FF4D4D", "#D90000", "#8C0000", "#400000", "#FFCFBF", "#FF9673",
            "#FF5C26", "#D93600", "#8C2300", "#401000", "#FFDFBF", "#FFB973", "#FF9326",
            "#D96D00", "#8C4600", "#402000", "#FFEFBF", "#FFDC73", "#FFC926", "#D9A300",
            "#8C6900", "#403000", "#FFFFBF", "#FFFF73", "#FFFF26", "#D9D900", "#8C8C00",
            "#404000", "#EFFFBF", "#DCFF73", "#C9FF26", "#A3D900", "#698C00", "#304000",
            "#CFFFBF", "#96FF73", "#5CFF26", "#36D900", "#238C00", "#104000", "#BFFFCF",
            "#73FF96", "#26FF5C", "#00D936", "#008C23", "#004010", "#BFFFDF", "#73FFB9",
            "#26FF93", "#00D96D", "#008C46", "#004020", "#BFFFEF", "#73FFDC", "#26FFC9",
            "#00D9A3", "#008C69", "#004030", "#BFFFFF", "#73FFFF", "#26FFFF", "#00D9D9",
            "#008C8C", "#004040", "#BFEFFF", "#73DCFF", "#26C9FF", "#00A3D9", "#00698C",
            "#003040", "#BFDFFF", "#BFCFFF", "#7396FF", "#265CFF", "#0036D9", "#00238C",
            "#001040", "#BFBFFF", "#7373FF", "#2626FF", "#0000D9", "#00008C", "#000040",
            "#EFBFFF", "#DC73FF", "#C926FF", "#A300D9", "#69008C", "#300040", "#FFBFFF",
            "#FF73FF", "#FF26FF", "#D900D9", "#8C008C", "#400040", "#EEEEEE", "#BBBBBB",
            "#8A8A7B", "#575748", "#242415", "#0F0F1E"};


    // Construct using an image and a copy will be made to fill into,
    // Construct with BufferedImage and flood fill will write directly to
    // provided BufferedImage
    public FloodFill(Bitmap img) {
        copyImage(img);
    }

    public FloodFill(Bitmap img, int targetColor, int newColor) {
        useImage(img);
        List<String> list = Arrays.asList(listColors);
        String hexColor = String.format("#%06X", (0xFFFFFF & targetColor));
        Log.i("hexColor", hexColor);
//        skipFill = (targetColor != Color.parseColor("#001A00") && (targetColor >= Color.parseColor("#000000") && targetColor <= Color.parseColor("#000020")));
//        skipFill = !Arrays.asList(list).contains(hexColor);
        skipFill = !list.contains(hexColor);
        setFillColor(newColor);
        setTargetColor(targetColor);
    }

    public void setTargetColor(int targetColor) {
        startColor[0] = Color.red(targetColor);
        startColor[1] = Color.green(targetColor);
        startColor[2] = Color.blue(targetColor);
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int value) {
        fillColor = value;
    }

    public int[] getTolerance() {
        return tolerance;
    }

    public void setTolerance(int[] value) {
        tolerance = value;
    }

    public void setTolerance(int value) {
        tolerance = new int[]{value, value, value};
    }

    public Bitmap getImage() {
        return image;
    }

    private void copyImage(Bitmap img) {
        // Copy data from provided Image to a BufferedImage to write flood fill
        // to, use getImage to retrieve
        // cache data in member variables to decrease overhead of property calls
        width = img.getWidth();
        height = img.getHeight();

        image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        canvas.drawBitmap(img, 0, 0, null);

        pixels = new int[width * height];

        image.getPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
    }

    private void useImage(Bitmap img) {
        // Use a pre-existing provided BufferedImage and write directly to it
        // cache data in member variables to decrease overhead of property calls
        width = img.getWidth();
        height = img.getHeight();
        image = img;

        pixels = new int[width * height];

        image.getPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
    }

    private void prepare() {
        // Called before starting flood-fill
        pixelsChecked = new boolean[pixels.length];
        ranges = new LinkedList<FloodFillRange>();
    }

    // Fills the specified point on the bitmap with the currently selected fill
    // color.
    // int x, int y: The starting coords for the fill
    public void floodFill(int x, int y) {
        if (!skipFill) {
            // Setup
            prepare();

            if (startColor[0] == 0) {
                // ***Get starting color.
                int startPixel = pixels[(width * y) + x];
                startColor[0] = (startPixel >> 16) & 0xff;
                startColor[1] = (startPixel >> 8) & 0xff;
                startColor[2] = startPixel & 0xff;
            }

            // ***Do first call to floodfill.
            LinearFill(x, y);

            // ***Call floodfill routine while floodfill ranges still exist on the
            // queue
            FloodFillRange range;

            while (ranges.size() > 0) {
                // **Get Next Range Off the Queue
                range = ranges.remove();

                // **Check Above and Below Each Pixel in the Floodfill Range
                int downPxIdx = (width * (range.Y + 1)) + range.startX;
                int upPxIdx = (width * (range.Y - 1)) + range.startX;
                int upY = range.Y - 1;// so we can pass the y coord by ref
                int downY = range.Y + 1;

                for (int i = range.startX; i <= range.endX; i++) {
                    // *Start Fill Upwards
                    // if we're not above the top of the bitmap and the pixel above
                    // this one is within the color tolerance
                    if (range.Y > 0 && (!pixelsChecked[upPxIdx])
                            && CheckPixel(upPxIdx))
                        LinearFill(i, upY);

                    // *Start Fill Downwards
                    // if we're not below the bottom of the bitmap and the pixel
                    // below this one is within the color tolerance
                    if (range.Y < (height - 1) && (!pixelsChecked[downPxIdx])
                            && CheckPixel(downPxIdx))
                        LinearFill(i, downY);

                    downPxIdx++;
                    upPxIdx++;
                }
            }

            image.setPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
        }
    }

    // Finds the furthermost left and right boundaries of the fill area
    // on a given y coordinate, starting from a given x coordinate, filling as
    // it goes.
    // Adds the resulting horizontal range to the queue of floodfill ranges,
    // to be processed in the main loop.

    // int x, int y: The starting coords
    private void LinearFill(int x, int y) {
        // ***Find Left Edge of Color Area
        int lFillLoc = x; // the location to check/fill on the left
        int pxIdx = (width * y) + x;

        while (true) {
            // **fill with the color
            pixels[pxIdx] = fillColor;

            // **indicate that this pixel has already been checked and filled
            pixelsChecked[pxIdx] = true;

            // **de-increment
            lFillLoc--; // de-increment counter
            pxIdx--; // de-increment pixel index

            // **exit loop if we're at edge of bitmap or color area
            if (lFillLoc < 0 || (pixelsChecked[pxIdx]) || !CheckPixel(pxIdx)) {
                break;
            }
        }

        lFillLoc++;

        // ***Find Right Edge of Color Area
        int rFillLoc = x; // the location to check/fill on the left

        pxIdx = (width * y) + x;

        while (true) {
            // **fill with the color
            pixels[pxIdx] = fillColor;

            // **indicate that this pixel has already been checked and filled
            pixelsChecked[pxIdx] = true;

            // **increment
            rFillLoc++; // increment counter
            pxIdx++; // increment pixel index

            // **exit loop if we're at edge of bitmap or color area
            if (rFillLoc >= width || pixelsChecked[pxIdx] || !CheckPixel(pxIdx)) {
                break;
            }
        }

        rFillLoc--;

        // add range to queue
        FloodFillRange r = new FloodFillRange(lFillLoc, rFillLoc, y);

        ranges.offer(r);
    }

    // Sees if a pixel is within the color tolerance range.
    private boolean CheckPixel(int px) {
        int red = (pixels[px] >>> 16) & 0xff;//16
        int green = (pixels[px] >>> 8) & 0xff;//8
        int blue = pixels[px] & 0xff;

        return (red >= (startColor[0] - tolerance[0])
                && red <= (startColor[0] + tolerance[0])
                && green >= (startColor[1] - tolerance[1])
                && green <= (startColor[1] + tolerance[1])
                && blue >= (startColor[2] - tolerance[2]) && blue <= (startColor[2] + tolerance[2]));
    }

    // Represents a linear range to be filled and branched from.
    private class FloodFillRange {
        private int startX;
        private int endX;
        private int Y;

        private FloodFillRange(int startX, int endX, int y) {
            this.startX = startX;
            this.endX = endX;
            this.Y = y;
        }
    }
}
