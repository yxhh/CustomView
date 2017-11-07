package com.ruolian.yw.coustomview;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ruolian.yw.R;
import com.ruolian.yw.utils.ScreenUtils;

/**
 * Created by yw on 17-11-7.
 */

public class FinaceProgressBar extends RelativeLayout {
    private ProgressBar progressBar;
    private TextView percantageView;
    private ImageView dotView;
    private boolean isShowPercanage;

    public FinaceProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public FinaceProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FinaceProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FinaceProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.finace_progressbar, this);
        percantageView = (TextView) view.findViewById(R.id.percentage);
        dotView = (ImageView) view.findViewById(R.id.dot);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FinaceProgressBar);

            int percantageIconId = typedArray.getResourceId(R.styleable.FinaceProgressBar_uiPercantageIcon, -1);
            if (percantageIconId != -1) {
                percantageView.setBackgroundResource(percantageIconId);
            }
            int uiDotId = typedArray.getResourceId(R.styleable.FinaceProgressBar_uiDotResources, -1);
            if (uiDotId != -1) {
                dotView.setImageResource(uiDotId);
            }
            isShowPercanage = typedArray.getBoolean(R.styleable.FinaceProgressBar_isShowPercanage, false);
            if (isShowPercanage) {
                percantageView.setVisibility(VISIBLE);
            } else {
                percantageView.setVisibility(GONE);
            }
            //注意进度设置放在最后,需要等其他控件初始化完成之后
            int uiProgress = typedArray.getInt(R.styleable.FinaceProgressBar_uiProgress, -1);
            if (uiProgress != -1) {
                setProgress(uiProgress);
            }
            typedArray.recycle();
        }
    }

    @SuppressWarnings("ResourceType")
    public void setProgress(final int progress) {
        if (progress >= 0) {
            ValueAnimator valueAnimator = new ValueAnimator().ofFloat(1f);
            valueAnimator.setDuration(2000);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float temp = (float) animation.getAnimatedValue();
                    int pro = (int) (temp * progress);
                    initProgress(pro);
                }
            });
            valueAnimator.start();

        }
    }

    private void initProgress(int progress) {
        progressBar.setProgress(progress);
        percantageView.setText(progress + "%");
        int progressMax = progressBar.getMax();
        int screenWidth = ScreenUtils.getScreenWith(getContext());

        LayoutParams dotParams = (LayoutParams) dotView.getLayoutParams();
        int dotLeftMargin = screenWidth * progress / progressMax - dotView.getWidth() / 2;
        dotLeftMargin = dotLeftMargin > screenWidth - dotView.getWidth() ? screenWidth - dotView.getWidth() : dotLeftMargin;
        dotParams.leftMargin = dotLeftMargin < 0 ? 0 : dotLeftMargin;
        dotView.setLayoutParams(dotParams);

        if (isShowPercanage) {
            int screenProgress = screenWidth * progress / progressMax;
            int percantageLeftMargin = screenProgress - percantageView.getWidth() / 2;

            LayoutParams percantageParams = (LayoutParams) percantageView.getLayoutParams();

            //最小的左侧间距
            int percantageLeftMarginMin = dotView.getWidth() / 2;
            if (screenProgress <= percantageLeftMarginMin) {
                percantageLeftMargin = percantageLeftMarginMin;
            } else if (screenProgress < percantageView.getWidth() / 2 + dotView.getWidth() / 2
                    && screenProgress > percantageLeftMarginMin) {
                percantageLeftMargin = screenProgress;
            }
            //判断最大值和红点对其
            int percantageLeftMarginMax = screenWidth - percantageView.getWidth() - dotView.getWidth() / 2;
            percantageLeftMargin = screenProgress > screenWidth - dotView.getWidth() / 2 ? percantageLeftMarginMax : percantageLeftMargin;
            if (screenProgress > screenWidth - percantageView.getWidth() / 2 - dotView.getWidth() / 2
                    && screenProgress < screenWidth - dotView.getWidth() / 2) {
                percantageLeftMargin = screenProgress - percantageView.getWidth();
            }
            percantageParams.leftMargin = percantageLeftMargin;
            percantageView.setLayoutParams(percantageParams);
            //屏幕左右两侧,不同的背景百分比
            if (screenProgress < percantageView.getWidth() / 2 + dotView.getWidth() / 2) {
                percantageView.setBackgroundResource(R.mipmap.progress_bg_left);
            } else if (screenWidth - screenProgress < percantageView.getWidth() / 2 + dotView.getWidth() / 2) {
                percantageView.setBackgroundResource(R.mipmap.progress_bg_right);
            } else {
                percantageView.setBackgroundResource(R.mipmap.progress_bg);
            }
        } else {

        }
    }

}
