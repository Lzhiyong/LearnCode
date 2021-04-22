package com.text.edit;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.text.TextPaint;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import java.lang.ref.WeakReference;

public class TextAnimate extends SuperscriptSpan implements ValueAnimator.AnimatorUpdateListener {
    private final WeakReference<TextView> textView;
    // ecah char delay time
    private final int delay;
    // animation duration
    private final int duration;
    // animate range
    private final float animatedRange;
    // char offset
    private int shift;
    private ValueAnimator valueAnimator;

    public TextAnimate(@NonNull TextView textView,
                       @IntRange(from = 1) int duration,
                       @IntRange(from = 0) int position,
                       @IntRange(from = 0) int waveCharOffset,
                       @FloatRange(from = 0, to = 1, fromInclusive = false) float animatedRange) {
        this.textView = new WeakReference<>(textView);
        this.delay = waveCharOffset * position;
        this.duration = duration;
        this.animatedRange = animatedRange;
    }


    @Override
    public void updateDrawState(TextPaint tp) {
        super.updateDrawState(tp);
        initIfNecessary(tp.ascent());
        // the text baseline
        tp.baselineShift = shift;

    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        super.updateMeasureState(tp);
        initIfNecessary(tp.ascent());
        tp.baselineShift = shift;
    }

    private void initIfNecessary(float ascent) {
        if(valueAnimator != null) {
            return;
        }

        this.shift = 0;
        int maxShift = (int) ascent / 2;
        // max offset shift*(0,1)
        valueAnimator = ValueAnimator.ofInt(0, maxShift);
        valueAnimator.setDuration(duration).setStartDelay(delay);
        valueAnimator.setInterpolator(new JumpInterpolator(animatedRange));
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.addUpdateListener(this);
        valueAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // No need for synchronization as this always runs on main thread anyway
        TextView v = textView.get();
        if(v != null) {
            updateAnimationFor(animation, v);
        }
    }

    private void updateAnimationFor(ValueAnimator animation, TextView v) {
        if(isAttachedToHierarchy(v)) {
            // char offset shift*(0,1)
            shift = (Integer)animation.getAnimatedValue();
            v.invalidate();
        }
    }

    private static boolean isAttachedToHierarchy(View v) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return v.isAttachedToWindow();
        }
        // Best-effort fallback (without adding support-v4 just for this...)
        return v.getParent() != null;   
    }

    private static class JumpInterpolator implements TimeInterpolator {

        private final float animRange;

        public JumpInterpolator(float animatedRange) {
            // animate range(0,1)
            animRange = Math.abs(animatedRange);
        }

        @Override
        public float getInterpolation(float input) {
            // We want to map the [0, PI] sine range onto [0, animRange]
            if(input > animRange) return 0f;
            // the radians reange(0,pi)
            double radians = (input / animRange) * Math.PI;
            // the range(0,1)
            return (float) Math.sin(radians);
        }
    }
}
