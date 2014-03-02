package com.dztech.app.ruler;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class RulerView extends View {

	private static final String TAG = "RulerView";
	
	private Context mContext;
	private int mWidth;
	private int mHeight;
	private boolean mIsHoldLeftCursor = true;
	private Bitmap mCursorImg;
	private Bitmap mBackground;
	private Paint mImgPaint; 	// 画图滤波画笔

	// View属性定义
	private int attrCmResultColor;
	private int attrInchResultColor;

	// 属性
	private float leftCursor;
	private float rightCursor;
	private float ydpi;

	public float getLeftCursor() {
		return leftCursor;
	}

	public void setLeftCursor(float leftCursor) {
		this.leftCursor = leftCursor;
	}

	public float getRightCursor() {
		return rightCursor;
	}

	public void setRightCursor(float rightCursor) {
		this.rightCursor = rightCursor;
	}

	public void setYdpi(float ydpi) {
		this.ydpi = ydpi;
	}

	public float getYdpi() {
		return ydpi;
	}

	public RulerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		getAttrs(mContext, attrs);
		initView();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (changed) {
			mWidth = right - left;
			mHeight = bottom - top;
			Log.i(TAG, "宽度:" + mWidth + "高度:" + mHeight);
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// 绘制背景
		drawBackground(canvas);
		// 绘制指示线
		drawCursor(canvas, getLeftCursor());
		drawCursor(canvas, getRightCursor());
		// 绘制刻度线
		drawMark(canvas);
		// 绘制测量结果(cm/inch)
		drawResult(canvas);

		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getPointerCount() == 1) {
			moveCursor(event);
			invalidate();
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.rulerViewAttr);
		attrCmResultColor = ta.getColor(
				R.styleable.rulerViewAttr_cmResultColor, 0xFFFF0000);
		attrInchResultColor = ta.getColor(
				R.styleable.rulerViewAttr_inchResultColor, 0xFFFF0000);
		ta.recycle();
	}

	/**
	 * 初始化View
	 */
	private void initView() {
		// 解析图片
		Resources res = mContext.getResources();
		mCursorImg = BitmapFactory.decodeResource(res, R.drawable.ruler_cursor);
		mBackground = BitmapFactory.decodeResource(res,
				R.drawable.tools_ruler_bg_ruler);

		// 设置画笔
		mImgPaint = new Paint();
		mImgPaint.setAntiAlias(true);
		mImgPaint.setFilterBitmap(true);

		// 初始化Cursor位置
		setLeftCursor(mCursorImg.getHeight() / 2);
		setRightCursor(mCursorImg.getHeight() * 2);
	}

	/**
	 * 移动游标
	 * 
	 * @param event
	 */
	private void moveCursor(MotionEvent event) {
		if ((event.getX() > mWidth / 2 - mCursorImg.getHeight() * 2)
				&& (event.getX() < mWidth / 2 + mCursorImg.getHeight() * 2)) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (event.getY() < (getLeftCursor() + getRightCursor()) / 2) {
					mIsHoldLeftCursor = true;
				} else {
					mIsHoldLeftCursor = false;
				}
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				// 选择移动的游标
				if (mIsHoldLeftCursor) {
					setLeftCursor(event.getY());
				} else {
					setRightCursor(event.getY());
				}

				if (getLeftCursor() > getRightCursor()) {
					float tempCursor = 0;
					tempCursor = getLeftCursor();
					setLeftCursor(getRightCursor());
					setRightCursor(tempCursor);
					mIsHoldLeftCursor = !mIsHoldLeftCursor;
				}

				if (getLeftCursor() < mCursorImg.getHeight() / 2) {
					setLeftCursor(mCursorImg.getHeight() / 2);
				} else if (getRightCursor() > mHeight - mCursorImg.getHeight()
						/ 2) {
					setRightCursor(mHeight - mCursorImg.getHeight() / 2);
				}
			}
		}
	}

	/**
	 * 绘制刻度尺背景
	 * 
	 * @param canvas
	 */
	private void drawBackground(Canvas canvas) {
		RectF bgArea = new RectF(50, 0, mWidth - 50, mHeight);
		canvas.drawBitmap(mBackground, null, bgArea, mImgPaint);
	}

	/**
	 * 绘制刻度尺刻度
	 * 
	 * @param canvas
	 */
	private void drawMark(Canvas canvas) {
		Paint markPaint = new Paint(); // 刻度线画笔
		markPaint.setAntiAlias(true);
		markPaint.setFilterBitmap(true);
		markPaint.setColor(0xAAAAAAAA);
		markPaint.setStyle(Paint.Style.STROKE);
		markPaint.setStrokeWidth(3);

		Paint markValuePaint = new Paint(); // 刻度值画笔

		markValuePaint.setAntiAlias(true);
		markValuePaint.setFilterBitmap(true);
		markValuePaint.setTextSize(30);
		markValuePaint.setColor(0x88888888);
		markValuePaint.setTypeface(Typeface.DEFAULT);

		/* 绘制cm刻度 */
		float pixPerFifthCm = (float) (getYdpi() / (2.54 * 5));
		float markStartX = mWidth - 70;
		float markStartY = mCursorImg.getHeight() / 2;
		int markLength = 30;

		for (int fifth_cm = 0; fifth_cm < mHeight / pixPerFifthCm; fifth_cm++) {
			if (fifth_cm % 5 == 0) {
				markLength = 30;
				markPaint.setStrokeWidth(4);
			} else {
				markLength = 20;
				markPaint.setStrokeWidth(2);
			}
			canvas.drawLine(markStartX, markStartY + fifth_cm * pixPerFifthCm,
					markStartX - markLength, markStartY + fifth_cm
							* pixPerFifthCm, markPaint);
		}

		// 绘制cm刻度值
		for (int cm = 0; cm < mHeight / (pixPerFifthCm * 5); cm++) {
			float textStartX = mWidth - 130;
			float textStartY = mCursorImg.getHeight() / 2;
			Path textpath = new Path();
			textpath.reset();
			textpath.moveTo(textStartX, textStartY + cm * pixPerFifthCm * 5);
			textpath.lineTo(textStartX, textStartY + (cm + 1) * pixPerFifthCm
					* 5);
			canvas.drawTextOnPath(Integer.toString(cm), textpath, 0, 0,
					markValuePaint);
		}

		/* 绘制inch刻度 */
		float pixPerInch = getYdpi();
		markStartX = 70;

		for (int tenth_inch = 0; tenth_inch < mHeight * 10 / pixPerInch; tenth_inch++) {
			if (tenth_inch % 10 == 0) {
				markLength = 30;
				markPaint.setStrokeWidth(4);
			} else if (tenth_inch % 2 == 0) {
				markLength = 20;
				markPaint.setStrokeWidth(2);
			} else {
				markLength = 8;
				markPaint.setStrokeWidth(1);
			}

			canvas.drawLine(markStartX, markStartY + tenth_inch * pixPerInch
					/ 10, markStartX + markLength, markStartY + tenth_inch
					* pixPerInch / 10, markPaint);
		}

		// 绘制inch刻度值
		for (int inch = 0; inch < mHeight / pixPerInch; inch++) {
			float textStartX = 110;
			float textStartY = mCursorImg.getHeight() / 2;
			Path textpath = new Path();
			textpath.reset();
			textpath.moveTo(textStartX, textStartY + inch * pixPerInch);
			textpath.lineTo(textStartX, textStartY + (inch + 1) * pixPerInch);
			canvas.drawTextOnPath(Integer.toString(inch), textpath, 0, 0,
					markValuePaint);
		}
	}

	/**
	 * 绘制测量游标
	 * 
	 * @param canvas
	 * @param pos
	 */
	private void drawCursor(Canvas canvas, float pos) {
		Matrix cursorMatrix = new Matrix();
		cursorMatrix.postTranslate(mWidth / 2 - mCursorImg.getWidth() / 2, pos
				- mCursorImg.getHeight() / 2);
		canvas.drawBitmap(mCursorImg, cursorMatrix, mImgPaint);
	}

	/**
	 * 绘制测量结果(cm/inch)
	 * 
	 * @param canvas
	 */
	private void drawResult(Canvas canvas) {
		Path resultPath = new Path();
		Paint resultPaint = new Paint();

		resultPaint.setAntiAlias(true);
		resultPaint.setFilterBitmap(true);
		resultPaint.setTextSize(30);
		resultPaint.setTypeface(Typeface.DEFAULT_BOLD);

		/* cm */
		resultPath.moveTo(mWidth - 40, mHeight / 2 - 40);
		resultPath.lineTo(mWidth - 40, mHeight);
		resultPaint.setColor(attrCmResultColor);

		float cmResult = (float) ((getRightCursor() - getLeftCursor()) / (getYdpi() / 2.54));
		String cmResultStr = new DecimalFormat("##0.00").format(cmResult)
				+ "  cm ";
		canvas.drawTextOnPath(cmResultStr, resultPath, 0, 0, resultPaint);

		/* inch */
		resultPath.reset();
		resultPath.moveTo(10, mHeight / 2 - 40);
		resultPath.lineTo(10, mHeight);
		resultPaint.setColor(attrInchResultColor);

		float inchResult = (getRightCursor() - getLeftCursor()) / getYdpi();
		String inchResultStr = new DecimalFormat("##0.000").format(inchResult)
				+ " inch";
		canvas.drawTextOnPath(inchResultStr, resultPath, 0, 0, resultPaint);
	}

}
