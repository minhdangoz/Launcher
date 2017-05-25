package com.kapp.kinflow.view.recyclerview.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/04/01 11:09
 */

public class LinearDecoration extends RecyclerView.ItemDecoration {

    private static final boolean DEBUG = true;
    private static final String TAG = LinearDecoration.class.getSimpleName();
    DecorationFactory.HeaderPainter mHeaderPainter;

    DecorationFactory.FooterPainter mFooterPainter;

    DecorationFactory.MarginProvider mMarginProvider;

    DecorationFactory.VisibilityProvider mVisibilityProvider;

    DecorationFactory.DecorationPainter mDecorationPainter;

    DecorationFactory.OrientationHandler mOrientationHandler;

    DecorationFactory.SizeProvider mSizeProvider;

    LinearDecoration(Builder builder) {
        mHeaderPainter = builder.mHeaderPainter;
        mFooterPainter = builder.mFooterPainter;
        mMarginProvider = builder.mMarginProvider;
        mVisibilityProvider = builder.mVisibilityProvider;
        mDecorationPainter = builder.mDecorationPainter;
        mOrientationHandler = builder.mOrientationHandler;
        mSizeProvider = builder.mSizeProvider;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();
        int lastDividerOffset = 1;
        if (position >= itemCount - lastDividerOffset) {
            if (mFooterPainter == null) {
                return;
            }
            int size = mFooterPainter.generateFooterSize(parent);
            mOrientationHandler.setFooterOffsets(outRect, size);
            return;
        }
        if (mHeaderPainter != null && position == 0) {
            int size = mHeaderPainter.generateHeaderSize(parent);
            mOrientationHandler.setHeaderOffsets(outRect, size);
        }
        if (mSizeProvider == null) {
            return;
        }
        int itemDividerSize = mSizeProvider.dividerSize(position, parent);
        logError("position = " + position + ",dividerSize = " + itemDividerSize);
        mOrientationHandler.setItemOffsets(outRect, position, itemDividerSize);


    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null || adapter.getItemCount() <= 0) {
            return;
        }
        int validChildCount = parent.getChildCount();
        if (mHeaderPainter != null && validChildCount > 0) {
            int mHeaderSize = mHeaderPainter.generateHeaderSize(parent);
            int marginStart = mMarginProvider == null ? 0 : mMarginProvider.startMargin(-1, parent);
            int marginEnd = mMarginProvider == null ? 0 : mMarginProvider.endMargin(-1, parent);
            View child = parent.getChildAt(0);
            if (child != null) {
                Rect headerBounds = mOrientationHandler.headerBounds(parent, child, marginStart, marginEnd,
                        mHeaderSize);
                mHeaderPainter.painting(parent, c, headerBounds);
            }

        }
        if (mDecorationPainter == null || mSizeProvider == null) {
            return;
        }
        int itemCount = adapter.getItemCount();
        int lastDividerOffset = 1;

        int lastChildPosition = RecyclerView.NO_POSITION;
        for (int i = 0; i < validChildCount; i++) {
            View child = parent.getChildAt(i);
            int childPosition = parent.getChildAdapterPosition(child);

            if (childPosition <= lastChildPosition) {
                continue;
            }
            lastChildPosition = childPosition;

            if (childPosition >= itemCount - lastDividerOffset) {
                if (mFooterPainter == null) {
                    continue;
                }
                int mFooterSize = mFooterPainter.generateFooterSize(parent);
                int marginStart = mMarginProvider == null ? 0 : mMarginProvider.startMargin(childPosition, parent);
                int marginEnd = mMarginProvider == null ? 0 : mMarginProvider.endMargin(childPosition, parent);
                Rect footerBounds = mOrientationHandler.itemBounds(parent, child, marginStart, marginEnd, mFooterSize);
                mFooterPainter.painting(parent, c, footerBounds);
                continue;
            }

            if (mVisibilityProvider != null && mVisibilityProvider.shouldHideDivider(childPosition, parent)) {
                continue;
            }
            int marginStart = mMarginProvider == null ? 0 : mMarginProvider.startMargin(childPosition, parent);
            int marginEnd = mMarginProvider == null ? 0 : mMarginProvider.endMargin(childPosition, parent);
            int dividerSize = mSizeProvider.dividerSize(childPosition, parent);
            Rect bounds = mOrientationHandler.itemBounds(parent, child, marginStart, marginEnd, dividerSize);
            mDecorationPainter.painting(parent, c, bounds, childPosition);

        }
    }

    public static class Builder {

        private static final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        DecorationFactory.HeaderPainter mHeaderPainter;

        DecorationFactory.FooterPainter mFooterPainter;

        DecorationFactory.DecorationPainter mDecorationPainter;

        DecorationFactory.VisibilityProvider mVisibilityProvider;

        DecorationFactory.MarginProvider mMarginProvider;

        DecorationFactory.OrientationHandler mOrientationHandler;

        DecorationFactory.SizeProvider mSizeProvider;
        @DecorationFactory.Orientation
        final int mOrientation;

        public Builder() {
            this(DecorationFactory.VERTICAL);
        }

        public Builder(@DecorationFactory.Orientation int orientation) {
            this.mOrientation = orientation;
        }

        public boolean isVerticalLayout() {
            return mOrientation == DecorationFactory.VERTICAL;
        }

        public Builder paint(final Paint paint) {
            return paintProvider(new DecorationFactory.PaintProvider() {
                @Override
                public Paint providePaint(int position, RecyclerView parent) {
                    return paint;
                }
            });
        }

        public Builder paintProvider(final DecorationFactory.PaintProvider provider) {
            mSizeProvider = new DecorationFactory.SizeProvider() {
                @Override
                public int dividerSize(int position, RecyclerView parent) {
                    return (int) provider.providePaint(position, parent).getStrokeWidth();
                }
            };
            return decorationPainter(new DecorationFactory.PaintPainter(provider));
        }

        public Builder color(final int color) {
            return colorProvider(new DecorationFactory.ColorProvider() {
                @Override
                public int dividerColor(int position, RecyclerView parent) {
                    return color;
                }
            });
        }

        public Builder colorProvider(DecorationFactory.ColorProvider colorProvider) {
            return decorationPainter(new DecorationFactory.ColorPainter(colorProvider));
        }

        public Builder drawable(final Drawable d) {
            return drawableProvider(new DecorationFactory.DrawableProvider() {
                @Override
                public Drawable drawableProvider(int position, RecyclerView parent) {
                    return d;
                }
            });
        }

        public Builder drawableProvider(final DecorationFactory.DrawableProvider drawableProvider) {
            mSizeProvider = new DecorationFactory.SizeProvider() {
                @Override
                public int dividerSize(int position, RecyclerView parent) {
                    Drawable d = drawableProvider.drawableProvider(position, parent);
                    return isVerticalLayout() ? d.getIntrinsicHeight() : d.getIntrinsicWidth();
                }
            };
            return decorationPainter(new DecorationFactory.DrawablePainter(drawableProvider));
        }

        public Builder defaultPainter(Context context) {
            TypedArray a = context.obtainStyledAttributes(ATTRS);
            final Drawable divider = a.getDrawable(0);
            a.recycle();
            return drawableProvider(new DecorationFactory.DrawableProvider() {
                @Override
                public Drawable drawableProvider(int position, RecyclerView parent) {
                    return divider;
                }
            });
        }

        public Builder decorationPainter(DecorationFactory.DecorationPainter painter) {
            this.mDecorationPainter = painter;
            return this;
        }

        public Builder visibilityProvider(DecorationFactory.VisibilityProvider visibilityProvider) {
            this.mVisibilityProvider = visibilityProvider;
            return this;
        }

        public Builder size(int size) {
            return size(generateSizeProvider(size));
        }

        public Builder size(DecorationFactory.SizeProvider provider) {
            if (mSizeProvider != null) {
                logError("mSizeProvider with set or auto generate may be replaced");
            }
            mSizeProvider = provider;
            return this;
        }

        public Builder header(final Paint painter) {
            return header(new DecorationFactory.HeaderPainter() {
                @Override
                public void painting(RecyclerView parent, Canvas canvas, Rect bounds) {
                    canvas.drawRect(bounds, painter);
                }

                @Override
                public int generateHeaderSize(RecyclerView parent) {
                    return (int) painter.getStrokeWidth();
                }
            });
        }

        public Builder header(final int color, final int size) {
            return header(new DecorationFactory.HeaderPainter() {
                @Override
                public void painting(RecyclerView parent, Canvas canvas, Rect bounds) {
                    Paint p = new Paint();
                    p.setColor(color);
                    canvas.drawRect(bounds, p);
                }

                @Override
                public int generateHeaderSize(RecyclerView parent) {
                    return size;
                }
            });
        }

        public Builder header(final Drawable drawable) {
            return header(new DecorationFactory.HeaderPainter() {
                @Override
                public void painting(RecyclerView parent, Canvas canvas, Rect bounds) {
                    drawable.setBounds(bounds);
                    drawable.draw(canvas);
                }

                @Override
                public int generateHeaderSize(RecyclerView parent) {
                    return isVerticalLayout() ? drawable.getIntrinsicHeight() : drawable.getIntrinsicWidth();
                }
            });
        }

        public Builder header(DecorationFactory.HeaderPainter painter) {
            mHeaderPainter = painter;
            return this;
        }

        public Builder footer(final Paint painter) {
            return footer(new DecorationFactory.FooterPainter() {
                @Override
                public void painting(RecyclerView parent, Canvas canvas, Rect bounds) {
                    canvas.drawRect(bounds, painter);
                }

                @Override
                public int generateFooterSize(RecyclerView parent) {
                    return (int) painter.getStrokeWidth();
                }
            });
        }

        public Builder footer(final int color, final int size) {
            return footer(new DecorationFactory.FooterPainter() {
                @Override
                public void painting(RecyclerView parent, Canvas canvas, Rect bounds) {
                    Paint p = new Paint();
                    p.setColor(color);
                    canvas.drawRect(bounds, p);
                }

                @Override
                public int generateFooterSize(RecyclerView parent) {
                    return size;
                }
            });
        }

        public Builder footer(final Drawable drawable) {
            return footer(new DecorationFactory.FooterPainter() {
                @Override
                public void painting(RecyclerView parent, Canvas canvas, Rect bounds) {
                    drawable.setBounds(bounds);
                    drawable.draw(canvas);
                }

                @Override
                public int generateFooterSize(RecyclerView parent) {
                    return isVerticalLayout() ? drawable.getIntrinsicHeight() : drawable.getIntrinsicWidth();
                }
            });
        }

        public Builder footer(DecorationFactory.FooterPainter painter) {
            mFooterPainter = painter;
            return this;
        }

        public Builder margin(final int leftMargin, final int rightMargin) {
            return marginProvider(new DecorationFactory.MarginProvider() {
                @Override
                public int startMargin(int position, RecyclerView parent) {
                    return leftMargin;
                }

                @Override
                public int endMargin(int position, RecyclerView parent) {
                    return rightMargin;
                }
            });
        }

        public Builder margin(int horizontalMargin) {
            return margin(horizontalMargin, horizontalMargin);
        }

        public Builder marginProvider(DecorationFactory.MarginProvider provider) {
            mMarginProvider = provider;
            return this;
        }

        DecorationFactory.SizeProvider generateSizeProvider(final int size) {
            return new DecorationFactory.SizeProvider() {
                @Override
                public int dividerSize(int position, RecyclerView parent) {
                    return size;
                }
            };
        }

        public LinearDecoration build() {
            checkParams();
            return new LinearDecoration(this);
        }

        static class HorizontalHandler implements DecorationFactory.OrientationHandler {

            @Override
            public Rect headerBounds(RecyclerView parent, View child, int marginStart, int marginEnd, int dividerSize) {
                Rect bounds = new Rect(0, 0, 0, 0);
                int transitionX = (int) ViewCompat.getTranslationX(child);
                int transitionY = (int) ViewCompat.getTranslationY(child);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                bounds.top = parent.getPaddingTop() + marginStart + transitionY;
                bounds.bottom = parent.getHeight() - parent.getPaddingBottom() - marginEnd + transitionY;
                bounds.right = child.getLeft() + params.leftMargin + transitionX;
                bounds.left = bounds.left - dividerSize;
                return bounds;
            }

            @Override
            public Rect itemBounds(RecyclerView parent, View child, int marginStart, int marginEnd, int dividerSize) {
                Rect bounds = new Rect(0, 0, 0, 0);
                int transitionX = (int) ViewCompat.getTranslationX(child);
                int transitionY = (int) ViewCompat.getTranslationY(child);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                bounds.top = parent.getPaddingTop() + marginStart + transitionY;
                bounds.bottom = parent.getHeight() - parent.getPaddingBottom() - marginEnd + transitionY;
                bounds.left = child.getRight() + params.leftMargin + transitionX;
                bounds.right = bounds.left + dividerSize;
                return bounds;
            }

            @Override
            public void setItemOffsets(Rect outRect, int itemIndex, int dividerSize) {
                outRect.right = dividerSize;
            }

            @Override
            public void setHeaderOffsets(Rect outRect, int dividerSize) {
                outRect.left = dividerSize;
            }

            @Override
            public void setFooterOffsets(Rect outRect, int dividerSize) {
                outRect.right = dividerSize;
            }
        }

        static class VerticalHandler implements DecorationFactory.OrientationHandler {
            @Override
            public Rect headerBounds(RecyclerView parent, View child, int marginStart, int marginEnd, int dividerSize) {
                Rect bounds = new Rect(0, 0, 0, 0);
                int transitionX = (int) ViewCompat.getTranslationX(child);
                int transitionY = (int) ViewCompat.getTranslationY(child);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                bounds.left = parent.getPaddingLeft() + marginStart + transitionX;
                bounds.right = parent.getWidth() - parent.getPaddingRight() - marginEnd + transitionX;
                bounds.bottom = child.getTop() + params.topMargin + transitionY;
                bounds.top = bounds.bottom - dividerSize;
                return bounds;
            }

            @Override
            public Rect itemBounds(RecyclerView parent, View child, int marginStart, int marginEnd, int dividerSize) {
                Rect bounds = new Rect(0, 0, 0, 0);
                int transitionX = (int) ViewCompat.getTranslationX(child);
                int transitionY = (int) ViewCompat.getTranslationY(child);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                bounds.left = parent.getPaddingLeft() + marginStart + transitionX;
                bounds.right = parent.getWidth() - parent.getPaddingRight() - marginEnd + transitionX;
                bounds.top = child.getBottom() + params.topMargin + transitionY;
                bounds.bottom = bounds.top + dividerSize;
                return bounds;
            }

            @Override
            public void setItemOffsets(Rect outRect, int itemIndex, int dividerSize) {
                outRect.bottom = dividerSize;
            }

            @Override
            public void setHeaderOffsets(Rect outRect, int dividerSize) {
                outRect.top = dividerSize;
            }

            @Override
            public void setFooterOffsets(Rect outRect, int dividerSize) {
                outRect.bottom = dividerSize;
            }
        }

        void checkParams() {


            if (mDecorationPainter == null) {
                logError("DecorationPainter is null");
                if (mSizeProvider == null) {
                    logError("offset size will be zero");
                }
            }
            if (mVisibilityProvider == null) {
                logError("VisibilityProvider is null");
            }
            if (mMarginProvider == null) {
                logError("MarginProvider is null");
            }
            if (mOrientationHandler == null) {
                mOrientationHandler = isVerticalLayout() ? new VerticalHandler() : new HorizontalHandler();
            }


        }

    }

    private static void logError(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }
}
