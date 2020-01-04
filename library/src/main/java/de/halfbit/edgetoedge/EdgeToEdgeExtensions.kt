package de.halfbit.edgetoedge

import android.app.Activity
import android.app.Dialog
import android.view.Window
import androidx.fragment.app.Fragment

/**
 * Declares a set of view fitting rules and requests [android.view.WindowInsets] to be
 * applied to the view hierarchy. The library will fit the views according to their
 * fitting rules each time [WindowInsets] are applied. The set is usually declared in
 * the `Fragment.onViewCreated()` callback, but it can be re-declared at any time later,
 * for instance after applying a [ConstraintSet]. Each declaration adds new or overwrites
 * already existing view fitting rules. For removing a fitting rule [EdgeToEdgeBuilder.unfit]
 * method can be used.
 *
 * ```
 * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *     super.onViewCreated(view, savedInstanceState)
 *     edgeToEdge {
 *         appbar.fit { Edge.Top }
 *         recycler.fit { Edge.Bottom }
 *     }
 * }
 * ```
 */
inline fun Fragment.edgeToEdge(block: EdgeToEdgeBuilder.() -> Unit) {
    val window = requireNotNull(activity?.window) { "fragment's activity must be not null" }
    EdgeToEdgeBuilder(requireView(), window).also(block).build()
}

/**
 * Forces previously declared in [edgeToEdge] block views to fit the edges again
 * by re-applying their fitting rules. This function can be called, for example, after
 * applying a new `ConstraintSet` to an instance of `ConstraintLayout`.
 */
fun Fragment.fitEdgeToEdge() {
    requireView().dispatchWindowInsets()
}

inline fun Dialog.edgeToEdge(block: EdgeToEdgeBuilder.() -> Unit) {
    val window = requireNotNull(window) { "Dialog's window must be not null" }
    EdgeToEdgeBuilder(window.decorView, window).also(block).build()
}

inline fun Activity.edgeToEdge(block: EdgeToEdgeBuilder.() -> Unit) {
    val window = requireNotNull(window) { "Dialog's window must be not null" }
    EdgeToEdgeBuilder(window.decorView, window).also(block).build()
}

fun Window.setEdgeToEdgeFlags() {
    with(decorView) {
        systemUiVisibility = systemUiVisibility or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}
