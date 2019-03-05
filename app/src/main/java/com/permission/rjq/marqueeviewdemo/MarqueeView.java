package com.permission.rjq.marqueeviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

public class MarqueeView extends ViewFlipper {

    private int textSize = 14;
    private int textColor = 0xffffffff;
    private boolean singleLine = false;

    private int gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    private static final int GRAVITY_LEFT = 0;
    private static final int GRAVITY_CENTER = 1;
    private static final int GRAVITY_RIGHT = 2;

    private int rollModel;
    private static final int ROLL_MODEL_FOREVER = 0;
    private static final int ROLL_MODEL_ONCE = 1;
    private boolean rollModelForeverStart;

    private int position;
    private List<? extends CharSequence> notices = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private boolean isRepeatUseView;

    public MarqueeView(Context context) {
        this(context, null);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public int px2sp(float pxValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeViewStyle, defStyleAttr, 0);

        singleLine = typedArray.getBoolean(R.styleable.MarqueeViewStyle_mvSingleLine, false);
        if (typedArray.hasValue(R.styleable.MarqueeViewStyle_mvTextSize)) {
            textSize = (int) typedArray.getDimension(R.styleable.MarqueeViewStyle_mvTextSize, textSize);
            textSize = px2sp(textSize);
        }
        textColor = typedArray.getColor(R.styleable.MarqueeViewStyle_mvTextColor, textColor);

        int gravityType = typedArray.getInt(R.styleable.MarqueeViewStyle_mvGravity, GRAVITY_LEFT);
        switch (gravityType) {
            case GRAVITY_LEFT:
                gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                break;
            case GRAVITY_CENTER:
                gravity = Gravity.CENTER;
                break;
            case GRAVITY_RIGHT:
                gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                break;
        }
        rollModel = typedArray.getInt(R.styleable.MarqueeViewStyle_mvRollModel, 0);
        typedArray.recycle();
    }

    /**
     * 根据字符串列表，启动翻页公告
     *
     * @param notices 字符串列表
     */
    public void startWithList(List<? extends CharSequence> notices) {
        startWithList(notices, 0);
    }

    /**
     * 根据字符串列表，启动翻页公告
     *
     * @param notices 字符串列表
     */
    public void startWithList(List notices, int interval) {
        if (notices == null || notices.size() == 0) return;
        //在外面使用到该控件的地方setFlipInterval()不生效；给该类声明一个成员变量，在使用的地方set成员变量，再在该方法中setFlipInterval(成员变量)也不生效，每次在该方法中使用时成员变量都是0；
        //那就直接把值带过来，在该方法中setFlipInterval()，生效！
        if (interval > 0) {
            setFlipInterval(interval);
        }
        if (rollModel == ROLL_MODEL_FOREVER) {
            this.notices = notices;
            if (getChildCount() >= 2) {
                position = 0;
            } else {
                position = -1;
            }
            if (!rollModelForeverStart) {
                rollModelForeverStart = true;
                position = 0;
                postStart();
            }
        } else if (rollModel == ROLL_MODEL_ONCE) {
            if (position == 0 || position == this.notices.size()) {
                this.notices = notices;
                position = 0;
                postStart();
            } else {
                this.notices = notices;
                if (getChildCount() >= 2) {
                    position = 0;
                } else {
                    position = -1;
                }
            }
        }
    }

    private void postStart() {
        post(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    private boolean isAnimStart = false;

    private void start() {
        removeAllViews();
        addView(createTextView(notices.get(position)));
        if (notices.size() > 1) {
            //开始View的切换，而且会循环进行
            startFlipping();
        }
        if (getInAnimation() != null) {
            getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (isAnimStart) {
                        animation.cancel();
                    }
                    isAnimStart = true;
                    if (isRepeatUseView) {
                        repeatUseItem(getCurrentView(), notices.get(position));
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    position++;
                    if (position >= notices.size()) {
                        if (rollModel == ROLL_MODEL_FOREVER) {
                            // 循环滚动；不设置position = 0， position == notices.size()时不addView，ViewFlipper也会循环滚动；
                            // 这里设置position = 0 ，为了后续startWith新数据时并不重新start()，能够有连续的动画
                            position = 0;
                        } else if (rollModel == ROLL_MODEL_ONCE) {
                            //滚动到notices的最后一条就停止滚动
                            stopFlipping();
                        }
                    }
                    //如果是循环滚动||(滚动一次 && position < notices.size())
                    if (rollModel == ROLL_MODEL_FOREVER || position < notices.size()) {
                        if (getChildCount() < 2) {
                            View view = createTextView(notices.get(position));
                            if (view.getParent() == null) {
                                addView(view);
                                isRepeatUseView = true;
                            }
                        }
                    }
                    isAnimStart = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    //复用item
    private void repeatUseItem(View view, CharSequence text) {
        TextView textView = (TextView) view;
        textView.setGravity(gravity);
        textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        textView.setSingleLine(singleLine);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(getPosition(), v);
                }
            }
        });
        textView.setText(text);
        textView.setTag(position);
    }

    //创建item
    private TextView createTextView(CharSequence text) {
        TextView textView = new TextView(getContext());
        textView.setGravity(gravity);
        textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        textView.setSingleLine(singleLine);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(getPosition(), v);
                }
            }
        });
        textView.setText(text);
        textView.setTag(position);
        return textView;
    }

    public int getPosition() {
        return (int) getCurrentView().getTag();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

}
