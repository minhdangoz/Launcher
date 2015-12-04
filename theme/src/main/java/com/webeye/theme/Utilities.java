package com.webeye.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

public class Utilities {

    private static final Canvas sCanvas = new Canvas();
    private static final Rect sOldBounds = new Rect();
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
	private static int sIconSWidth = -1;
    private static int sIconSHeight = -1;
    private static int sIconTextureWidth = -1;
    private static int sIconTextureHeight = -1;
    private static final Paint sIconPaint = new Paint();
    
    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;
        sIconSWidth = sIconSHeight = resources.getDimensionPixelSize(R.dimen.app_icon_texture_size);
    }

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
	
	public static Bitmap createBitmap(Drawable drawable, int width, int height, Context context) {
        try {
			synchronized (sCanvas) { // we share the statics :-(
				if (drawable == null) {
					return null;
				}
			    if (drawable instanceof PaintDrawable) {
			        PaintDrawable painter = (PaintDrawable) drawable;
			        painter.setIntrinsicWidth(width);
			        painter.setIntrinsicHeight(height);
			    } else if (drawable instanceof BitmapDrawable) {
			        // Ensure the bitmap has a density.
			        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			        Bitmap bitmap = bitmapDrawable.getBitmap();
			        if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
			            bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
			        }
			    }
			    int sourceWidth = drawable.getIntrinsicWidth();
			    int sourceHeight = drawable.getIntrinsicHeight();
			    if (width <= 0) {
			    	width = sourceWidth;
			    }
			    if (height <= 0) {
			    	height = sourceHeight;
			    }
			    /*** MODIFYBY: zhaoxy . DATE: 2012-04-05 . START***/
			    /*if (sourceWidth > 0 && sourceHeight > 0) {
			        // There are intrinsic sizes.
			        if (width < sourceWidth || height < sourceHeight) {
			            // It's too big, scale it down.
			            final float ratio = (float) sourceWidth / sourceHeight;
			            if (sourceWidth > sourceHeight) {
			                height = (int) (width / ratio);
			            } else if (sourceHeight > sourceWidth) {
			                width = (int) (height * ratio);
			            }
			        } else if (sourceWidth < width && sourceHeight < height) {
			            // Don't scale up the icon
			            width = sourceWidth;
			            height = sourceHeight;
			        }
			    }*/
			    /*** MODIFYBY: zhaoxy . DATE: 2012-04-05 . END***/

			    final Bitmap bitmap = Bitmap.createBitmap(width, height,
			            Bitmap.Config.ARGB_8888);
			    final Canvas canvas = sCanvas;
			    canvas.setBitmap(bitmap);

			    final int left = 0;
			    final int top = 0;

			    sOldBounds.set(drawable.getBounds());
			    drawable.setBounds(left, top, left+width, top+height);
			    drawable.draw(canvas);
			    drawable.setBounds(sOldBounds);
			    canvas.setBitmap(null);

			    return bitmap;
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
    }


    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // Don't scale up the icon
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                /*// draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);*/
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }
	
    /**
     * <p>
     * Create an icon that uses the specified resources(Background, Foreground and Mask).
     * </p>
     *
     * @param icon
     *            Which icon do u want to modify.May be null.
     * @param bg
     *            Background in the bottom of the composite image. May be null.
     * @param fg
     *            Foreground in the top of the composite image. May be null.
     * @param mask
     *            Only the alpha channel information of this image will be used.
     *            The pixel in icon at the same corresponding location will
     *            change based on this mask. May be null.
     * @param launcherContext
     * @return
     */
    public static Bitmap createIconBitmap(Drawable icon, Bitmap bg, Bitmap fg, Bitmap mask, Context launcherContext) {
        try {
			synchronized (sCanvas) { // we share the statics :-(
			    if (sIconWidth == -1 /*|| sIconWidth != SettingsValue.getIconSizeValue(context)*/) {
			        initStatics(launcherContext);
			    }
			    //48dp 72px
			    /*LauncherApplication app = (LauncherApplication) launcherContext
	    				.getApplicationContext();*/
	    		int isScale = 0 /*app.mLauncherContext.getInteger(
	    				R.integer.config_scale_appicon, R.integer.config_scale_appicon)*/;
	    		int width = (int) (sIconWidth);
				int height = (int) (sIconHeight);
	    		if(isScale == 1){
		    		width = (int) (sIconSWidth);
					height = (int) (sIconSHeight);
	    		}
			    if (icon == null) {
			        icon = launcherContext.getResources().getDrawable(R.drawable.nothing);
			    }
			    if (icon instanceof PaintDrawable) {
			        PaintDrawable painter = (PaintDrawable) icon;
			        painter.setIntrinsicWidth(width);
			        painter.setIntrinsicHeight(height);
			    } else if (icon instanceof BitmapDrawable) {
			        // Ensure the bitmap has a density.
			        BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
			        Bitmap bitmap = bitmapDrawable.getBitmap();
			        if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
			            bitmapDrawable.setTargetDensity(launcherContext.getResources().getDisplayMetrics());
			        }
			    }
			    int sourceWidth = icon.getIntrinsicWidth();
			    int sourceHeight = icon.getIntrinsicHeight();
			    if (sourceWidth > 0 && sourceHeight > 0) {
			        final float ratio = (float) sourceWidth / sourceHeight;
			        if (sourceWidth > sourceHeight) {
			            height = (int) (width / ratio);
			        } else if (sourceHeight > sourceWidth) {
			            width = (int) (height * ratio);
			        }
			        Resources res = launcherContext.getResources();
			        Bitmap lessBitmap = null;
			        if (icon instanceof FastBitmapDrawable) {
			        	lessBitmap = lessenBitmap(((FastBitmapDrawable) icon).getBitmap(), width, height, false);
			            icon = new BitmapDrawable(res,lessBitmap );
			        } else if (icon instanceof BitmapDrawable) {
			        	lessBitmap = lessenBitmap(((BitmapDrawable) icon).getBitmap(), width, height, false);
			            icon = new BitmapDrawable(res,lessBitmap );
			        } else {
			        	lessBitmap =lessenBitmap(drawableToBitmap(icon), width, height, false);
			            icon = new BitmapDrawable(res, lessBitmap);
			        }
			    }
			    // no intrinsic size --> use default size
			    // 54dp 81px
			    int textureWidth = //sIconTextureWidth;
			    		sIconWidth;
			    int textureHeight =// sIconTextureHeight;
			    		sIconHeight;
			    //bugfix zhanglz1
/*	            if (FLAG_DRAWABLE_PADDING) {
			        textureWidth += 2;
			        textureHeight += 2;
			    }*/

			    final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
			    final Canvas canvas = sCanvas;
			    canvas.setBitmap(bitmap);

			    final int left = (textureWidth - width) / 2;
			    final int top = (textureHeight - height) / 2;
			    sOldBounds.set(icon.getBounds());
			    icon.setBounds(left, top, left + width, top + height);
			    BitmapDrawable bd = (BitmapDrawable) icon;
			    Bitmap bm = bd.getBitmap();
			    canvas.drawBitmap(bm, left, top, null);
			    if (mask != null) {
			        if (mask.getWidth() != textureWidth || mask.getHeight() != textureHeight) {
			            mask = Bitmap.createScaledBitmap(mask, textureWidth, textureHeight, true);
			        }
			        sIconPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			        canvas.drawBitmap(mask, 0f, 0f, sIconPaint);
			    }
			    icon.setBounds(sOldBounds);
			    if (bg != null) {
			        if (bg.getWidth() != textureWidth || bg.getHeight() != textureHeight) {
			            bg = Bitmap.createScaledBitmap(bg, textureWidth, textureHeight, true);
			        }
			        sIconPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
			        canvas.drawBitmap(bg, 0f, 0f, sIconPaint);
			    }
			    if (fg != null) {
			        if (fg.getWidth() != textureWidth || fg.getHeight() != textureHeight) {
			            fg = Bitmap.createScaledBitmap(fg, textureWidth, textureHeight, true);
			        }
			        canvas.drawBitmap(fg, 0f, 0f, null);
			    }
			    canvas.setBitmap(null);

			    return bitmap;
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
    }
    
    /**
     * @param src
     * @param destWidth
     * @param destHeight
     * @param needRecycle
     * @return
     */
    public static Bitmap lessenBitmap(Bitmap src, int destWidth, int destHeight, boolean needRecycle) {
        try {
			if (src == null){
			    return null;
		    }
			int w = src.getWidth();
			int h = src.getHeight();
			float scaleWidth = ((float) destWidth) / w;
			float scaleHeight = ((float) destHeight) / h;
			Matrix m = new Matrix();
			m.postScale(scaleWidth, scaleHeight);
			Bitmap resizeBitmap = Bitmap.createBitmap(src, 0, 0, w, h, m, true);
			if (needRecycle && !src.isRecycled()) {
			    src.recycle();
			    src = null;
			}
			return resizeBitmap;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
    }
    
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
		    return drawableToBitmap(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
	}
	
	public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
		Bitmap bitmap = null;
		try {
			bitmap = Bitmap
					.createBitmap(
							1,
							1,
							drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
									: Bitmap.Config.RGB_565);
			bitmap = Bitmap.createScaledBitmap(bitmap,
					width,
					height, true);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, width,
					height);
			drawable.draw(canvas);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	public static Bitmap createIconBitmapForZipTheme(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null && bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                /*canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);*/
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }
	
	/*
     * <p> Create an icon that uses the specified resources(Background, Foreground and Mask). </p>
     *
     * @param icon Which icon do u want to modify.May be null.
     *
     * @param bg Background in the bottom of the composite image. May be null.
     *
     * @param fg Foreground in the top of the composite image. May be null.
     *
     * @param mask Only the alpha channel information of this image will be used. The pixel in icon
     * at the same corresponding location will change based on this mask. May be null.
     *
     * @param context
     *
     * @return
     */

    public static Bitmap composeIcon(Drawable icon, Bitmap bg, Bitmap fg, Bitmap mask, Context context) {
        try {
            synchronized (sCanvas) {
                if (sIconWidth == -1) {
                    initStatics(context);
                }

                if (icon == null) {
                    icon = context.getResources().getDrawable(R.drawable.nothing);
                }
                int width = (int) (sIconWidth);
                int height = (int) (sIconHeight);

                // icon.
                if (icon instanceof PaintDrawable) {
                    PaintDrawable painter = (PaintDrawable) icon;
                    painter.setIntrinsicWidth(width);
                    painter.setIntrinsicHeight(height);
                } else if (icon instanceof BitmapDrawable) {
                    // Ensure the bitmap has a density.
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                        bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                    }
                }

                // width and height
                int sourceWidth = icon.getIntrinsicWidth();
                int sourceHeight = icon.getIntrinsicHeight();

                if (sourceWidth > 0 && sourceHeight > 0) {
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                }

                int textureWidth = sIconWidth;
                int textureHeight = sIconHeight;

                // draw icon
                final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                        Bitmap.Config.ARGB_8888);
                final Canvas canvas = sCanvas;
                canvas.setBitmap(bitmap);

                final int left = (textureWidth - width) / 2;
                final int top = (textureHeight - height) / 2;
                sOldBounds.set(icon.getBounds());
                icon.setBounds(left, top, left + width, top + height);
                icon.draw(canvas);
                icon.setBounds(sOldBounds);

                // draw mask, background and foreground.
                if (mask != null) {
                    sIconPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    canvas.drawBitmap(mask, 0f, 0f, sIconPaint);
                }
                if (bg != null) {
                    sIconPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                    canvas.drawBitmap(bg, 0f, 0f, sIconPaint);
                }
                if (fg != null) {
                    canvas.drawBitmap(fg, 0f, 0f, null);
                }

                canvas.setBitmap(null);
                return bitmap;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    public static int getIconWidth(Context context) {
        if (sIconWidth == -1) {
            initStatics(context);
        }
        return sIconWidth;
    }
}
