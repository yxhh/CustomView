package com.ruolian.yw.coustomview.chart;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.ruolian.yw.coustomview.chart.chartdata.CircleChartData;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ruolian.yw.utils.MathUtils.cos;
import static com.ruolian.yw.utils.MathUtils.sin;
import static com.ruolian.yw.utils.ScreenUtils.dp2px;
import static com.ruolian.yw.utils.ScreenUtils.sp2px;

/**
 * @author yangwang
 * @date 18-6-14 10:13
 * @company Beijing QiaoData Management Co.
 * @projectName code
 * @packageName com.yw.linechat.linechat
 */
public class CircleChart extends View {
    private List<CircleChartData> datas = new ArrayList<>();
    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint();
    private float mMarginLeft = dp2px(112), mMarginRight = dp2px(112);
    private float mMarginTop = dp2px(40), mMarginBottom = dp2px(40);

    public CircleChart(Context context) {
        super(context);
        init(context, null);
    }

    public CircleChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawCircle(canvas);


    }

    private void drawCircle(Canvas canvas) {
        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp2px(2));
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);

        //计算圆环的大小
        float left, top, right, bottom, radius;
        /*if (getWidth() >= getHeight()) {
            left = top = mMarginTop;
            right = bottom = getHeight() - mMarginTop;
            radius = (getHeight() - 2 * mMarginTop) / 2;
        } else {
            left = top = mMarginLeft;
            right = bottom = getWidth() - mMarginLeft;
            radius = (getWidth() - 2 * mMarginLeft) / 2;
        }*/
        left = mMarginLeft;
        top = mMarginTop;
        right = getWidth() - mMarginLeft;
        bottom = getWidth() - 2 * mMarginLeft + mMarginTop;
        radius = (getWidth() - 2 * mMarginLeft) / 2;
        RectF oval = new RectF(left, top, right, bottom);

        int sum = 0;
        for (int i = 0; i < datas.size(); i++) {
            sum += datas.get(i).dur;
        }
        float unit = 360f / sum;

        float startAngle = 0;
        float centerX = left + radius;
        float centerY = top + radius;
        canvas.drawCircle(centerX, centerY, dp2px(2), paint);
        Path textLinePath = new Path();
        for (int i = 0; i < datas.size(); i++) {
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp2px(2));
            paint.setColor(Color.parseColor(datas.get(i).color));
            if (i == 0) {
                startAngle += -90;
            } else {
                startAngle += datas.get(i - 1).dur * unit;
            }
            //画弧线
            canvas.drawArc(oval, startAngle, datas.get(i).dur * unit, false, paint);
            float smallCenterX = 0, smallCenterY = 0;
            float textX = 0, textY = 0;
            int textWidth = 0;
            float midAngle = startAngle + datas.get(i).dur * unit / 2;
            if (midAngle > -90 && midAngle <= 0) {
                float dy = sin(Math.abs(midAngle)) * radius;
                float dx = cos(Math.abs(midAngle)) * radius;
                smallCenterX = centerX + dx;
                smallCenterY = centerY - dy;
                textX = smallCenterX + dp2px(30);
                textY = smallCenterY - dp2px(30);
                textWidth = (int) (getWidth() - smallCenterX - dp2px(30));
            } else if (midAngle > 0 && midAngle <= 90) {
                float dy = sin(Math.abs(midAngle)) * radius;
                float dx = cos(Math.abs(midAngle)) * radius;
                smallCenterX = centerX + dx;
                smallCenterY = centerY + dy;
                textX = smallCenterX + dp2px(30);
                textY = smallCenterY + dp2px(10);
                textWidth = (int) (getWidth() - smallCenterX - dp2px(30));
            } else if (midAngle > 90 && midAngle <= 180) {
                float dy = sin(Math.abs(180 - midAngle)) * radius;
                float dx = cos(Math.abs(180 - midAngle)) * radius;
                smallCenterX = centerX - dx;
                smallCenterY = centerY + dy;
                textX = smallCenterX - dp2px(30);
                textY = smallCenterY + dp2px(10);
                textWidth = (int) textX;
            } else if (midAngle > 180 && midAngle <= 270) {
                float dy = sin(Math.abs(midAngle - 180)) * radius;
                float dx = cos(Math.abs(midAngle - 180)) * radius;
                smallCenterX = centerX - dx;
                smallCenterY = centerY - dy;
                textX = smallCenterX - dp2px(30);
                textY = smallCenterY - dp2px(30);
                textWidth = (int) textX;
            }
            if (datas.get(i).dur >= 15) {
                //文字的引线=====1
                paint.setStrokeWidth(dp2px((int) 0.5f));
                textLinePath.reset();
                textLinePath.moveTo(smallCenterX, smallCenterY);
                textLinePath.lineTo(textX, textY);
                //画文字
                textPaint.setTextSize(sp2px(10));
                textPaint.setStyle(Paint.Style.FILL);
                textPaint.setColor(Color.BLUE);
                //处理中英文数字混排列，换行问题
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < datas.get(i).des.length(); j++) {
                    sb.append(datas.get(i).des.charAt(j));
                    if (textPaint.measureText(sb.toString()) >= textWidth) {
                        sb = new StringBuilder();
                        StringBuilder stringBuilder = new StringBuilder(datas.get(i).des);
                        stringBuilder.insert(j - 1, "\n");
                        datas.get(i).des = stringBuilder.toString();
                    }
                }
                StaticLayout layout = new StaticLayout(datas.get(i).des, textPaint,
                        textWidth, Layout.Alignment.ALIGN_NORMAL,
                        1f, 0f, false);
                canvas.save();
                //文字的引线=====2
                if (midAngle < 90) {
                    canvas.translate(textX, textY);
                    textLinePath.lineTo(textX + dp2px(20), textY);
                } else {
                    textLinePath.lineTo(textX - dp2px(20), textY);
                    //再圆环的左侧的时候需要判断文字的长度，否则回出现文字再左侧，引线距离文字很远的情况
                    if (textPaint.measureText(datas.get(i).des) < textWidth) {
                        canvas.translate(textWidth - textPaint.measureText(datas.get(i).des), textY);
                    } else {
                        canvas.translate(0, textY);
                    }
                }
                layout.draw(canvas);
                canvas.restore();
                //文字的引线=====3
                canvas.drawPath(textLinePath, paint);
            }
            //画弧线中点的圆圈
            //继续画小圆圈
            paint.setStrokeWidth(dp2px(2));
            canvas.drawCircle(smallCenterX, smallCenterY, dp2px(4), paint);
            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(smallCenterX, smallCenterY, dp2px(3), paint);


        }
    }

    public void setDatas(List<CircleChartData> list) {
        this.datas = list;
        invalidate();
    }
}
