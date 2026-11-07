package com.example.register

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView inside a ScrollView only lays out a few rows by default.
 * Measure every item (including margins) and set the full height so all rows are visible.
 */
fun RecyclerView.expandToFitAllItems() {
    val listAdapter = adapter ?: return
    if (listAdapter.itemCount == 0) return

    post {
        val parentView = parent as? View
        val availableWidth = when {
            width > 0 -> width - paddingLeft - paddingRight
            parentView != null -> parentView.width - paddingLeft - paddingRight
            else -> resources.displayMetrics.widthPixels
        }
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            availableWidth.coerceAtLeast(0),
            View.MeasureSpec.EXACTLY,
        )

        var totalHeight = paddingTop + paddingBottom
        for (index in 0 until listAdapter.itemCount) {
            val viewType = listAdapter.getItemViewType(index)
            val holder = listAdapter.createViewHolder(this, viewType)
            listAdapter.bindViewHolder(holder, index)
            holder.itemView.measure(
                widthSpec,
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )

            val margins = when (val params = holder.itemView.layoutParams) {
                is ViewGroup.MarginLayoutParams -> params.topMargin + params.bottomMargin
                else -> 0
            }
            totalHeight += holder.itemView.measuredHeight + margins
        }

        layoutParams = layoutParams.apply {
            height = totalHeight
        }
        requestLayout()
    }
}
