package de.halfbit.edgetoedge

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@DslMarker
annotation class EdgeToEdgeDsl

@EdgeToEdgeDsl
class EdgeToEdge(
    private val rootView: View
) {
    private val fittings: MutableList<Fitting> = mutableListOf()

    fun View.fit(block: FittingBuilder.() -> Edge) {
        FittingBuilder(
            adjustment = if (this is Space) Adjustment.Height else Adjustment.Padding,
            clipToPadding = if (this is ScrollingView && this is ViewGroup) false else null,
            consumeInsets = true
        ).also {
            fittings += Fitting(
                view = this,
                adjustment = it.adjustment,
                edge = block(it),
                clipToPadding = it.clipToPadding,
                consumeInsets = it.consumeInsets
            )
        }
    }

    @PublishedApi
    internal fun build() {
        rootView.onApplyWindowInsets { insets ->
            var consumeTop = false
            var consumeBottom = false
            for (fitting in fittings) {
                with(fitting) {
                    when (edge) {
                        Edge.Top -> {
                            consumeTop = consumeInsets
                            when (adjustment) {
                                Adjustment.Padding ->
                                    view.applyTopInsetAsPadding(padding, insets)
                                Adjustment.Height ->
                                    view.applyTopInsetAsHeight(insets)
                            }
                        }
                        Edge.Bottom -> {
                            consumeBottom = consumeInsets
                            when (adjustment) {
                                Adjustment.Padding ->
                                    view.applyBottomInsetAsPadding(padding, insets)
                                Adjustment.Height ->
                                    view.applyBottomInsetAsHeight(insets)
                            }
                        }
                        Edge.TopBottom -> {
                            consumeTop = consumeInsets
                            consumeBottom = consumeInsets
                            when (fitting.adjustment) {
                                Adjustment.Padding ->
                                    view.applyTopAndBottomInsetsAsPadding(padding, insets)
                                Adjustment.Height -> error(
                                    "Height adjustment can only be used either Top or Bottom."
                                )
                            }
                        }
                    }
                }
            }
            insets.replaceSystemWindowInsets(
                insets.systemWindowInsetLeft,
                if (consumeTop) 0 else insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                if (consumeBottom) 0 else insets.systemWindowInsetBottom
            )
        }
    }

    private fun View.onApplyWindowInsets(
        block: (insets: WindowInsetsCompat) -> WindowInsetsCompat
    ) {
        for (fitting in fittings) {
            with(fitting) {
                clipToPadding?.let {
                    check(view is ViewGroup) {
                        "'clipToPadding' can only be applied to ViewGroup, actual: $this"
                    }
                    view.clipToPadding = it
                }
                padding = Rect(
                    view.paddingLeft,
                    view.paddingTop,
                    view.paddingRight,
                    view.paddingBottom
                )
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> block(insets) }
        if (isAttachedToWindow) ViewCompat.requestApplyInsets(this)
        else addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    view.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(view)
                }

                override fun onViewDetachedFromWindow(view: View) = Unit
            }
        )
    }
}

@EdgeToEdgeDsl
class FittingBuilder(
    var adjustment: Adjustment,
    var clipToPadding: Boolean?,
    var consumeInsets: Boolean
)

sealed class Edge {
    object Top : Edge() {
        operator fun plus(bottom: Bottom): Edge = TopBottom
    }

    object Bottom : Edge()
    internal object TopBottom : Edge()
}

internal data class Fitting(
    val view: View,
    val adjustment: Adjustment,
    val edge: Edge,
    val clipToPadding: Boolean?,
    val consumeInsets: Boolean
) {
    lateinit var padding: Rect
}

enum class Adjustment { Padding, Height }

private fun View.applyTopInsetAsPadding(padding: Rect, insets: WindowInsetsCompat) {
    setPadding(
        padding.left,
        padding.top + insets.systemWindowInsetTop,
        padding.right,
        padding.bottom
    )
}

private fun View.applyBottomInsetAsPadding(padding: Rect, insets: WindowInsetsCompat) {
    setPadding(
        padding.left,
        padding.top,
        padding.right,
        padding.bottom + insets.systemWindowInsetBottom
    )
}

private fun View.applyTopAndBottomInsetsAsPadding(padding: Rect, insets: WindowInsetsCompat) {
    setPadding(
        padding.left,
        padding.top + insets.systemWindowInsetTop,
        padding.right,
        padding.bottom + insets.systemWindowInsetBottom
    )
}

private fun View.applyTopInsetAsHeight(insets: WindowInsetsCompat) {
    if (height != insets.systemWindowInsetTop) {
        val params = this.layoutParams
        params.height = View.MeasureSpec
            .makeMeasureSpec(insets.systemWindowInsetTop, View.MeasureSpec.EXACTLY)
        this.layoutParams = params
    }
}

private fun View.applyBottomInsetAsHeight(insets: WindowInsetsCompat) {
    if (height != insets.systemWindowInsetBottom) {
        val params = this.layoutParams
        params.height = View.MeasureSpec
            .makeMeasureSpec(insets.systemWindowInsetBottom, View.MeasureSpec.EXACTLY)
        this.layoutParams = params
    }
}
