package com.ruolian.yw.coustomview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yw on 17-10-26.
 * 目前发现oppo手机自动识别url有bug
 * oppo会识别url以及包含以后的所有文本
 */

@SuppressLint("AppCompatCustomView")
public class UIUrlTextView extends TextView {
    private String pattern =
            "((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?|(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
    private boolean isUnderLine;
    private int linkStatus;
    private int linkTextColor;
    private OnLinkClickListener listener;

    public UIUrlTextView(Context context) {
        super(context);
        init(context, null);
    }

    public UIUrlTextView(Context context,  AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public UIUrlTextView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("NewApi")
    public UIUrlTextView(Context context,  AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UIUrlTextView);
            isUnderLine = typedArray.getBoolean(R.styleable.UIUrlTextView_isLinkUnderline, false);
            linkStatus = typedArray.getInt(R.styleable.UIUrlTextView_autoLink, -1);
            linkTextColor = typedArray.getColor(R.styleable.UIUrlTextView_linkTextColor, -1);
            setURLText(getText());
            typedArray.recycle();
        }
    }

    public void setURLText(final CharSequence text) {
        if (linkStatus != -1 && linkStatus == 0) {
            Spannable sp = new SpannableString(text);
            Pattern r = Pattern.compile(pattern);
            Matcher matcher = r.matcher(text);
            while (matcher.find()) {
                final int start = matcher.start();
                final int end = matcher.end();
                ClickableSpan cspan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (listener != null) {
                            listener.onClick(text.subSequence(start, end));
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        if (linkTextColor != -1) {
                            ds.setColor(linkTextColor);
                        }
                        ds.setUnderlineText(isUnderLine);
                    }
                };
                sp.setSpan(cspan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            setText(sp);
            setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            setText(text);
        }
    }

    public void setLinkClickListener(OnLinkClickListener listener) {
        this.listener = listener;
    }

    public interface OnLinkClickListener {
        void onClick(CharSequence linkText);
    }
}
