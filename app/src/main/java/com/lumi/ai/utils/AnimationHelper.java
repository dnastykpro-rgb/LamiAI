package com.lumi.ai.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class AnimationHelper {

    // Пружинный отклик при нажатии на любую кнопку + легкая вибрация
    public static void setupSpringButton(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    animateScale(v, 0.90f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animateScale(v, 1.0f);
                    break;
            }
            return false;
        });
    }

    private static void animateScale(View view, float targetScale) {
        SpringAnimation scaleX = new SpringAnimation(view, DynamicAnimation.SCALE_X, targetScale);
        SpringAnimation scaleY = new SpringAnimation(view, DynamicAnimation.SCALE_Y, targetScale);

        SpringForce spring = new SpringForce(targetScale)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_MEDIUM);

        scaleX.setSpring(spring);
        scaleY.setSpring(spring);
        scaleX.start();
        scaleY.start();
    }

    // Плавный вылет сообщения в чате с пружинной доводкой
    public static void animateMessageAppear(View itemView, boolean isUser) {
        itemView.setAlpha(0f);
        itemView.setTranslationY(60f);
        itemView.setTranslationX(isUser ? 40f : -40f);

        itemView.animate()
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        SpringAnimation transY = new SpringAnimation(itemView, DynamicAnimation.TRANSLATION_Y, 0f);
        transY.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        transY.getSpring().setStiffness(SpringForce.STIFFNESS_LOW);
        transY.start();

        SpringAnimation transX = new SpringAnimation(itemView, DynamicAnimation.TRANSLATION_X, 0f);
        transX.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
        transX.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM);
        transX.start();
    }

    // Пульсация индикатора «Думает...»
    public static ObjectAnimator startPulseAnimation(View dotView) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(dotView, "alpha", 0.25f, 1.0f);
        animator.setDuration(650);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
        return animator;
    }

    // Плавная смена текста «Думает...» -> «Думает еще...»
    public static void crossfadeText(android.widget.TextView textView, String newText) {
        textView.animate()
                .alpha(0f)
                .translationY(-10f)
                .setDuration(200)
                .withEndAction(() -> {
                    textView.setText(newText);
                    textView.setTranslationY(10f);
                    textView.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(250)
                            .start();
                })
                .start();
    }
}

