package com.ruolian.yw.coustomview.chart;

import android.animation.ValueAnimator;
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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.ruolian.yw.R;
import com.ruolian.yw.coustomview.chart.chartdata.LineChartData;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author yangwang
 * @date 18-6-1 16:14
 * @company Beijing QiaoData Management Co.
 * @projectName code
 * @packageName com.yw.linechat.linechat
 */
public class LineChart extends View {
    private static final int ANIMATOR_TIME = 2 * 1000;
    float CTRL_VALUE_A = 0.2f, CTRL_VALUE_B = 0.2f;//滑度系数
    int MAX_SHOW_SIZE = 8;
    int MAX_RECRUIT_SHOW_SIZE = 31;
    float downX = 0, downY;
    private float touchMoveX = 0;
    private LineChartEnum chartEnum;
    private Paint linePaint, textPaint;
    private List<LineChartData> list = new ArrayList<>();//原始数据
    private List<PointF> fList = new ArrayList<>();//存储转换为xy轴坐标的数据
    private float mScreenWidth, mScreenHeight, mMarginLeft = dip2px(10), mMarginRight = dip2px(10);
    private float mMarginTop = dip2px(20), mMarginBottom = dip2px(20);
    private float mOffset = dip2px(10);//偏移量
    private float singHeight, mMinSalary = 0, mMaxSalary = 0;
    private float radius = dip2px(3);//圆圈半径
    private int midLineColor = Color.parseColor("#F1F1F1");//中间线的色值
    private int defaultShadowColor = Color.parseColor("#503aa7ff");//中间线的色值
    private int lineColor, textColor, shadowColor;
    private OnTouchChartListener touchListener;
    private int textBackground;
    private boolean isBezierCurve;
    private List<LineChartData> sourceData/*//源数据，所有数据*/;
    //======事件分发需要===================
    private float xDown;// 记录手指按下时的横坐标。
    private float xMove;// 记录手指移动时的横坐标。
    private float yDown;// 记录手指按下时的纵坐标。
    private float yMove;// 记录手指移动时的纵坐标。
    //======事件分发需要===================
    private GestureDetector gestureDetector;
    private int yScaleMax, yScaleTopMid, yScaleBottomMid, yScaleMin;//y轴的刻度值

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
        linePaint.setStrokeWidth(dip2px(1));
        gestureDetector = new GestureDetector(context, new MyOnGestureListener());


        textPaint = new Paint();
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineChart);
            lineColor = typedArray.getColor(R.styleable.LineChart_lineColor, Color.parseColor("#3AA7FF"));
            textColor = typedArray.getColor(R.styleable.LineChart_textColor, Color.parseColor("#3AA7FF"));
            textBackground = typedArray.getResourceId(R.styleable.LineChart_textBackground, -1);
            shadowColor = typedArray.getColor(R.styleable.LineChart_shadowColor, Color.parseColor("#503aa7ff"));
            isBezierCurve = typedArray.getBoolean(R.styleable.LineChart_iseBezierCurve, false);
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
        if (chartEnum == LineChartEnum.SALARY) {
            drawLine(canvas);
        } else if (chartEnum == LineChartEnum.JOB) {
            drawRecruitLine(canvas);
        }

        drawY(canvas);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xDown = ev.getX();
                yDown = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = ev.getX();
                float yMove = ev.getY();
                if (Math.abs(yMove - yDown) < Math.abs(xMove - xDown)
                        && Math.abs(xMove - xDown) > 2) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((chartEnum == LineChartEnum.SALARY && sourceData.size() <= MAX_SHOW_SIZE)
                || (chartEnum == LineChartEnum.JOB && sourceData.size() <= MAX_RECRUIT_SHOW_SIZE)
                ) {
            return super.onTouchEvent(event);
        } else {
            gestureDetector.onTouchEvent(event);
            return true;
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
            path.moveTo(touchMoveX + fList.get(1).x, fList.get(1).y);
            for (int i = 1; i < fList.size() - 3; i++) {
                if (isBezierCurve) {
                    PointF ctrlPointA = new PointF();
                    PointF ctrlPointB = new PointF();
                    getCtrlPoint(fList, i, ctrlPointA, ctrlPointB);
                    path.cubicTo(ctrlPointA.x, ctrlPointA.y, ctrlPointB.x, ctrlPointB.y, fList.get(i + 1).x, fList.get(i + 1).y);
                } else {
                    path.lineTo(touchMoveX + fList.get(i + 1).x, fList.get(i + 1).y);
                }
            }
            linePaint.reset();
            linePaint.setAntiAlias(true);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(dip2px(2));
            linePaint.setColor(lineColor);
            canvas.drawPath(path, linePaint);
            //画折线图的下方的阴影
            linePaint.reset();
            linePaint.setShader(new LinearGradient(touchMoveX + fList.get(fList.size() - 2).x, fList.get(fList.size() - 2).y,
                    touchMoveX + fList.get(fList.size() - 2).x, mScreenHeight,
                    shadowColor, Color.TRANSPARENT, Shader.TileMode.CLAMP));

            path.lineTo(touchMoveX + fList.get(fList.size() - 2).x + radius, fList.get(fList.size() - 2).y);
            path.lineTo(touchMoveX + fList.get(fList.size() - 2).x + radius, mScreenHeight - mMarginBottom);
            path.lineTo(touchMoveX + fList.get(0).x - radius, mScreenHeight - mMarginBottom);
            path.lineTo(touchMoveX + fList.get(0).x - radius, fList.get(0).y);
            path.close();
            canvas.drawPath(path, linePaint);

            Bitmap bitmap;
            if (textBackground != -1) {
                bitmap = BitmapFactory.decodeResource(getResources(), textBackground);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.message);
            }
            //中间的竖线画笔
            linePaint.reset();
            linePaint.setColor(midLineColor);
            linePaint.setAntiAlias(true);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(dip2px(1));
            linePaint.setPathEffect(new DashPathEffect(new float[]{dip2px(2), dip2px(2)}, 0));

            for (int i = 0; i < list.size(); i++) {
                LineChartData chartData = list.get(i);
                float x = touchMoveX + fList.get(i + 1).x, y = fList.get(i + 1).y;
                //画对应的竖线
                canvas.drawLine(x, 0, x, mScreenHeight - mMarginBottom, linePaint);
                //画圆圈
                textPaint.reset();
                textPaint.setAntiAlias(true);
                textPaint.setStyle(Paint.Style.STROKE);
                textPaint.setStrokeWidth(dip2px(2));
                textPaint.setColor(lineColor);
                canvas.drawCircle(x, y, radius, textPaint);
                textPaint.reset();
                textPaint.setAntiAlias(true);
                textPaint.setColor(Color.WHITE);
                canvas.drawCircle(x, y, radius * 0.7f, textPaint);

                //画文字背景
                canvas.drawBitmap(bitmap, null, new Rect((int) (x - dip2px(15)),
                        (int) (y - radius - dip2px(25)),
                        (int) (x + dip2px(15)),
                        (int) (y - radius)), textPaint);
                //画文字
                textPaint.setColor(textColor);
                textPaint.setTextSize(sp2px(10));
                switch (chartEnum) {
                    case JOB:
                        if (chartData.salary / 10 < 1) {
                            canvas.drawText(String.valueOf(chartData.salary), x - dip2px(3), y - dip2px(13), textPaint);
                        } else if (chartData.salary / 100 >= 1) {
                            canvas.drawText(String.valueOf(chartData.salary), x - dip2px(9), y - dip2px(13), textPaint);
                        } else {
                            canvas.drawText(String.valueOf(chartData.salary), x - dip2px(6), y - dip2px(13), textPaint);
                        }
                        break;

                    case SALARY:
                        if (chartData.salary / 10 < 1) {
                            canvas.drawText(chartData.salary + "K", x - dip2px(6), y - dip2px(13), textPaint);
                        } else if (chartData.salary / 100 >= 1) {
                            canvas.drawText(chartData.salary + "K", x - dip2px(12), y - dip2px(13), textPaint);
                        } else {
                            canvas.drawText(chartData.salary + "K", x - dip2px(9), y - dip2px(13), textPaint);
                        }
                        break;
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

    /**
     * 画增员的图
     * 当前为柱状图不需要贝塞尔曲线
     *
     * @param canvas
     */
    private void drawRecruitLine(Canvas canvas) {
        if (list != null && list.size() > 0 && fList.size() > 0) {
            Path path = new Path();
            linePaint.reset();
            linePaint.setAntiAlias(true);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(dip2px(4));
            linePaint.setColor(lineColor);
            //投递次数的背景
            Bitmap bitmap;
            if (textBackground != -1) {
                bitmap = BitmapFactory.decodeResource(getResources(), textBackground);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.message);
            }
            textPaint.reset();
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.WHITE);
            textPaint.setColor(textColor);
            textPaint.setTextSize(sp2px(10));

            boolean flag = true;//只画最大值里面，最近的一个日期
            for (int i = list.size() - 1; i >= 0; i--) {
                LineChartData chartData = list.get(i);
                float x = touchMoveX + fList.get(i + 1).x, y = fList.get(i + 1).y;
                if (chartData.salary != 0) {
                    path.reset();
                    path.moveTo(x, y);
                    path.lineTo(x, mScreenHeight - mMarginBottom - 0.1f * mOffset);
                    canvas.drawPath(path, linePaint);
                }

                if (chartData.salary == mMaxSalary && flag) {
                    flag = false;
                    //画文字背景
                    canvas.drawBitmap(bitmap, null, new Rect((int) (x - dip2px(15)),
                            (int) (y - radius - dip2px(25)),
                            (int) (x + dip2px(15)),
                            (int) (y - radius)), textPaint);
                    //画文字
                    if (chartData.salary / 10 < 1) {
                        canvas.drawText(String.valueOf(chartData.salary), x - dip2px(3), y - dip2px(13), textPaint);
                    } else if (chartData.salary / 100 >= 1) {
                        canvas.drawText(String.valueOf(chartData.salary), x - dip2px(9), y - dip2px(13), textPaint);
                    } else {
                        canvas.drawText(String.valueOf(chartData.salary), x - dip2px(6), y - dip2px(13), textPaint);
                    }
                }
            }
        }
    }

    /**
     * 画Y轴
     *
     * @param canvas
     */

    private void drawY(Canvas canvas) {
        linePaint.reset();
        linePaint.setColor(Color.parseColor("#ffffff"));
        linePaint.setAntiAlias(true);
        Path path = new Path();
        float y = mScreenHeight - mMarginBottom;
        path.moveTo(0, 0);
        path.lineTo(2 * mOffset, 0);
        path.lineTo(2 * mOffset, mScreenHeight);
        path.lineTo(0, mScreenHeight);
        path.lineTo(0, 0);
        path.close();
        canvas.drawPath(path, linePaint);

        linePaint.reset();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(dip2px(1));
        linePaint.setColor(Color.parseColor("#999999"));
        linePaint.setStyle(Paint.Style.STROKE);
        //y轴 箭头
        path.reset();
        path.moveTo(2 * mOffset, 0);
        path.lineTo((float) (2.45 * mOffset), 0.5f * mOffset);
        canvas.drawPath(path, linePaint);
        path.moveTo(2 * mOffset, 0);
        path.lineTo((float) (1.55 * mOffset), 0.5f * mOffset);
        canvas.drawPath(path, linePaint);
        //y座标轴
        path.reset();
        path.moveTo(2 * mOffset, 0);
        path.lineTo(2 * mOffset, y);
        canvas.drawPath(path, linePaint);
        //画y轴的数据
        textPaint.reset();
        textPaint.setColor(Color.parseColor("#999999"));
        textPaint.setTextSize(sp2px(10));
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);

        if (mMaxSalary > 60) {
            canvas.drawText(String.valueOf(yScaleMax), 0, y - yScaleMax / singHeight + mOffset / 2, textPaint);
        } else {
            canvas.drawText(String.valueOf(yScaleMax), 0.7f * mOffset, y - yScaleMax / singHeight + mOffset / 2, textPaint);
        }
        canvas.drawText(String.valueOf(yScaleTopMid), 0.7f * mOffset, y - yScaleTopMid / singHeight + mOffset / 2, textPaint);
        canvas.drawText(String.valueOf(yScaleBottomMid), 0.7f * mOffset, y - yScaleBottomMid / singHeight + mOffset / 2, textPaint);
        if (yScaleMin < 10) {
            canvas.drawText(String.valueOf(yScaleMin), mOffset, y - yScaleMin / singHeight + mOffset / 2, textPaint);
        } else {
            canvas.drawText(String.valueOf(yScaleMin), 0.7f * mOffset, y - yScaleMin / singHeight + mOffset / 2, textPaint);
        }
        canvas.drawText("0", mOffset, y + mOffset / 2, textPaint);

    }

    private void drawLineFrame(Canvas canvas) {
        if (fList.size() == 0) {
            return;
        }
        linePaint.reset();
        linePaint.setColor(Color.parseColor("#999999"));
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dip2px(1));
//        linePaint.setPathEffect(new DashPathEffect(new float[]{dip2px(2), dip2px(2)}, 0));
        //边框---横线 x轴
        float y = mScreenHeight - mMarginBottom;
        Path path = new Path();
        if (sourceData == null || sourceData.size() < MAX_SHOW_SIZE) {
            //x轴的箭头
           /* path.moveTo(mScreenWidth, y);
            path.lineTo(mScreenWidth - mOffset, y - mOffset / 2);
            canvas.drawPath(path, linePaint);
            path.moveTo(mScreenWidth, y);
            path.lineTo(mScreenWidth - mOffset, y + mOffset / 2);
            canvas.drawPath(path, linePaint);*/
            //X轴线
            path.moveTo(0, y);
            path.lineTo(mScreenWidth, y);
            canvas.drawPath(path, linePaint);
        } else {
            //x轴箭头暂时隐藏
            /*if (touchMoveX == mScreenWidth - mMarginLeft - mOffset - fList.get(fList.size() - 1).x) {
                //x轴的箭头
                path.moveTo(mScreenWidth, y);
                path.lineTo(mScreenWidth - mOffset, y - mOffset / 2);
                canvas.drawPath(path, linePaint);
                path.moveTo(mScreenWidth, y);
                path.lineTo(mScreenWidth - mOffset, y + mOffset / 2);
                canvas.drawPath(path, linePaint);
            }*/
            //X轴线
            path.moveTo(0, y);
            path.lineTo(mScreenWidth / MAX_SHOW_SIZE * sourceData.size(), y);
            canvas.drawPath(path, linePaint);
        }


        //中间的横线
        linePaint.reset();
        linePaint.setColor(midLineColor);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dip2px(1));
        if (sourceData == null || sourceData.size() < MAX_SHOW_SIZE) {
            canvas.drawLine(0, y - yScaleMax / singHeight, mScreenWidth, y - yScaleMax / singHeight, linePaint);
            canvas.drawLine(0, y - yScaleTopMid / singHeight, mScreenWidth, y - yScaleTopMid / singHeight, linePaint);
            canvas.drawLine(0, y - yScaleBottomMid / singHeight, mScreenWidth, y - yScaleBottomMid / singHeight, linePaint);
            canvas.drawLine(0, y - yScaleMin / singHeight, mScreenWidth, y - yScaleMin / singHeight, linePaint);
        } else {
            canvas.drawLine(0, y - yScaleMax / singHeight, mScreenWidth / MAX_SHOW_SIZE * sourceData.size(), y - yScaleMax / singHeight, linePaint);
            canvas.drawLine(0, y - yScaleTopMid / singHeight, mScreenWidth / MAX_SHOW_SIZE * sourceData.size(), y - yScaleTopMid / singHeight, linePaint);
            canvas.drawLine(0, y - yScaleBottomMid / singHeight, mScreenWidth / MAX_SHOW_SIZE * sourceData.size(), y - yScaleBottomMid / singHeight, linePaint);
            canvas.drawLine(0, y - yScaleMin / singHeight, mScreenWidth / MAX_SHOW_SIZE * sourceData.size(), y - yScaleMin / singHeight, linePaint);
        }
        //画年份
        //年份的画笔
        textPaint.reset();
        textPaint.setColor(Color.parseColor("#999999"));
        textPaint.setTextSize(sp2px(10));
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);

        //三角形需要的画笔
        linePaint.reset();
        linePaint.setAntiAlias(true);
        linePaint.setColor(lineColor);
        for (int i = list.size() - 1; i >= 0; i--) {
            float x = touchMoveX + fList.get(i + 1).x;
            if (chartEnum == LineChartEnum.JOB) {
                //画刻度
                canvas.drawLine(x, mScreenHeight - mMarginBottom, x, mScreenHeight - mMarginBottom - dip2px(2), textPaint);
                //每五天显示一个x轴时间
                if ((list.size() - 1 - i) % 5 != 0) {
                    continue;
                }
            }
            //年份&日期
            if (chartEnum == LineChartEnum.JOB) {
                //画日期
                canvas.drawText(String.valueOf(list.get(i).year), x - 1.5f * mOffset, mScreenHeight - 0.1f * mOffset, textPaint);
            } else if (chartEnum == LineChartEnum.SALARY) {
                canvas.drawText(String.valueOf(list.get(i).year), x - mOffset, mScreenHeight - 0.1f * mOffset, textPaint);
            }
            //年份上面的三角形
            path.reset();
            path.moveTo(x, mScreenHeight - mMarginBottom + 0.3f * mOffset);
            path.lineTo(x - 0.4f * mOffset, mScreenHeight - mMarginBottom + 0.9f * mOffset);
            path.lineTo(x + 0.4f * mOffset, mScreenHeight - mMarginBottom + 0.9f * mOffset);
            path.lineTo(x, mScreenHeight - mMarginBottom + 0.3f * mOffset);
            path.close();
            canvas.drawPath(path, linePaint);
        }


    }

    public void setLineData(final List<LineChartData> finalLineChartDataList, final LineChartEnum chartEnum) {
        if (finalLineChartDataList == null || finalLineChartDataList.size() == 0) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                LineChart.this.sourceData = finalLineChartDataList;

                mMinSalary = finalLineChartDataList.get(0).salary;
                mMaxSalary = 0;

                for (int i = 0; i < finalLineChartDataList.size(); i++) {
                    mMinSalary = mMinSalary < finalLineChartDataList.get(i).salary ? mMinSalary : finalLineChartDataList.get(i).salary;
                    mMaxSalary = mMaxSalary > finalLineChartDataList.get(i).salary ? mMaxSalary : finalLineChartDataList.get(i).salary;
                }
                setData(finalLineChartDataList, chartEnum);
            }
        });
    }


    private void setData(List<LineChartData> chartDataList, LineChartEnum chartEnum) {
        LineChart.this.chartEnum = chartEnum;
        list = chartDataList;
        //处理折线数据
        if (fList.size() > 0) {
            fList.clear();
        }
        //坐标换算
        switch (chartEnum) {
            case SALARY:
                //换算薪资的数据
                getSalaryPoint();

                break;
            case JOB:
                getRecruitPoint(chartDataList);

                break;
        }
        //以下数据为了画出第一段和最后一段的曲线
        if (fList.size() == 0) {
            this.setVisibility(GONE);
            return;
        }
        fList.add(0, new PointF(fList.get(0).x, fList.get(0).y));
        fList.add(new PointF(fList.get(fList.size() - 1).x, fList.get(fList.size() - 1).y));
        fList.add(new PointF(fList.get(fList.size() - 1).x, fList.get(fList.size() - 1).y));
        invalidate();
    }

    /**
     * 获取求职天数坐标点
     *
     * @param chartDataList
     */
    private void getRecruitPoint(List<LineChartData> chartDataList) {
        //y轴数据
        //最小值是5，也就是最小的间隔为5，然后随着最大值的增加，间隔成倍数增加
        float tempCount = (mMaxSalary / 4);
        int multiple = 1;
        if (tempCount % 5 == 0) {
            multiple = (int) (tempCount / 5);
        } else {
            multiple = (int) (tempCount / 5 + 1);
        }
        yScaleMax = 5 * (multiple + 3);
        yScaleTopMid = 5 * (multiple + 2);
        yScaleBottomMid = 5 * (multiple + 1);
        yScaleMin = 5 * multiple;

        singHeight = yScaleMax / (mScreenHeight - mMarginBottom - mMarginTop);

        //x轴数据
        if (chartDataList.size() < MAX_RECRUIT_SHOW_SIZE) {
            //如果修改接口字段注意修改此处的处理方式
//            chartDataList = get30ChartList(chartDataList);
            //此时代表服务端处理数据异常
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            LineChartData chartData = list.get(i);
            float x = (i + 1) * (mScreenWidth - 4 * mOffset) / (MAX_RECRUIT_SHOW_SIZE + 1) + mMarginLeft + 3 * mOffset;
            float y = getScreenY(chartData);
            fList.add(new PointF(x, y));
        }
        touchMoveX = mScreenWidth - mMarginLeft - mOffset - fList.get(fList.size() - 1).x;
    }

    private List<LineChartData> get30ChartList(List<LineChartData> chartDataList) {
        LineChartData chartData = chartDataList.get(chartDataList.size() - 1);
        String[] timeArr = chartData.year.split("-");
        Calendar calendar = Calendar.getInstance();
//        calendar.set(calendar.get(Calendar.YEAR), ConvertHelper.string2Int(timeArr[0]), ConvertHelper.string2Int(timeArr[1]));

        List<LineChartData> tempList = new ArrayList<>();


        return null;
    }

    /**
     * 换算薪资的数据
     */
    private void getSalaryPoint() {
        //y轴数据
    /*薪资
        <15  显示5-10-15-20k
        * <30 显示10-20-30-40k
        * <60 显示20-40-60-80k
        * <120 显示30-60-90-100k以上*/
        if (mMaxSalary <= 15) {
            singHeight = 20 / (mScreenHeight - mMarginBottom - mMarginTop);
            yScaleMax = 20;
            yScaleTopMid = 15;
            yScaleBottomMid = 10;
            yScaleMin = 5;
        } else if (mMaxSalary <= 30) {
            singHeight = 40 / (mScreenHeight - mMarginBottom - mMarginTop);
            yScaleMax = 40;
            yScaleTopMid = 30;
            yScaleBottomMid = 20;
            yScaleMin = 10;
        } else if (mMaxSalary <= 60) {
            singHeight = 80 / (mScreenHeight - mMarginBottom - mMarginTop);
            yScaleMax = 80;
            yScaleTopMid = 60;
            yScaleBottomMid = 40;
            yScaleMin = 20;
        } else {
            singHeight = 120 / (mScreenHeight - mMarginBottom - mMarginTop);
            yScaleMax = 120;
            yScaleTopMid = 90;
            yScaleBottomMid = 60;
            yScaleMin = 30;
        }
        //x轴数据
        if (list.size() == 1) {
            touchMoveX = 0;
            LineChartData chartData = list.get(0);
            float x = mScreenWidth / 2;
            float y = getScreenY(chartData);
            fList.add(new PointF(x, y));
        } else if (list.size() < MAX_SHOW_SIZE) {
            touchMoveX = 0;
            for (int i = 0; i < list.size(); i++) {
                LineChartData chartData = list.get(i);
                float x = (i) * (mScreenWidth - 6 * mMarginLeft) / (list.size() - 1) + mMarginLeft + 3 * mOffset;
                float y = getScreenY(chartData);
                fList.add(new PointF(x, y));
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                LineChartData chartData = list.get(i);
                float x = (i) * (mScreenWidth - 2 * mOffset) / (MAX_SHOW_SIZE) + mMarginLeft + 3 * mOffset;
                float y = getScreenY(chartData);
                fList.add(new PointF(x, y));
            }
            touchMoveX = mScreenWidth - mMarginLeft - mOffset - fList.get(fList.size() - 1).x;
        }
    }

    private float getScreenY(LineChartData chartData) {
        float y;
        if (singHeight == 0) {//如果所有的数值都相等，则直接在中间画横线
            y = mScreenHeight / 2;
        } else {
            y = mScreenHeight - mMarginBottom - (chartData.salary) / singHeight;
        }
        return y;
    }

    public void clearData() {
        list.clear();
        invalidate();
    }

    public void setOnTouchChartListener(OnTouchChartListener listener) {
        this.touchListener = listener;
    }

    private int dip2px(float dpValue) {
        return (int) (dpValue * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int sp2px(float spValue) {
        return (int) (spValue * getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

    public void setLineColor(int color) {
        if (color != -1) {
            this.lineColor = color;
        }
    }

    public void setShadowColor(int color) {
        if (color != -1) {
            this.shadowColor = color;
        }
    }

    public void setTextBackgrounResource(int resourceId) {
        if (resourceId != -1) {
            this.textBackground = resourceId;
        }
    }

    public enum LineChartEnum {
        //主要是为了区分文字的偏移量
        SALARY, JOB
    }

    public interface OnTouchChartListener {
        void onTouch(MotionEvent event);
    }

    /**
     * 手势事件
     */
    class MyOnGestureListener implements GestureDetector.OnGestureListener {

        private ValueAnimator valueAnimator;

        @Override
        public boolean onDown(MotionEvent e) { // 按下事件
            if (valueAnimator != null) {
                if (valueAnimator.isRunning()) {
                    valueAnimator.cancel();
                }
            }
            return false;
        }

        // 按下停留时间超过瞬时，并且按下时没有松开或拖动，就会执行此方法
        @Override
        public void onShowPress(MotionEvent motionEvent) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) { // 单击抬起
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1.getX() > mMarginLeft && e1.getX() < mScreenWidth - mMarginRight &&
                    e1.getY() > mMarginTop && e1.getY() < mScreenHeight - mMarginBottom) {
                //注意：这里的distanceX是e1.getX()-e2.getX()
                distanceX = -distanceX;
                if (touchMoveX + distanceX > 0) {//越界恢复
                    touchMoveX = 0;

                } else if (touchMoveX + distanceX < mScreenWidth - mMarginLeft - mOffset - fList.get(fList.size() - 1).x) {
                    touchMoveX = mScreenWidth - mMarginLeft - mOffset - fList.get(fList.size() - 1).x;
                } else {
                    touchMoveX = (touchMoveX + distanceX);
                }
                invalidate();
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
        } // 长按事件

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float targetTouchMoveX = touchMoveX + velocityX * ANIMATOR_TIME / 1000;
            //velocityX 代表的是e2 - e1 抬起的坐标减去按下的坐标
            if (targetTouchMoveX > 0) {//越界恢复
                targetTouchMoveX = 0;
            } else if (targetTouchMoveX < mScreenWidth - mMarginLeft - mOffset - fList.get(fList.size() - 1).x) {
                targetTouchMoveX = mScreenWidth - mMarginLeft - mOffset - fList.get(fList.size() - 1).x;
            } else {
                targetTouchMoveX = touchMoveX + velocityX * ANIMATOR_TIME / 1000;
            }
            valueAnimator = ValueAnimator.ofFloat(touchMoveX, targetTouchMoveX);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    touchMoveX = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            valueAnimator.setDuration(ANIMATOR_TIME);
            valueAnimator.setRepeatCount(0);
            valueAnimator.start();
            return false;
        }
    }
}