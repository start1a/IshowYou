package com.start3a.ishowyou.data

import android.content.Context
import android.content.pm.ActivityInfo
import android.util.DisplayMetrics
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentActivity

class FullScreenController(
    private val activity: FragmentActivity,
    private val parentView: ConstraintLayout,
    private val contentViewFrame: View,
    private val talkViewFrame: View
) {
    var contentExitFullScreenMode: (() -> Unit)? = null


    fun enterFullScreenView(weightContent: Float, weightTalk: Float) {
        // 가로로 보기 180도 회전
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        contentViewFrame.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        talkViewFrame.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        changeWeight(true, weightContent, weightTalk)
    }

    fun exitFullScreenView() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        contentViewFrame.layoutParams =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        talkViewFrame.layoutParams =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)

        changeConstraint(talkViewFrame, parentView, ConstraintSet.BOTTOM, ConstraintSet.BOTTOM, 0.0f)
        changeWeight(false)
    }

    fun rotate(isFullScreen: Boolean) {
        if (isFullScreen)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun resizeScreenHeight(height: Int = ConstraintLayout.LayoutParams.MATCH_PARENT) {
        var width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        contentViewFrame.layoutParams = ConstraintLayout.LayoutParams(width, height)
        width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        talkViewFrame.layoutParams = ConstraintLayout.LayoutParams(width, height)
    }

    private fun changeConstraint(
        mainView: View,
        targetView: View,
        mainConstraint: Int,
        targetConstraint: Int,
        valMargin: Float
    ) {
        ConstraintSet().let { cs ->
            cs.clone(parentView)
            cs.connect(
                mainView.id,
                mainConstraint,
                targetView.id,
                targetConstraint,
                convertDpToPixel(valMargin, activity)
            )
            cs.applyTo(parentView)
        }
    }

    fun changeWeight(isFullScreen: Boolean, weightContent: Float = 0.0f, weightTalk: Float = 0.0f) {
        ConstraintSet().let { cs ->
            cs.clone(parentView)
            if (isFullScreen) {
                cs.removeFromVerticalChain(contentViewFrame.id)
                cs.removeFromVerticalChain(talkViewFrame.id)
                cs.createHorizontalChain(
                    ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                    arrayOf(contentViewFrame.id, talkViewFrame.id).toIntArray(),
                    arrayOf(weightContent, weightTalk).toFloatArray(), ConstraintSet.CHAIN_SPREAD
                )
            } else {
                cs.removeFromHorizontalChain(contentViewFrame.id)
                cs.removeFromHorizontalChain(talkViewFrame.id)
            }
            cs.applyTo(parentView)
        }
    }

    private fun convertDpToPixel(dp: Float, context: Context): Int {
        return (dp * (context.resources
            .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}