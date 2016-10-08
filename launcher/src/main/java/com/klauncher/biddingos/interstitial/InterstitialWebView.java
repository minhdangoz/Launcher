package com.klauncher.biddingos.interstitial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;

import com.alpsdroid.ads.banner.AdSize;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.web.BaseWebView;


@SuppressLint("SetJavaScriptEnabled")
public class InterstitialWebView extends BaseWebView {
	private static final String TAG = "InterstitialWebView";
	private InterstitialWebInterface interstitialWebInterface = null;
	protected int  showState=NOTSHOW;
	protected static final int NOTSHOW=0;
	protected static final int ISSHOWING=1;
	protected static final int HASSHOWED=2;
	protected static final int AD_TYPE_HALF_SCREEN=3;
	protected static final int AD_TYPE_FULL_SCREEN=4;
	protected boolean isAdload=false;
	protected int img_with = -1;
	protected int img_height = -1;
	protected int adType = -1;
	protected String logUrl;
	protected String st;
	protected String transactionid;
	protected boolean notifyImpression=false;

	private Paint paint1;
	private Paint paint2;
	private float m_radius;
	private int width;
	private int height;
	private int x;
	private int y;

	public InterstitialWebView(Context context, AdSize adSize) {
		super(context, adSize);
		// TODO Auto-generated constructor stub
	}

	public void loadAd() {
		this.loadUrl(getInterstitialUrl(mContext, placeMentId));
	}

	private String getInterstitialUrl(Context mContext, int placeMentId) {
		return getUrl(Setting.getBiddingOS_INTERSTITIAIL_URL(getAdSuitableWidth(), adSize.getHeight(), nDpi),mContext,placeMentId);
	}

	public boolean init(int placeMentId, final Handler mHandler){
		super.init(placeMentId, mHandler);
		loadAd();
        interstitialWebInterface = new InterstitialWebInterface(mContext, this, placeMentId, mHandler);
		this.addJavascriptInterface(interstitialWebInterface, "BosAd");
		paint1 = new Paint();
		paint1.setAntiAlias(true);
		//取下层绘制非交集部分
		paint1.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

		paint2 = new Paint();
		paint2.setXfermode(null);
        return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogUtils.i(TAG, "onMeasure" + " img_with=" + img_with + " img_height=" + img_height);
		int nWidth=-1;
		int nHeight=-1;
		if(adType==AD_TYPE_HALF_SCREEN) {
			nWidth = img_with == -1 ? (getAdSuitableWidth() ) : img_with;
			nHeight = img_height == -1 ? (adSize.getHeight()) : img_height;
		}else if(adType==AD_TYPE_FULL_SCREEN){
			nWidth=getScreenPixWidth();
			nHeight=getScreenPixHeight();
		}
			setMeasuredDimension(nWidth, nHeight);

	}


	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if(adType==AD_TYPE_HALF_SCREEN&&img_with>0&&img_height>0) {
			width=img_with;
			height=img_height;
			m_radius=10*nDpi/160;
			x = this.getScrollX();
			y = this.getScrollY();
			LogUtils.i(TAG,"x="+x+" y="+y);
			Bitmap bitmap = Bitmap.createBitmap(x + width, y + height,
					Bitmap.Config.ARGB_8888);
			drawLeftUp(canvas);
			drawRightUp(canvas);
			drawLeftDown(canvas);
			drawRightDown(canvas);
			canvas.drawBitmap(bitmap, 0, 0, paint2);
			//释放bitmap
			bitmap.recycle();
		}
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
	}
	private void drawLeftUp(Canvas canvas) {
		Path path = new Path();
		path.moveTo(x, m_radius);
		path.lineTo(x, y);
		path.lineTo(m_radius, y);
		path.arcTo(new RectF(x, y, x + m_radius * 2, y + m_radius * 2), -90, -90);
		path.close();
		canvas.drawPath(path, paint1);
	}


	private void drawLeftDown(Canvas canvas) {
		Path path = new Path();
		path.moveTo(x, y + height - m_radius);
		path.lineTo(x, y + height);
		path.lineTo(x + m_radius, y + height);
		path.arcTo(new RectF(x, y + height - m_radius * 2,
				x + m_radius * 2, y + height), 90, 90);
		path.close();
		canvas.drawPath(path, paint1);
	}


	private void drawRightDown(Canvas canvas) {
		Path path = new Path();
		path.moveTo(x + width - m_radius, y + height);
		path.lineTo(x + width, y + height);
		path.lineTo(x + width, y + height - m_radius);
		path.arcTo(new RectF(x + width - m_radius * 2, y + height
				- m_radius * 2, x + width, y + height), 0, 90);
		path.close();
		canvas.drawPath(path, paint1);
	}


	private void drawRightUp(Canvas canvas) {
		Path path = new Path();
		path.moveTo(x + width, y + m_radius);
		path.lineTo(x + width, y);
		path.lineTo(x + width - m_radius, y);
		path.arcTo(new RectF(x + width - m_radius * 2, y, x + width,
				y + m_radius * 2), -90, 90);
		path.close();
		canvas.drawPath(path, paint1);
	}
}
