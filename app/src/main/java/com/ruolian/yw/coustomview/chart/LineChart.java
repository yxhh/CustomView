package com.ruolian.yw.coustomview.chart;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ruolian.yw.R;
import com.ruolian.yw.coustomview.chart.chartdata.LineChartData;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ruolian.yw.utils.ScreenUtils.dp2px;
import static com.ruolian.yw.utils.ScreenUtils.sp2px;

/**
 * @author yangwang
 * @date 18-6-1 16:14
 * @company Beijing QiaoData Management Co.
 * @projectName code
 * @packageName com.yw.linechat.linechat
 */
public class LineChart extends View {
    float CTRL_VALUE_A = 0.2f, CTRL_VALUE_B = 0.2f;//滑度系数
    private Paint linePaint, textPaint;
    private List<LineChartData> list = new ArrayList<>();//原始数据
    private List<PointF> fList = new ArrayList<>();//存储转换为xy轴坐标的数据
    private float mScreenWidth, mScreenHeight, mMarginLeft = dp2px(10), mMarginRight = dp2px(10);
    private float mMarginTop = dp2px(20), mMarginBottom = dp2px(20);
    private float mOffset = dp2px(10);//偏移量
    private float singHeight, mMinSalary = 0, mMaxSalary = 0;
    private float radius = dp2px(3);//圆圈半径
    private int midLineColor = Color.parseColor("#F1F1F1");//中间线的色值
    private int lineColor, textColor;


    private int moveFlag;
    private OnTouchChartListener touchListener;

    public LineChart(Context context) {
        super(context);
        init(context, null, -1);
    }

    public LineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public LineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.RED);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp2px(1));

        textPaint = new Paint();
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineChart);
            lineColor = typedArray.getColor(R.styleable.LineChart_lineColor, Color.parseColor("#3AA7FF"));
            textColor = typedArray.getColor(R.styleable.LineChart_textColor, Color.parseColor("#ee0000"));

            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mScreenWidth = getMeasuredWidth();
        mScreenHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLineFrame(canvas);

        drawLine(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchListener != null) {
            float downX, downY;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moveFlag = -1;
                    downX = event.getX();
                    downY = event.getY();
                    if (fList != null && fList.size() > 0) {
                        for (int i = 1; i < fList.size() - 2; i++) {
                            PointF pointF = fList.get(i);
                            if (Math.abs(downX - pointF.x) < dp2px(10) && Math.abs(downY - pointF.y) < dp2px(10)) {
                                moveFlag = i;
//                                Log.d("Tag", String.valueOf(moveFlag));
                                continue;
                            }
                        }

                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (moveFlag != -1) {
                        // TODO: 18-6-8 是否限制最小最大值
                        fList.get(moveFlag).y = event.getY();
                        LineChartData lineChartData = list.get(moveFlag - 1);
                        if (lineChartData.salary == mMinSalary) {
                            lineChartData.salary = (int) ((mScreenHeight - mMarginBottom - event.getY() + mOffset) * singHeight + mMinSalary);
                        } else if (lineChartData.salary == mMaxSalary) {
                            lineChartData.salary = (int) ((mScreenHeight - mMarginBottom - event.getY() - mOffset) * singHeight + mMinSalary);
                        } else {
                            lineChartData.salary = (int) ((mScreenHeight - mMarginBottom - event.getY()) * singHeight + mMinSalary);
                        }
                        setLineData(list);
//                        Log.d("Tag", String.valueOf(event.getY()));
                        touchListener.onTouch(event);
//                        invalidate();
                    }
                    break;
                default:
                    touchListener.onTouch(event);
                    break;
            }
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * 画折线
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas) {
        if (list != null && list.size() > 0) {
            Path path = new Path();
            //画折线
            path.moveTo(fList.get(1).x, fList.get(1).y);
            for (int i = 1; i < fList.size() - 3; i++) {
                PointF ctrlPointA = new PointF();
                PointF ctrlPointB = new PointF();
                getCtrlPoint(fList, i, ctrlPointA, ctrlPointB);
                path.cubicTo(ctrlPointA.x, ctrlPointA.y, ctrlPointB.x, ctrlPointB.y, fList.get(i + 1).x, fList.get(i + 1).y);
            }
            linePaint.reset();
            linePaint.setAntiAlias(true);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(dp2px(2));
            linePaint.setColor(lineColor);
            canvas.drawPath(path, linePaint);
            //画折线图的下方的阴影
            linePaint.reset();
            linePaint.setShader(new LinearGradient(fList.get(fList.size() - 2).x, fList.get(fList.size() - 2).y,
                    fList.get(fList.size() - 2).x, mScreenHeight,
                    Color.parseColor("#503aa7ff"), Color.TRANSPARENT, Shader.TileMode.CLAMP));

            path.lineTo(fList.get(fList.size() - 2).x, mScreenHeight - mMarginBottom);
            path.lineTo(fList.get(0).x, mScreenHeight - mMarginBottom);
            path.close();
            canvas.drawPath(path, linePaint);


            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.message);
            //中间的竖线画笔
            linePaint.reset();
            linePaint.setColor(midLineColor);
            linePaint.setAntiAlias(true);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(dp2px(1));

            for (int i = 0; i < list.size(); i++) {
                LineChartData lineChartData = list.get(i);
                float x = fList.get(i + 1).x, y = fList.get(i + 1).y;
                //画对应的竖线
                canvas.drawLine(x, 0, x, mScreenHeight - mMarginBottom + 0.2f * mOffset, linePaint);
                //画圆圈
                textPaint.reset();
                textPaint.setAntiAlias(true);
                textPaint.setStyle(Paint.Style.STROKE);
                textPaint.setStrokeWidth(dp2px(2));
                textPaint.setColor(lineColor);
                canvas.drawCircle(x, y, radius, textPaint);
                textPaint.reset();
                textPaint.setAntiAlias(true);
                textPaint.setColor(Color.WHITE);
                canvas.drawCircle(x, y, radius * 0.7f, textPaint);

                //画文字背景
                canvas.drawBitmap(bitmap, null, new Rect((int) (x - dp2px(15)),
                        (int) (y - radius - dp2px(25)),
                        (int) (x + dp2px(15)),
                        (int) (y - radius)), textPaint);
                //画文字
                textPaint.setColor(textColor);
                textPaint.setTextSize(sp2px(10));
                if (lineChartData.salary / 1000 < 10) {
                    canvas.drawText(lineChartData.salary / 1000 + "K", x - dp2px(6), y - dp2px(13), textPaint);
                } else {
                    canvas.drawText(lineChartData.salary / 1000 + "K", x - dp2px(9), y - dp2px(13), textPaint);
                }
            }
        }
    }

    private void getCtrlPoint(List<PointF> pointFList, int currentIndex,
                              PointF ctrlPointA, PointF ctrlPointB) {
        ctrlPointA.x = pointFList.get(currentIndex).x +
                (pointFList.get(currentIndex + 1).x - pointFList.get(currentIndex - 1).x) * CTRL_VALUE_A;
        ctrlPointA.y = pointFList.get(currentIndex).y +
                (pointFList.get(currentIndex + 1).y - pointFList.get(currentIndex - 1).y) * CTRL_VALUE_A;
        ctrlPointB.x = pointFList.get(currentIndex + 1).x -
                (pointFList.get(currentIndex + 2).x - pointFList.get(currentIndex).x) * CTRL_VALUE_B;
        ctrlPointB.y = pointFList.get(currentIndex + 1).y -
                (pointFList.get(currentIndex + 2).y - pointFList.get(currentIndex).y) * CTRL_VALUE_B;
    }

    private void drawLineFrame(Canvas canvas) {
        linePaint.reset();
        linePaint.setColor(lineColor);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp2px(1));
        linePaint.setPathEffect(new DashPathEffect(new float[]{dp2px(2), dp2px(2)}, 0));
        //边框---横线
        float y = mScreenHeight - mMarginBottom;
        Path path = new Path();
        path.moveTo(0, y);
        path.lineTo(mScreenWidth, y);
        canvas.drawPath(path, linePaint);
        //中间的横线
        linePaint.reset();
        linePaint.setColor(midLineColor);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp2px(1));
        canvas.drawLine(0, y * 2 / 3, mScreenWidth, y * 2 / 3, linePaint);
        canvas.drawLine(0, y * 1 / 3, mScreenWidth, y * 1 / 3, linePaint);
        //画年份
        textPaint.reset();
        textPaint.setColor(Color.parseColor("#999999"));
        textPaint.setTextSize(sp2px(10));
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < list.size(); i++) {
            canvas.drawText(String.valueOf(list.get(i).year), i * mScreenWidth / list.size() + mMarginLeft, mScreenHeight - 0.2f * mMarginBottom, textPaint);
        }

    }

    public void setLineData(List<LineChartData> lineChartDataList) {
        if (lineChartDataList != null && lineChartDataList.size() > 0) {
            this.list = lineChartDataList;
            //处理折线数据
            mMinSalary = list.get(0).salary;

            for (int i = 0; i < list.size(); i++) {
                mMinSalary = mMinSalary < list.get(i).salary ? mMinSalary : list.get(i).salary;
                mMaxSalary = mMaxSalary > list.get(i).salary ? mMaxSalary : list.get(i).salary;
            }

            singHeight = (mMaxSalary - mMinSalary) / (mScreenHeight - mMarginBottom - mMarginTop);
            if (fList.size() > 0) {
                fList.clear();
            }
            //坐标换算
            for (int i = 0; i < list.size(); i++) {
                LineChartData lineChartData = list.get(i);
                float x = i * mScreenWidth / list.size() + mMarginLeft + mOffset;
                float y;
                if (lineChartData.salary == mMinSalary) {
                    y = (float) (mScreenHeight - mMarginBottom - (lineChartData.salary - mMinSalary) / singHeight - mOffset * 0.5);
                } else if (lineChartData.salary == mMaxSalary) {
                    y = mScreenHeight - mMarginBottom - (lineChartData.salary - mMinSalary) / singHeight + mOffset;
                } else {
                    y = mScreenHeight - mMarginBottom - (lineChartData.salary - mMinSalary) / singHeight;
                }
                fList.add(new PointF(x, y));
            }
            //以下数据为了画出第一段和最后一段的曲线
            fList.add(0, new PointF(fList.get(0).x, fList.get(0).y));
            fList.add(new PointF(fList.get(fList.size() - 1).x, fList.get(fList.size() - 1).y));
            fList.add(new PointF(fList.get(fList.size() - 1).x, fList.get(fList.size() - 1).y));
            invalidate();
        }

    }

    public void setOnTouchChartListener(OnTouchChartListener listener) {
        this.touchListener = listener;
    }

    public interface OnTouchChartListener {
        void onTouch(MotionEvent event);
    }
}
