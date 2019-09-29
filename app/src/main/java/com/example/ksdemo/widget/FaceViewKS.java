package com.example.ksdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import megvii.facepass.types.FacePassFace;
import megvii.facepass.types.FacePassRect;

/**
 * https://cloud.tencent.com/developer/article/1006277
 * https://blog.csdn.net/hgfygfc/article/details/83949088
 * Created by codemaster on 14/06/2019.
 */

public class FaceViewKS extends View {
    private static final double minDistance = 100;
    private Context mContext;
    private Paint mLinePaint;


    private int centerX = 1;
    private int centerY = 1;

    private float slantLength = 100;
    private float straightLength = 200;
    private float sin45 = 0.7071f;

    private float nameOffsetX = 60;
    private float nameOffsetY = 26;
    private String name;

    private FacePassFace lastFace;
    private FacePassFace mFace;

    public FaceViewKS(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initPaint();
        mContext = context;
    }


    public void setFaces(FacePassFace face){
        this.mFace = face;
        // invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mFace == null){
            invalidate();
            return;
        }

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        RectF mRect = null;
        if (lastFace == null){
            lastFace = mFace;
            mRect = new RectF(mFace.rect.left, mFace.rect.top, mFace.rect.right, mFace.rect.bottom);
        } else {
           // 判断前后两次矩形是不是移动超过一定范围，超过了再用新的绘制
            FacePassRect lastRect = lastFace.rect;
            FacePassRect curRect = mFace.rect;
            double d = Math.sqrt((lastRect.left - curRect.left) * (lastRect.left - curRect.left)
                    + (lastRect.top - curRect.top) * (lastRect.top - curRect.top));
            // 需要重新绘制了
            if(d >= minDistance){
                lastFace = mFace;
                mRect = new RectF(mFace.rect.left, mFace.rect.top, mFace.rect.right, mFace.rect.bottom);
            } else {
                mRect = new RectF(lastFace.rect.left, lastFace.rect.top, lastFace.rect.right, lastFace.rect.bottom);
            }
        }

        canvas.save();
        canvas.rotate(-0);   //Canvas.rotate()默认是逆时针

        // 绘制外圈的白色细线
        RectF outsideRect = new RectF(mRect.left - 60, mRect.top - 60, mRect.right + 60, mRect.bottom + 60);
        mLinePaint.setARGB(150, 255, 255, 255);
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(4);
        canvas.drawArc(outsideRect, 0 , 360, false, mLinePaint);

        // 如果识别出来了，显示名字
        // 先判断圆圈的中心点是在哪个象限
        if(!TextUtils.isEmpty(name)){
            float cx = (outsideRect.left + outsideRect.right) / 2;
            float cy = (outsideRect.top + outsideRect.bottom) / 2;

            PointF startP = new PointF();
            PointF secondP = new PointF();
            PointF thirdP = new PointF();
            PointF nameP = new PointF();

            float nameOffset = name.length() * nameOffsetX;

            if(cx >= centerX && cy <= centerY){
                // 第一象限
                startP.x = cx;
                startP.y = outsideRect.bottom;
                // 第二个点左转45°,长度设置成60
                secondP.x = startP.x - sin45 * slantLength;
                secondP.y = startP.y + sin45 * slantLength;
                // 第三个点，右直
                thirdP.x = secondP.x - straightLength;
                thirdP.y = secondP.y;

                nameP.x = thirdP.x - nameOffset;
                nameP.y = thirdP.y + nameOffsetY;

            } else if (cx < centerX && cy < centerY){
                // 第二象限
                startP.x = cx;
                startP.y = outsideRect.bottom;
                // 第二个点右转45°,长度设置成60
                secondP.x = startP.x + sin45 * slantLength;
                secondP.y = startP.y + sin45 * slantLength;
                // 第三个点，右直
                thirdP.x = secondP.x + straightLength;
                thirdP.y = secondP.y;

                nameP.x = thirdP.x;
                nameP.y = thirdP.y + nameOffsetY;

            } else if (cx < centerX && cy > centerY){
                // 第三象限
                startP.x = cx;
                startP.y = outsideRect.top;
                // 第二个点右转45°,长度设置成60
                secondP.x = startP.x + sin45 * slantLength;
                secondP.y = startP.y - sin45 * slantLength;
                // 第三个点，右直
                thirdP.x = secondP.x + straightLength;
                thirdP.y = secondP.y;

                nameP.x = thirdP.x;
                nameP.y = thirdP.y + nameOffsetY;
            } else {
                // 第四象限
                startP.x = cx;
                startP.y = outsideRect.top;
                // 第二个点左转45°,长度设置成60
                secondP.x = startP.x - sin45 * slantLength;
                secondP.y = startP.y - sin45 * slantLength;
                // 第三个点，左直
                thirdP.x = secondP.x - straightLength;
                thirdP.y = secondP.y;

                nameP.x = thirdP.x - nameOffset;
                nameP.y = thirdP.y + nameOffsetY;
            }
            // float[] pts = {startP.x, startP.y, secondP.x, secondP.y, thirdP.x, thirdP.y};
            // canvas.drawLines(pts, mLinePaint);
            canvas.drawLine(startP.x, startP.y, secondP.x, secondP.y, mLinePaint);
            canvas.drawLine(secondP.x, secondP.y, thirdP.x, thirdP.y, mLinePaint);

            mLinePaint.setTextSize(50);
            mLinePaint.setStyle(Style.FILL);
            mLinePaint.setARGB(255, 0, 255, 255);
            canvas.drawText(name, nameP.x, nameP.y, mLinePaint);
        }

        canvas.restore();
        super.onDraw(canvas);
        invalidate();
    }

    private void initPaint(){
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //		int color = Color.rgb(0, 150, 255);
        int color = Color.rgb(98, 212, 68);
        //		mLinePaint.setColor(Color.RED);
        mLinePaint.setColor(color);
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(5f);
        mLinePaint.setAlpha(180);
    }

    public void setName(String name) {
        this.name = name;
    }
}
