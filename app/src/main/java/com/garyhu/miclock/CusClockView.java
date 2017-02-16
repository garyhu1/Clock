package com.garyhu.miclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

/**
 * 作者： garyhu.
 * 时间： 2017/2/16.
 */

public class CusClockView extends View {

    private Rect mTextRect;//文本的矩形框
    private Paint mTextPaint;//文本的画笔
    private Paint mArcPaint;//圆弧的画笔
    /* 时钟半径，不包括padding值 */
    private float mRadius;

    /* 小时圆圈线条宽度 */
    private float mCircleStrokeWidth = 2;

    /* 刻度线长度 */
    private float mScaleLength;

    /* 梯度扫描渐变 */
    private SweepGradient mSweepGradient;
    /* 渐变矩阵，作用在SweepGradient */
    private Matrix mGradientMatrix;
    /* 指针的在x轴的位移 */
    private float mCanvasTranslateX;
    /* 指针的在y轴的位移 */
    private float mCanvasTranslateY;

    /* 秒针角度 */
    private float mSecondDegree;
    /* 秒针路径 */
    private Path mSecondHandPath;
    /* 秒针画笔 */
    private Paint mSecondHandPaint;

    /* 时针角度 */
    private float mHourDegree;
    /* 分针角度 */
    private float mMinuteDegree;

    /* 时针路径 */
    private Path mHourHandPath;
    /* 分针路径 */
    private Path mMinuteHandPath;
    /* 时针画笔 */
    private Paint mHourHandPaint;
    /* 分针画笔 */
    private Paint mMinuteHandPaint;

    /* 小时圆圈的外接矩形 */
    private RectF mCircleRectF;


    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;

    /* 刻度圆弧画笔 */
    private Paint mScaleArcPaint;
    /* 刻度圆弧的外接矩形 */
    private RectF mScaleArcRectF;
    /* 刻度线画笔 */
    private Paint mScaleLinePaint;

    private int hour;
    private int minute;
    private int second;
    private Context context;


    public CusClockView(Context context) {
        this(context,null);
    }

    public CusClockView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CusClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //宽和高分别去掉padding值，取min的一半即表盘的半径
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom()) / 2;
        mDefaultPadding = 0.12f * mRadius;//根据比例确定默认padding大小
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + getPaddingLeft()+50;
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + getPaddingTop()+50;
        mPaddingRight = mPaddingLeft;
        mPaddingBottom = mPaddingTop;
        mScaleLength = 0.12f * mRadius;//根据比例确定刻度线长度
        mScaleArcPaint.setStrokeWidth(0.5f*mScaleLength);
        mScaleLinePaint.setStrokeWidth(0.005f * mRadius);
//        mMaxCanvasTranslate = 0.02f * mRadius;
//        //梯度扫描渐变，以(w/2,h/2)为中心点，两种起止颜色梯度渐变
//        //float数组表示，[0,0.75)为起始颜色所占比例，[0.75,1}为起止颜色渐变所占比例
        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[]{0x80ffffff, 0xffffffff}, new float[]{0.75f, 1});
    }

    public int measureSize(int m){
        int result;
        int mode = MeasureSpec.getMode(m);
        int size = MeasureSpec.getSize(m);
        if(mode == MeasureSpec.EXACTLY){
            result = size;
        }else {
            result = 800;
            if(mode == MeasureSpec.AT_MOST){
                result = Math.min(800,size);
            }
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureSize(widthMeasureSpec),measureSize(heightMeasureSpec));
    }

    public void init(Context context,AttributeSet attrs){
        if(attrs==null){
            return;
        }
        this.context = context;

        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mCircleStrokeWidth);
        mArcPaint.setColor(0x80ffffff);

        mTextRect = new Rect();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(DensityUtils.sp2px(context,10));
        mTextPaint.setColor(0x80ffffff);

        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(0x80ffffff);

        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(0x80ffffff);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(0x80ffffff);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setColor(0xffffffff);

        mScaleArcRectF = new RectF();
        mGradientMatrix = new Matrix();

        mSecondHandPath = new Path();
        mHourHandPath = new Path();
        mMinuteHandPath = new Path();

        mCircleRectF = new RectF();

    }

    /**
     * 获取当前时分秒所对应的角度
     * 为了不让秒针走得像老式挂钟一样僵硬，需要精确到毫秒
     */
    private void getTimeDegree() {
        Calendar calendar = Calendar.getInstance();
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float minute = calendar.get(Calendar.MINUTE) + second / 60;
        float hour = calendar.get(Calendar.HOUR) + minute / 60;
        mSecondDegree = second / 60 * 360;
        mMinuteDegree = minute / 60 * 360;
        mHourDegree = hour / 12 * 360;
    }

    public void getTime(){
        Calendar cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR);
        minute = cal.get(Calendar.MINUTE);
        second = cal.get(Calendar.SECOND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getTime();
        getTimeDegree();
        drawArc(canvas);
        drawLine(canvas);
        drawSecondHand(canvas);
        drawTime(canvas);
        drawHourHandler(canvas);
        drawMinuteHandler(canvas);
        invalidate();

    }

    public void drawArc(Canvas canvas){
        String timeText = "12";
        mTextPaint.getTextBounds(timeText, 0, timeText.length(), mTextRect);
        int textLargeWidth = mTextRect.width();//两位数字的宽
        canvas.drawText("12", getWidth() / 2 - textLargeWidth / 2, mPaddingTop + mTextRect.height(), mTextPaint);
        timeText = "3";
        mTextPaint.getTextBounds(timeText, 0, timeText.length(), mTextRect);
        int textSmallWidth = mTextRect.width();//一位数字的宽
        canvas.drawText("3", getWidth() - mPaddingRight - mTextRect.height() / 2 - textSmallWidth / 2,
                getHeight() / 2 + mTextRect.height() / 2, mTextPaint);
        canvas.drawText("6", getWidth() / 2 - textSmallWidth / 2, getHeight() - mPaddingBottom, mTextPaint);
        canvas.drawText("9", mPaddingLeft + mTextRect.height() / 2 - textSmallWidth / 2,
                getHeight() / 2 + mTextRect.height() / 2, mTextPaint);

        RectF rectF = new RectF();
        rectF.set(mPaddingLeft + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                mPaddingTop + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                getWidth() - mPaddingRight - mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                getHeight() - mPaddingBottom - mTextRect.height() / 2 + mCircleStrokeWidth / 2);
        for (int i = 0; i < 4; i++) {
            canvas.drawArc(rectF,5+90*i,80,false,mArcPaint);
        }
    }

    public void drawLine(Canvas canvas){
        canvas.translate(mCanvasTranslateX, mCanvasTranslateY);
        mScaleArcRectF.set(mPaddingLeft + 1.25f * mScaleLength + mTextRect.height() / 2,
                mPaddingTop + 1.25f * mScaleLength + mTextRect.height() / 2,
                getWidth() - mPaddingRight - mTextRect.height() / 2 - 1.25f * mScaleLength,
                getHeight() - mPaddingBottom - mTextRect.height() / 2 - 1.25f * mScaleLength);
        //matrix默认会在三点钟方向开始颜色的渐变，为了吻合钟表十二点钟顺时针旋转的方向，把秒针旋转的角度减去90度
        mGradientMatrix.setRotate(mSecondDegree - 90, getWidth() / 2, getHeight() / 2);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        mScaleArcPaint.setShader(mSweepGradient);
        canvas.drawArc(mScaleArcRectF, 0, 360, false, mScaleArcPaint);

        for (int i = 0; i < 120; i++) {
            if(i%10==0){
                canvas.drawLine(getWidth()/2,mPaddingTop+1.04f*mScaleLength+mTextRect.height()/2,
                        getWidth()/2,mPaddingTop+1.8f*mScaleLength+mTextRect.height()/2,mScaleLinePaint);
            }else {
                canvas.drawLine(getWidth()/2,mPaddingTop+1.04f*mScaleLength+mTextRect.height()/2,
                        getWidth()/2,mPaddingTop+1.5f*mScaleLength+mTextRect.height()/2,mScaleLinePaint);
            }
            canvas.rotate(3f,getWidth()/2,getHeight()/2);
        }
    }

    /**
     * 画秒针，根据不断变化的秒针角度旋转画布
     */
    private void drawSecondHand(Canvas mCanvas) {
        mCanvas.save();
        mCanvas.rotate(mSecondDegree, getWidth() / 2, getHeight() / 2);
        mSecondHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        mSecondHandPath.moveTo(getWidth() / 2, offset + 0.26f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2 - 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2 + 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.close();
        mSecondHandPaint.setColor(0x80ffffff);
        mCanvas.drawPath(mSecondHandPath, mSecondHandPaint);
        mCanvas.restore();
    }

    public void drawTime(Canvas canvas){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(DensityUtils.sp2px(context,16));
        paint.setColor(0xffffffff);
        String text1,text2,text3;
        if(hour<10){
            text1 = "0"+hour;
        }else {
            text1 = hour+"";
        }
        if(minute<10){
            text2 = "0"+minute;
        }else {
            text2 = ""+minute;
        }
        if(second<10){
            text3 = "0"+second;
        }else {
            text3 = ""+second;
        }
        String text = text1+":"+text2+":"+text3;
        paint.getTextBounds(text,0,text.length(),mTextRect);
        canvas.drawText(text,getWidth()/2-mTextRect.width()/2,getHeight() - mPaddingBottom+50,paint);
    }

    public void drawMinuteHandler(Canvas mCanvas){
        mCanvas.save();
//        mCanvas.translate(mCanvasTranslateX * 2f, mCanvasTranslateY * 2f);
        mCanvas.rotate(mMinuteDegree, getWidth() / 2, getHeight() / 2);
        mMinuteHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        mMinuteHandPath.moveTo(getWidth() / 2 - 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 - 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.quadTo(getWidth() / 2, offset + 0.345f * mRadius,
                getWidth() / 2 + 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 + 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.close();
        mMinuteHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mMinuteHandPath, mMinuteHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(0.02f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mMinuteHandPaint);
        mCanvas.restore();
    }

    public void drawHourHandler(Canvas mCanvas){
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX * 1.2f, mCanvasTranslateY * 1.2f);
        mCanvas.rotate(mHourDegree, getWidth() / 2, getHeight() / 2);
        mHourHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        mHourHandPath.moveTo(getWidth() / 2 - 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 - 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.quadTo(getWidth() / 2, offset + 0.46f * mRadius,
                getWidth() / 2 + 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 + 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.close();
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mHourHandPath, mHourHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeWidth(0.01f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mHourHandPaint);
        mCanvas.restore();
    }
}
