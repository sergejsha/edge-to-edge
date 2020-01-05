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
     * Fits the view to the returned [Edge] of the screen by adjusting its `padding`,
     * `margin` or `height`. The function detects the type of [Adjustment] and whether
     * `clipToPadding` should be applied to the view as following.
     *
     * - for [android.widget.Space] the adjustment is [Adjustment.Height]
     * - for [android.widget.Button] or [android.widget.ImageButton] the adjustment
     * is [Adjustment.Margin]
     * - for any other widget the adjustment is [Adjustment.Padding]
     * - `clipToPadding` is disabled for the [android.widget.ScrollView] and instances
     * of [androidx.core.view.ScrollingView], and not changed for all the other widgets.
     *
     * Default values can be overridden inside the `block` lambda function.
     */
    fun View.fit(block: FittingBuilder.() -> Edge) {
        FittingBuilder(
            adjustment = detectAdjustment(),
            clipToPadding = detectClipToPadding()
        ).also { builder ->
            val edge = builder.block()
            val adjustment = builder.adjustment
            val clipToPadding = builder.clipToPadding
            verifyEdgeAdjustment(edge, adjustment)
            applyClipToPadding(clipToPadding)
            edgeToEdge.fittings[this] = createFitting(edge, adjustment, clipToPadding)
        }
    }

    /** Same as [fit] but overrides default adjustment to [Adjustment.Padding]. */
    inline fun View.fitPadding(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Padding
            block()
        }
    }

    /** Same as [fit] but overrides default adjustment to [Adjustment.Margin]. */
    inline fun View.fitMargin(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Margin
            block()
        }
    }

    /** Same as [fit] but overrides default adjustment to [Adjustment.Height]. */
    inline fun View.fitHeight(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Height
            block()
        }
    }

    /** Removes fitting rule for the view. */
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
                    when (adjustment) {
                        Adjustment.Padding -> applyInsetsAsPadding(insets, view, edge.flags)
                        Adjustment.Margin -> applyInsetsAsMargin(insets, view, edge.flags)
                        Adjustment.Height -> applyInsetsAsHeight(insets, view, edge.flags)
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

private const val FLAG_LEFT = 1
private const val FLAG_TOP = 1.shl(1)
private const val FLAG_RIGHT = 1.shl(2)
private const val FLAG_BOTTOM = 1.shl(3)

sealed class Edge(
    internal val flags: Int
) {
    object Left : Edge(FLAG_LEFT)
    object Top : Edge(FLAG_TOP)
    object Right : Edge(FLAG_RIGHT)
    object Bottom : Edge(FLAG_BOTTOM)
    internal class CompositeEdge(edges: Int) : Edge(edges)

    operator fun plus(edge: Edge): Edge =
        CompositeEdge(this.flags + edge.flags)
}

enum class Adjustment { Padding, Margin, Height }

private data class Fitting(
    val view: WeakReference<View>,
    val adjustment: Adjustment,
    val edge: Edge,
    val clipToPadding: Boolean?,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int,
    val marginLeft: Int,
    val marginTop: Int,
    val marginRight: Int,
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

private fun View.verifyEdgeAdjustment(edge: Edge, adjustment: Adjustment) {
    if (adjustment == Adjustment.Height && edge is Edge.CompositeEdge) {
        val edges = StringBuilder()
        if (edge.flags and FLAG_LEFT > 0) edges.append(", Left")
        if (edge.flags and FLAG_TOP > 0) edges.append(", Top")
        if (edge.flags and FLAG_RIGHT > 0) edges.append(", Right")
        if (edge.flags and FLAG_BOTTOM > 0) edges.append(", Bottom")
        throw IllegalArgumentException(
            "Height adjustment can only be applied to a single edge." +
                    " Actual edges: ${edges.substring(2)}, View: $this"
        )
    }
}

private fun Fitting.applyInsetsAsPadding(insets: WindowInsetsCompat, view: View, flags: Int) {
    val left = if (flags and FLAG_LEFT > 0)
        paddingLeft + insets.systemWindowInsetLeft else view.paddingLeft
    val top = if (flags and FLAG_TOP > 0)
        paddingTop + insets.systemWindowInsetTop else view.paddingTop
    val right = if (flags and FLAG_RIGHT > 0)
        paddingRight + insets.systemWindowInsetRight else view.paddingRight
    val bottom = if (flags and FLAG_BOTTOM > 0)
        paddingBottom + insets.systemWindowInsetBottom else view.paddingBottom

    if (view.paddingLeft != left ||
        view.paddingTop != top ||
        view.paddingRight != right ||
        view.paddingBottom != bottom
    ) view.setPadding(left, top, right, bottom)
}

private fun Fitting.applyInsetsAsMargin(insets: WindowInsetsCompat, view: View, flags: Int) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams

    val left = if (flags and FLAG_LEFT > 0)
        marginLeft + insets.systemWindowInsetLeft else layoutParams.leftMargin
    val top = if (flags and FLAG_TOP > 0)
        marginTop + insets.systemWindowInsetTop else layoutParams.topMargin
    val right = if (flags and FLAG_RIGHT > 0)
        marginRight + insets.systemWindowInsetRight else layoutParams.rightMargin
    val bottom = if (flags and FLAG_BOTTOM > 0)
        marginBottom + insets.systemWindowInsetBottom else layoutParams.bottomMargin

    if (left != layoutParams.leftMargin ||
        top != layoutParams.topMargin ||
        right != layoutParams.rightMargin ||
        bottom != layoutParams.bottomMargin
    ) {
        layoutParams.leftMargin = left
        layoutParams.topMargin = top
        layoutParams.rightMargin = right
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun applyInsetsAsHeight(insets: WindowInsetsCompat, view: View, flags: Int) {
    val height = when (flags) {
        FLAG_LEFT -> insets.systemWindowInsetLeft
        FLAG_TOP -> insets.systemWindowInsetTop
        FLAG_RIGHT -> insets.systemWindowInsetRight
        FLAG_BOTTOM -> insets.systemWindowInsetBottom
        else -> error("Unexpected edge flags: $flags")
    }
    if (view.height != height) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            height, View.MeasureSpec.EXACTLY
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
        paddingLeft = paddingLeft,
        paddingTop = paddingTop,
        paddingRight = paddingRight,
        paddingBottom = paddingBottom,
        marginLeft = layoutMargin?.leftMargin ?: 0,
        marginTop = layoutMargin?.topMargin ?: 0,
        marginRight = layoutMargin?.rightMargin ?: 0,
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
