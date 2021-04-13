package com.start3a.ishowyou.data

import android.content.Context
import android.util.AttributeSet
import com.hoanganhtuan95ptit.draggable.DraggablePanel
import com.hoanganhtuan95ptit.draggable.utils.inflate
import com.hoanganhtuan95ptit.draggable.utils.reWidth
import com.start3a.ishowyou.R
import kotlinx.android.synthetic.main.layout_draggable_top.view.*

class DraggableRoom @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : DraggablePanel(context, attrs, defStyleAttr) {

    var mWidthWhenMax = 0
    var mWidthWhenMiddle = 0
    var mWidthWhenMin = 0

    init {
        getFrameFirst().addView(inflate(R.layout.layout_draggable_top))
        getFrameSecond().addView(inflate(R.layout.layout_draggable_bottom))
    }

    override fun initFrame() {
        mWidthWhenMax = width

        mWidthWhenMiddle = (width - mPercentWhenMiddle * mMarginEdgeWhenMin).toInt()

        mWidthWhenMin = mHeightWhenMin * 22 / 9

        super.initFrame()
    }

    override fun refreshFrameFirst() {
        super.refreshFrameFirst()

        val width = if (mCurrentPercent < mPercentWhenMiddle) {
            (mWidthWhenMax - (mWidthWhenMax - mWidthWhenMiddle) * mCurrentPercent)
        } else {
            (mWidthWhenMiddle - (mWidthWhenMiddle - mWidthWhenMin) * (mCurrentPercent - mPercentWhenMiddle) / (1 - mPercentWhenMiddle))
        }

        frameTop.reWidth(width.toInt())
    }

    fun resizeFraneFirstWidth(w: Int) {
        getFrameFirst().content_frame.reWidth(w)
    }
}