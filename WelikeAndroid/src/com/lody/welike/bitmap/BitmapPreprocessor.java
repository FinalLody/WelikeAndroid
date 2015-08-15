package com.lody.welike.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import com.lody.welike.WelikeContext;
import com.lody.welike.utils.ScreenUtils;

import java.io.File;

/**
 * Bitmap预处理器,全权负责Bitmap的缩放和防OOM处理.
 * 你可以用本工具类做你想做的事情.
 *
 * @author Lody
 * @version 1.0
 */
public class BitmapPreprocessor {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 3;
    public static final int BOTTOM = 4;

    /**
     * 按指定宽度和高度缩放图片,不保证宽高比例
     *
     * @param bitmap       要缩放的图片
     * @param targetWidth  目标高度
     * @param targetHeight 目标宽度
     * @return 缩放后的图片
     */

    public static Bitmap zoomBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {

        if (bitmap == null) {
            return null;
        }
        //图片原宽
        int width = bitmap.getWidth();
        //图片原高
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        //宽度缩放比例
        float scaleWidth = ((float) targetWidth / width);
        //高度缩放比例
        float scaleHeight = ((float) targetHeight / height);

        matrix.postScale(scaleWidth, scaleHeight);


        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

    }

    /**
     * 加载指定文件的bitmap,如果出现OOM,就会尝试减少
     *
     * @param imagePath 图片路径
     * @return 加载后的位图
     */
    public static Bitmap decodeFileNoOOM(String imagePath) {
        try {
            return BitmapFactory.decodeFile(imagePath, null);

        } catch (OutOfMemoryError e) {

        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, opts);
        int[] wh = ScreenUtils.getScreenWidthAndHeight(WelikeContext.getApplication());
        int width = wh[0] > opts.outWidth ? opts.outWidth : wh[0];
        int height = wh[1] > opts.outHeight ? opts.outHeight : wh[1];
        opts.inSampleSize = computeSampleSize(opts, -1, width * height);
        opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, opts);
    }


    /**
     * 加载指定文件的bitmap,如果出现OOM,就会尝试减少
     *
     * @param data 图片对应的字节数组
     * @return 加载后的位图
     */
    public static Bitmap decodeBitmapNoOOM(byte[] data) {
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);

        } catch (OutOfMemoryError e) {

        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        int[] wh = ScreenUtils.getScreenWidthAndHeight(WelikeContext.getApplication());
        int width = wh[0] > opts.outWidth ? opts.outWidth : wh[0];
        int height = wh[1] > opts.outHeight ? opts.outHeight : wh[1];
        opts.inSampleSize = computeSampleSize(opts, -1, width * height);
        opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
    }


    /**
     * 加载指定文件的bitmap,如果出现OOM,就会尝试减少
     *
     * @param data 图片对应的字节数组
     * @param reqWidget 理想宽度
     * @param reqHeight 理想高度
     * @return 加载后的位图
     */
    public static Bitmap decodeBitmapNoOOM(byte[] data,int reqWidget,int reqHeight) {
        try {
            return BitmapFactory.decodeByteArray(data,0,data.length);

        } catch (OutOfMemoryError e) {

        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data,0,data.length,opts);
        opts.inSampleSize = computeSampleSize(opts, -1, reqWidget * reqHeight);
        opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(data,0,data.length,opts);
    }


    public static Bitmap decodeFileNoOOM(File imageFile) {
        return decodeFileNoOOM(imageFile.getAbsolutePath());
    }


    /**
     * @param options        inJustDecodeBounds为true并且已经decode过的Options.
     * @param minSideLength  用于指定最小宽度或高度
     * @param maxNumOfPixels 用于指定的最大大小(以像素为单位)可容忍的内存使用情况。
     * @return
     */
    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {

        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /** */
    /**
     * 图片去色,返回灰度图片
     *
     * @param bmpOriginal 传入的图片
     * @return 去色后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bitmap;
    }

    /**
     * 把图片变成圆角
     *
     * @param bitmap 需要修改的图片
     * @param pixels 圆角的弧度
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 把图片变成圆角
     *
     * @param bitmapDrawable 需要修改的图片
     * @param pixels         圆角的弧度
     * @return
     */
    public static BitmapDrawable toRoundCorner(BitmapDrawable bitmapDrawable,
                                               int pixels) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        bitmapDrawable = new BitmapDrawable(WelikeContext.getApplication().getResources(),
                toRoundCorner(bitmap, pixels));
        return bitmapDrawable;
    }

    /**
     * 给图片打上水印
     *
     * @param src       源图片
     * @param watermark 水印
     * @return
     */
    public static Bitmap createBitmapForWatermark(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int ww = watermark.getWidth();
        int wh = watermark.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newBitmap);

        cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
        //画水印
        cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, null);// 在src的右下角画入水印

        cv.save(Canvas.ALL_SAVE_FLAG);// 保存

        cv.restore();// 存储
        return newBitmap;
    }

    /**
     * 图片合成,把多张图片放到一起
     *
     * @return 合并后的图片
     */
    public static Bitmap mixBitmap(int direction, Bitmap... bitmaps) {
        if (bitmaps.length <= 0) {
            return null;
        }
        if (bitmaps.length == 1) {
            return bitmaps[0];
        }
        Bitmap newBitmap = bitmaps[0];
        for (int i = 1; i < bitmaps.length; i++) {
            newBitmap = mixBitmap(newBitmap, bitmaps[i], direction);
        }
        return newBitmap;
    }


    /**
     * 图片合成,把两张图片放到一起
     *
     * @param first     第一张图片
     * @param second    第二张图片
     * @param direction 合并后的图片
     * @return
     */
    private static Bitmap mixBitmap(Bitmap first, Bitmap second,
                                    int direction) {
        if (first == null) {
            return null;
        }
        if (second == null) {
            return first;
        }
        int fw = first.getWidth();
        int fh = first.getHeight();
        int sw = second.getWidth();
        int sh = second.getHeight();
        Bitmap newBitmap = null;
        if (direction == LEFT) {
            newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, sw, 0, null);
            canvas.drawBitmap(second, 0, 0, null);
        } else if (direction == RIGHT) {
            newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, fw, 0, null);
        } else if (direction == TOP) {
            newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, 0, sh, null);
            canvas.drawBitmap(second, 0, 0, null);
        } else if (direction == BOTTOM) {
            newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, 0, fh, null);
        }
        return newBitmap;
    }


}
