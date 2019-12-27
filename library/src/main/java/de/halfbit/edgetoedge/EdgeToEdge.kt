package de.halfbit.edgetoedge

import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Space
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@DslMarker
annotation class EdgeToEdgeDsl

@EdgeToEdgeDsl
class EdgeToEdge(
    private val rootView: View,
    private val window: Window
) {
    private val fittings: MutableList<Fitting> = mutableListOf()

    fun View.fit(block: FittingBuilder.() -> Edge) {
        FittingBuilder(
            adjustment = if (this is Space) Adjustment.Height else Adjustment.Padding,
            clipToPadding = if (this is ScrollingView && this is ViewGroup) false else null,
            consumeInsets = false
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
        if (rootView.getTag(R.id.edgetoedge) == null) {
            window.enableEdgeToEdge()
            rootView.setTag(R.id.edgetoedge, Unit)
        }

        rootView.onApplyWindowInsets { insets ->
            var consumeTop = false
            var consumeBottom = false
            for (fitting in fittings) {
                with(fitting) {
                    when (edge) {
                        Edge.Top -> {
                            consumeTop = consumeInsets
                            when (adjustment) {
                                Adjustment.Padding -> applyTopInsetAsPadding(insets)
                                Adjustment.Margin -> applyTopInsetAsMargin(insets)
                                Adjustment.Height -> applyTopInsetAsHeight(insets)
                            }
                        }
                        Edge.Bottom -> {
                            consumeBottom = consumeInsets
                            when (adjustment) {
                                Adjustment.Padding -> applyBottomInsetAsPadding(insets)
                                Adjustment.Margin -> applyBottomInsetAsMargin(insets)
                                Adjustment.Height -> applyBottomInsetAsHeight(insets)
                            }
                        }
                        Edge.TopBottom -> {
                            consumeTop = consumeInsets
                            consumeBottom = consumeInsets
                            when (fitting.adjustment) {
                                Adjustment.Padding -> applyTopAndBottomInsetsAsPadding(insets)
                                Adjustment.Margin -> applyTopAndBottomInsetAsMargin(insets)
                                Adjustment.Height -> error(
                                    "Height adjustment can only be used either Top or Bottom."
                                )
                            }
                        }
                    }
                }
            }

            if (consumeTop || consumeBottom) {
                insets.replaceSystemWindowInsets(
                    insets.systemWindowInsetLeft,
                    if (consumeTop) 0 else insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    if (consumeBottom) 0 else insets.systemWindowInsetBottom
                )
            } else insets
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

                paddingTop = view.paddingTop
                paddingBottom = view.paddingBottom

                val layoutMargin = view.layoutParams as? ViewGroup.MarginLayoutParams
                layoutMargin?.let {
                    marginTop = it.topMargin
                    marginBottom = it.bottomMargin
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> block(insets) }
        if (isAttachedToWindow) ViewCompat.requestApplyInsets(this)
        else addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(view: View) {}
                override fun onViewAttachedToWindow(view: View) {
                    view.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(view)
                }
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
        @Suppress("UNUSED_PARAMETER")
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
    var paddingTop: Int = 0
    var paddingBottom: Int = 0
    var marginTop: Int = 0
    var marginBottom: Int = 0
}

enum class Adjustment { Padding, Margin, Height }

private fun Fitting.applyTopInsetAsPadding(insets: WindowInsetsCompat) {
    val top = paddingTop + insets.systemWindowInsetTop
    if (view.paddingTop != top) view.setPadding(
        view.paddingLeft, top, view.paddingRight, view.paddingBottom
    )
}

private fun Fitting.applyBottomInsetAsPadding(insets: WindowInsetsCompat) {
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopAndBottomInsetsAsPadding(insets: WindowInsetsCompat) {
    val top = paddingTop + insets.systemWindowInsetTop
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingTop != top || view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, top, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopInsetAsMargin(insets: WindowInsetsCompat) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    if (top != layoutParams.topMargin) {
        layoutParams.topMargin = top
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyBottomInsetAsMargin(insets: WindowInsetsCompat) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (bottom != layoutParams.bottomMargin) {
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyTopAndBottomInsetAsMargin(insets: WindowInsetsCompat) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (top != layoutParams.topMargin || bottom != layoutParams.bottomMargin) {
        layoutParams.topMargin = top
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyTopInsetAsHeight(insets: WindowInsetsCompat) {
    if (view.height != insets.systemWindowInsetTop) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetTop, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyBottomInsetAsHeight(insets: WindowInsetsCompat) {
    if (view.height != insets.systemWindowInsetBottom) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetBottom, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}

private fun Window.enableEdgeToEdge() {
    with(decorView) {
        systemUiVisibility = systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}
