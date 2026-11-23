package com.fireframe.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.service.dreams.DreamService;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

// Daydream of bundled scenic stills. No network. Stays black if assets/screensaver is missing.
public class ScenicDream extends DreamService {
    static final String DIR = "screensaver";
    static final long SLIDE_MS = 60000;
    static final int LONG_EDGE = 1920;
    static final float ZOOM = 0.08f;
    static final float PAN = 0.08f;

    Handler handler;
    KenBurnsView image;
    TextView caption;
    String[] names = new String[0];
    int index;
    Bitmap showing;

    final Runnable advance = new Runnable() {
        @Override public void run() {
            if (names.length == 0) return;
            index = (index + 1) % names.length;
            showCurrent();
            if (handler != null) handler.postDelayed(this, SLIDE_MS);
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);
        handler = new Handler();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        image = new KenBurnsView(this);
        root.addView(image, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        caption = new TextView(this);
        caption.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Medium.ttf"));
        caption.setTextColor(Color.WHITE);
        caption.setTextSize(28);
        FrameLayout.LayoutParams cap = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        cap.gravity = Gravity.BOTTOM | Gravity.START;
        int pad = (int) (48 * getResources().getDisplayMetrics().density);
        cap.leftMargin = pad;
        cap.bottomMargin = pad;
        root.addView(caption, cap);
        setContentView(root);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        HomeService.onDreamStarted();
        names = listJpgs();
        index = 0;
        if (names.length == 0 || handler == null) return;
        showCurrent();
        handler.postDelayed(advance, SLIDE_MS);
    }

    @Override
    public void onDreamingStopped() {
        if (handler != null) handler.removeCallbacks(advance);
        HomeService.onDreamStopped();
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
        if (handler != null) handler.removeCallbacks(advance);
        if (image != null) image.clear();
        if (showing != null && !showing.isRecycled()) showing.recycle();
        showing = null;
        super.onDetachedFromWindow();
    }

    String[] listJpgs() {
        String[] all;
        try {
            all = getAssets().list(DIR);
        } catch (IOException e) {
            return new String[0];
        }
        if (all == null) return new String[0];
        int n = 0;
        for (int i = 0; i < all.length; i++) {
            if (isJpg(all[i])) n++;
        }
        String[] jpg = new String[n];
        int j = 0;
        for (int i = 0; i < all.length; i++) {
            if (isJpg(all[i])) jpg[j++] = all[i];
        }
        Arrays.sort(jpg);
        return jpg;
    }

    static boolean isJpg(String name) {
        return name != null && name.toLowerCase().endsWith(".jpg");
    }

    void showCurrent() {
        if (names.length == 0 || image == null) return;
        final Bitmap next = decode(DIR + "/" + names[index]);
        if (next == null) return;
        final Bitmap previous = showing;
        showing = next;
        caption.setText(captionFor(names[index]));
        image.setSlide(next, index, SystemClock.uptimeMillis());
        recycleIfIdle(previous);
    }

    void recycleIfIdle(Bitmap bitmap) {
        if (bitmap != null && bitmap != showing && !bitmap.isRecycled()) bitmap.recycle();
    }

    // yosemite-valley.jpg -> "Yosemite Valley"
    static String captionFor(String filename) {
        String base = filename;
        int slash = base.lastIndexOf('/');
        if (slash >= 0) base = base.substring(slash + 1);
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);
        String[] words = base.replace('_', '-').split("-");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() == 0) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(words[i].charAt(0)));
            if (words[i].length() > 1) sb.append(words[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    Bitmap decode(String path) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        InputStream in = null;
        try {
            in = getAssets().open(path);
            BitmapFactory.decodeStream(in, null, bounds);
        } catch (IOException e) {
            return null;
        } finally {
            close(in);
        }
        int edge = Math.max(bounds.outWidth, bounds.outHeight);
        int sample = 1;
        while (sample * 2 <= edge / LONG_EDGE) sample *= 2;
        if (edge > 0 && edge / sample > LONG_EDGE * 2) sample *= 2;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        try {
            in = getAssets().open(path);
            return BitmapFactory.decodeStream(in, null, opts);
        } catch (IOException e) {
            return null;
        } finally {
            close(in);
        }
    }

    static void close(InputStream in) {
        if (in == null) return;
        try {
            in.close();
        } catch (IOException ignored) {}
    }

    // Draws the still opaque every frame. Dream windows drop View alpha animations.
    static final class KenBurnsView extends View {
        Bitmap bitmap;
        int slideIndex;
        long startedAt;
        final Matrix matrix = new Matrix();
        final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        KenBurnsView(Context context) {
            super(context);
        }

        void setSlide(Bitmap next, int index, long started) {
            bitmap = next;
            slideIndex = index;
            startedAt = started;
            invalidate();
        }

        void clear() {
            bitmap = null;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Bitmap frame = bitmap;
            if (frame == null || frame.isRecycled()) return;
            int vw = getWidth();
            int vh = getHeight();
            if (vw <= 0 || vh <= 0) return;
            float bw = frame.getWidth();
            float bh = frame.getHeight();
            if (bw <= 0f || bh <= 0f) return;

            float scale0 = Math.max(vw / bw, vh / bh);
            long elapsed = SystemClock.uptimeMillis() - startedAt;
            float t = 0f;
            if (elapsed >= SLIDE_MS) t = 1f;
            else if (elapsed > 0) t = elapsed / (float) SLIDE_MS;
            float s = t * t * (3f - 2f * t);
            float z = (slideIndex % 2 == 0) ? (1f + ZOOM * s) : (1f + ZOOM * (1f - s));
            float draw = scale0 * z;
            float dw = bw * draw;
            float dh = bh * draw;
            float extraX = bw * scale0 * (z - 1f);
            float extraY = bh * scale0 * (z - 1f);
            float sx = ((slideIndex & 1) == 0) ? 1f : -1f;
            float sy = ((slideIndex & 2) == 0) ? 1f : -1f;
            float tx = (vw - dw) / 2f + sx * PAN * extraX * s;
            float ty = (vh - dh) / 2f + sy * PAN * extraY * s;
            float minX = vw - dw;
            float minY = vh - dh;
            if (tx > 0f) tx = 0f;
            if (tx < minX) tx = minX;
            if (ty > 0f) ty = 0f;
            if (ty < minY) ty = minY;

            matrix.setScale(draw, draw);
            matrix.postTranslate(tx, ty);
            canvas.save();
            canvas.concat(matrix);
            canvas.drawBitmap(frame, 0f, 0f, paint);
            canvas.restore();
            if (t < 1f) postInvalidateOnAnimation();
        }
    }
}
