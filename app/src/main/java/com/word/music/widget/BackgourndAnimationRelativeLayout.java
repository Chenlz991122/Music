package com.word.music.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import com.word.music.R;


public class BackgourndAnimationRelativeLayout extends RelativeLayout {

    private final int DURATION_ANIMATION = 500;
    private final int INDEX_BACKGROUND = 0;
    private final int INDEX_FOREGROUND = 1;


    private LayerDrawable layerDrawable;
    private ObjectAnimator objectAnimator;
    private int musicPicRes = -1;

    public BackgourndAnimationRelativeLayout(Context context) {
        this(context, null);
    }

    public BackgourndAnimationRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BackgourndAnimationRelativeLayout(Context context, AttributeSet attrs, int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayerDrawable();
        initObjectAnimator();
    }

    private void initLayerDrawable() {
        Drawable backgroundDrawable = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            backgroundDrawable = getContext().getDrawable(R.drawable.ic_bg);
        }
        Drawable[] drawables = new Drawable[2];

        drawables[INDEX_BACKGROUND] = backgroundDrawable;
        drawables[INDEX_FOREGROUND] = backgroundDrawable;

        layerDrawable = new LayerDrawable(drawables);
    }

    private void initObjectAnimator() {
        objectAnimator = ObjectAnimator.ofFloat(this, "number", 0f, 1.0f);
        objectAnimator.setDuration(DURATION_ANIMATION);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int foregroundAlpha = (int) ((float) animation.getAnimatedValue() * 255);
                layerDrawable.getDrawable(INDEX_FOREGROUND).setAlpha(foregroundAlpha);
                BackgourndAnimationRelativeLayout.this.setBackground(layerDrawable);
            }
        });
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    layerDrawable.setDrawable(INDEX_BACKGROUND, layerDrawable.getDrawable(
                            INDEX_FOREGROUND));
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void setForeground(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layerDrawable.setDrawable(INDEX_FOREGROUND, drawable);
        }
    }

    public void beginAnimation() {
        objectAnimator.start();
    }

    public boolean isNeed2UpdateBackground(int musicPicRes) {
        if (this.musicPicRes == -1) return true;
        if (musicPicRes != this.musicPicRes) {
            return true;
        }
        return false;
    }
}
