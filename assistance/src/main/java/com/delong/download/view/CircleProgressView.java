package com.delong.download.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.delong.download.R;


/**
 * Created by zhuruqiao on 2016/11/30.
 */

public class CircleProgressView extends View {

    private Context mContext;

    private float progressTextSize = 0.2f;
    private int progressWidth = 10;
    private int progressTextColor = 0XFFFFFFFF;
    private int strokeColor = 0XFFFFFFFF;
    private int backgroundColor = 0X00000000;
    private int progressColor = 0XFF1FA0DC;
    private int centerColor = 0X00000000;
    private int progress = 0;
    private int strokeWidth = 2;
    private int pauseColor = 0XFF00FF00;

    private boolean install;


    private Paint strokePaint = new Paint();
    private Paint progressPaint = new Paint();
    private Paint centerCirclePaint = new Paint();

    private Paint progressTextPaint = new Paint();
    private Paint pauseButtonPaint = new Paint();
    private Paint installPaint = new Paint();

    Paint bgPaint = new Paint();

    private int width;

    private int height;

    private boolean pause;

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(context, attrs);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);


    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CircleProgressView);

        try {
            progressTextSize = typedArray.getFloat(R.styleable.CircleProgressView_cpv_text_size, progressTextSize);
            progressTextColor = typedArray.getColor(R.styleable.CircleProgressView_cpv_text_color, progressTextColor);
            strokeColor = typedArray.getColor(R.styleable.CircleProgressView_cpv_stroke_color, strokeColor);
            backgroundColor = typedArray.getColor(R.styleable.CircleProgressView_cpv_bg, backgroundColor);
            progressColor = typedArray.getColor(R.styleable.CircleProgressView_cpv_progress_color, progressColor);
            progress = typedArray.getColor(R.styleable.CircleProgressView_cpv_progress, progress);
            centerColor = typedArray.getColor(R.styleable.CircleProgressView_cpv_center_color, centerColor);
            strokeWidth = typedArray.getDimensionPixelOffset(R.styleable.CircleProgressView_cpv_stroke_width, strokeWidth);
            progressWidth = typedArray.getDimensionPixelOffset(R.styleable.CircleProgressView_cpv_progress_width, progressWidth);
            pauseColor = typedArray.getColor(R.styleable.CircleProgressView_cpv_pause_color, pauseColor);

        } finally {
            typedArray.recycle();
        }

        strokePaint.setColor(strokeColor);
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidth);

        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        //以圆环为半径，向外扩张strokeWidth
        progressPaint.setStrokeWidth(progressWidth);

        centerCirclePaint.setColor(centerColor);
        centerCirclePaint.setAntiAlias(true);
        centerCirclePaint.setStyle(Paint.Style.FILL);

        progressTextPaint.setColor(progressTextColor);
        progressTextPaint.setAntiAlias(true);

        pauseButtonPaint.setColor(pauseColor);
        pauseButtonPaint.setAntiAlias(true);
        pauseButtonPaint.setStrokeWidth(5);
        pauseButtonPaint.setStrokeCap(Paint.Cap.BUTT);
        pauseButtonPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        installPaint.setColor(0xFF05C11B);
        installPaint.setAntiAlias(true);
        installPaint.setStyle(Paint.Style.FILL);

        bgPaint.setColor(backgroundColor);
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        loadViewSize(canvas);
        if (install) {
            installPaint.setColor(0xFF05C11B);
            canvas.drawCircle(width / 2, height / 2, getRadius() * 2 / 3, installPaint);
            installPaint.setColor(0XFFFFFFFF);
            installPaint.setTextSize(getRadius() * 3 / 7);
            float textLength = installPaint.measureText("安装");
            Paint.FontMetrics fontMetrics = installPaint.getFontMetrics();
            float textHeight = Math.abs(fontMetrics.ascent);
            canvas.drawText("安装", width / 2 - textLength / 2, height / 2 + textHeight / 2, installPaint);
            return;
        }
        //画背景
        canvas.drawRoundRect(getRectF(canvas), getRadius()/6,getRadius()/6, bgPaint);

        RectF rect = new RectF(width / 2 - getRadius() * 2 / 3 + progressWidth / 2, height / 2 - getRadius() * 2 / 3 + progressWidth / 2, width / 2 + getRadius() * 2 / 3
                - progressWidth / 2, height / 2 + getRadius() * 2 / 3 - progressWidth / 2);
        canvas.drawArc(rect, -90f, 360 * progress / 100f, false, progressPaint);

        int radius = (getRadius()) * 2 / 3 - progressWidth / 2 - strokeWidth;

        canvas.drawCircle(width / 2, height / 2, radius, centerCirclePaint);

        if (!pause) {
            String s = progress + "%";
            progressTextPaint.setTextSize(progressTextSize * getRadius());
            float textLength = progressTextPaint.measureText(s);
            Paint.FontMetrics fontMetrics = progressTextPaint.getFontMetrics();
            float textHeight = Math.abs(fontMetrics.ascent);
            canvas.drawText(s, width / 2 - textLength / 2, height / 2 + textHeight / 2, progressTextPaint);
        }


        //画进度的外边框
        canvas.drawCircle(width / 2, height / 2, getRadius() * 2 / 3, strokePaint);

        //画暂停
        if (pause) {
            Path path = new Path();
            path.moveTo(width / 2 - radius / 3, height / 2 - radius * 4 / 7);// 此点为多边形的起点
            path.lineTo(width / 2 + radius / 2, height / 2);
            path.lineTo(width / 2 - radius / 3, height / 2 + radius * 4 / 7);
            path.close(); // 使这些点构成封闭的多边形
            canvas.drawPath(path, pauseButtonPaint);
        }


    }

    @NonNull
    private RectF getRectF(Canvas canvas) {

        if (bgRectF == null) {
            Rect rect1 = new Rect(4, 4, canvas.getWidth() - 4, canvas.getHeight() - 4);
            bgRectF = new RectF(rect1);
        }
        return bgRectF;
    }

    private RectF bgRectF;

    private int getRadius() {
        return width <= height ? width / 2 : height / 2;
    }

    private void loadViewSize(Canvas canvas) {
        if (width == 0) {
            width = canvas.getWidth();
        }
        if (height == 0) {
            height = canvas.getHeight();
        }
    }

    public void setProgress(int progress) {
        this.pause = false;
        if (progress < 0) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }
        this.progress = progress;
        postInvalidate();
    }

    public void setPause() {
        this.pause = true;
        this.install = false;
        postInvalidate();
    }


    public void setInstall() {
        this.install = true;
        this.pause = false;
        postInvalidate();
    }


    public void setDownload() {
        this.install = false;
        this.pause = false;
        postInvalidate();
    }
}
