package de.halfbit.edgetoedge

import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Space
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.ref.WeakReference
import java.util.WeakHashMap

@DslMarker
annotation class EdgeToEdgeDsl

@EdgeToEdgeDsl
class EdgeToEdgeBuilder(
    private val rootView: View,
    private val window: Window
) {
    private val edgeToEdge: EdgeToEdge =
        rootView.getTag(R.id.edgetoedge) as? EdgeToEdge
            ?: EdgeToEdge().also { rootView.setTag(R.id.edgetoedge, it) }

    /**
     * Returns the [Edge] of the screen, to which the view should aligned. The function
     * detects the type of [Adjustment] and whether the view should disable `clipToPadding`
     * as following:
     *
     * - for the [android.widget.Space], the adjustment is [Adjustment.Height]
     * - for a [android.widget.Button] or [android.widget.ImageButton],  the adjustment
     * is [Adjustment.Margin]
     * - for any other widget, the adjustment is  [Adjustment.Padding]
     * - `clipToPadding` is disabled for the [android.widget.ScrollView] and
     * any instances of [androidx.core.view.ScrollingView]
     *
     * The defaults above can be overridden inside the fit-block.
     */
    fun View.fit(block: FittingBuilder.() -> Edge) {
        FittingBuilder(
            adjustment = detectAdjustment(),
            clipToPadding = detectClipToPadding()
        ).also { builder ->
            val edge = builder.block()
            val adjustment = builder.adjustment
            val clipToPadding = builder.clipToPadding
            applyClipToPadding(clipToPadding)
            edgeToEdge.fittings[this] = createFitting(edge, adjustment, clipToPadding)
        }
    }

    /**
     * Same as [fit] but overriding default adjustment to [Adjustment.Padding].
     */
    inline fun View.fitPadding(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Padding
            block()
        }
    }

    /**
     * Same as [fit] but overriding default adjustment to [Adjustment.Margin].
     */
    inline fun View.fitMargin(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Margin
            block()
        }
    }

    /**
     * Same as [fit] but overriding default adjustment to [Adjustment.Height].
     */
    inline fun View.fitHeight(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Height
            block()
        }
    }

    /**
     * Removes fitting rule for the view.
     */
    fun View.unfit() {
        edgeToEdge.fittings.remove(this)
    }

    @PublishedApi
    internal fun build() {
        if (!window.hasEdgeToEdgeFlags()) {
            window.setEdgeToEdgeFlags()
        }

        rootView.onApplyWindowInsets { insets ->
            for (fitting in edgeToEdge.fittings.values) {
                val view = fitting.view.get() ?: continue
                with(fitting) {
                    when (edge) {
                        Edge.Top -> when (adjustment) {
                            Adjustment.Padding -> applyTopInsetAsPadding(insets, view)
                            Adjustment.Margin -> applyTopInsetAsMargin(insets, view)
                            Adjustment.Height -> applyTopInsetAsHeight(insets, view)
                        }
                        Edge.Bottom -> when (adjustment) {
                            Adjustment.Padding -> applyBottomInsetAsPadding(insets, view)
                            Adjustment.Margin -> applyBottomInsetAsMargin(insets, view)
                            Adjustment.Height -> applyBottomInsetAsHeight(insets, view)
                        }
                        Edge.TopBottom -> when (adjustment) {
                            Adjustment.Padding -> applyTopAndBottomInsetsAsPadding(insets, view)
                            Adjustment.Margin -> applyTopAndBottomInsetsAsMargin(insets, view)
                            Adjustment.Height -> error(
                                "Height adjustment can only be applied to either" +
                                        " Top or Bottom edge."
                            )
                        }
                    }
                }
            }
            insets
        }
    }

    private inline fun View.onApplyWindowInsets(
        crossinline block: (insets: WindowInsetsCompat) -> WindowInsetsCompat
    ) {
        if (!edgeToEdge.listening) {
            edgeToEdge.listening = true
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> block(insets) }
        }
        dispatchWindowInsets()
    }
}

@EdgeToEdgeDsl
class FittingBuilder(
    var adjustment: Adjustment,
    var clipToPadding: Boolean?
)

sealed class Edge {
    object Top : Edge() {
        @Suppress("UNUSED_PARAMETER")
        operator fun plus(bottom: Bottom): Edge = TopBottom
    }

    object Bottom : Edge()
    internal object TopBottom : Edge()
}

enum class Adjustment { Padding, Margin, Height }

private data class Fitting(
    val view: WeakReference<View>,
    val adjustment: Adjustment,
    val edge: Edge,
    val clipToPadding: Boolean?,
    val paddingTop: Int,
    val paddingBottom: Int,
    val marginTop: Int,
    val marginBottom: Int
)

private data class EdgeToEdge(
    val fittings: WeakHashMap<View, Fitting> = WeakHashMap(),
    var listening: Boolean = false
)

internal fun View.dispatchWindowInsets() {
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

private fun Fitting.applyTopInsetAsPadding(insets: WindowInsetsCompat, view: View) {
    val top = paddingTop + insets.systemWindowInsetTop
    if (view.paddingTop != top) view.setPadding(
        view.paddingLeft, top, view.paddingRight, view.paddingBottom
    )
}

private fun Fitting.applyBottomInsetAsPadding(insets: WindowInsetsCompat, view: View) {
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopAndBottomInsetsAsPadding(insets: WindowInsetsCompat, view: View) {
    val top = paddingTop + insets.systemWindowInsetTop
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingTop != top || view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, top, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopInsetAsMargin(insets: WindowInsetsCompat, view: View) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    if (top != layoutParams.topMargin) {
        layoutParams.topMargin = top
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyBottomInsetAsMargin(insets: WindowInsetsCompat, view: View) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (bottom != layoutParams.bottomMargin) {
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyTopAndBottomInsetsAsMargin(insets: WindowInsetsCompat, view: View) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (top != layoutParams.topMargin || bottom != layoutParams.bottomMargin) {
        layoutParams.topMargin = top
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun applyTopInsetAsHeight(insets: WindowInsetsCompat, view: View) {
    if (view.height != insets.systemWindowInsetTop) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetTop, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}

private fun applyBottomInsetAsHeight(insets: WindowInsetsCompat, view: View) {
    if (view.height != insets.systemWindowInsetBottom) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetBottom, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}

private fun View.detectAdjustment(): Adjustment =
    when {
        this is Space -> Adjustment.Height
        (this is Button || this is ImageButton) &&
                layoutParams is ViewGroup.MarginLayoutParams -> Adjustment.Margin
        else -> Adjustment.Padding
    }

private fun View.detectClipToPadding(): Boolean? =
    if (this is ScrollView || (this is ScrollingView && this is ViewGroup)) false else null

private fun View.createFitting(
    edge: Edge, adjustment: Adjustment, clipToPadding: Boolean?
): Fitting {
    val layoutMargin = layoutParams as? ViewGroup.MarginLayoutParams
    return Fitting(
        view = WeakReference(this),
        edge = edge,
        adjustment = adjustment,
        clipToPadding = clipToPadding,
        paddingTop = paddingTop,
        paddingBottom = paddingBottom,
        marginTop = layoutMargin?.topMargin ?: 0,
        marginBottom = layoutMargin?.bottomMargin ?: 0
    )
}

private fun View.applyClipToPadding(clipToPadding: Boolean?) {
    clipToPadding?.let {
        check(this is ViewGroup) {
            "'clipToPadding' can only be applied to a ViewGroup, actual: $this"
        }
        this.clipToPadding = it
    }
}

private fun Window.hasEdgeToEdgeFlags() =
    decorView.systemUiVisibility and
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) > 0
