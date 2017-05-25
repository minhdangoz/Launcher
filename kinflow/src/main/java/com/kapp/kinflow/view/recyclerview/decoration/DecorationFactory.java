package com.kapp.kinflow.view.recyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * description：分割线绘制工厂
 * <br>author：caowugao
 * <br>time： 2017/04/01 11:10
 */

public class DecorationFactory {
    public final static int HORIZONTAL = 0;

    public final static int VERTICAL = 1;

    @IntDef({HORIZONTAL, VERTICAL})
    public @interface Orientation {
    }

    public interface PaintProvider {
        Paint providePaint(int position, RecyclerView parent);
    }

    public interface SizeProvider {
        int dividerSize(int position, RecyclerView parent);
    }

    public interface DrawableProvider {
        Drawable drawableProvider(int position, RecyclerView parent);
    }

    public interface ColorProvider {
        int dividerColor(int position, RecyclerView parent);
    }

    public interface VisibilityProvider {

        boolean shouldHideDivider(int position, RecyclerView parent);
    }

    public interface MarginProvider {

        int startMargin(int position, RecyclerView parent);

        int endMargin(int position, RecyclerView parent);
    }

    public interface DecorationPainter {

        void painting(RecyclerView recyclerView, Canvas canvas, Rect bounds, int position);
    }


    public interface OrientationHandler {
        Rect headerBounds(RecyclerView parent, View child, int marginStart, int marginEnd, int dividerSize);

        Rect itemBounds(RecyclerView parent, View child, int marginStart, int marginEnd, int dividerSize);

        void setItemOffsets(Rect outRect, int itemIndex, int dividerSize);

        void setHeaderOffsets(Rect outRect, int dividerSize);

        void setFooterOffsets(Rect outRect, int dividerSize);
    }

    public interface HeaderPainter {

        void painting(RecyclerView parent, Canvas canvas, Rect bounds);

        int generateHeaderSize(RecyclerView parent);
    }

    public interface FooterPainter {

        void painting(RecyclerView parent, Canvas canvas, Rect bounds);

        int generateFooterSize(RecyclerView parent);
    }

    //some implements

    //Generate sizeProvider auto
    static class PaintPainter implements DecorationPainter {
        public PaintProvider mPaintProvider;

        private Paint mPaint;

        public PaintPainter(@NonNull PaintProvider mPaintProvider) {
            this.mPaintProvider = mPaintProvider;
        }

        @Override
        public void painting(RecyclerView recyclerView, Canvas canvas, Rect bounds, int position) {
            mPaint = mPaintProvider.providePaint(position, recyclerView);
            canvas.drawRect(bounds, mPaint);
        }
    }

    static class ColorPainter implements DecorationPainter {
        private ColorProvider mColorProvider;

        private Paint mPaint;

        public ColorPainter(ColorProvider mColorProvider) {
            this.mColorProvider = mColorProvider;
            mPaint = new Paint();
        }

        @Override
        public void painting(RecyclerView recyclerView, Canvas canvas, Rect bounds, int position) {
            int color = mColorProvider.dividerColor(position, recyclerView);
            mPaint.setColor(color);
            canvas.drawRect(bounds, mPaint);
        }
    }

    //Generate sizeProvider auto
    static class DrawablePainter implements DecorationPainter {

        private DrawableProvider mDrawableProvider;

        public DrawablePainter(DrawableProvider mDrawableProvider) {
            this.mDrawableProvider = mDrawableProvider;
        }

        @Override
        public void painting(RecyclerView recyclerView, Canvas canvas, Rect bounds, int position) {
            Drawable drawable = mDrawableProvider.drawableProvider(position, recyclerView);
            drawable.setBounds(bounds);
            drawable.draw(canvas);
        }
    }


}

