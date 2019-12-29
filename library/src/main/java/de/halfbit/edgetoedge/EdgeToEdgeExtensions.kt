package de.halfbit.edgetoedge

import android.app.Activity
import android.app.Dialog
import android.view.Window
import androidx.fragment.app.Fragment

inline fun Fragment.edgeToEdge(block: EdgeToEdge.() -> Unit) {
    val rootView = requireNotNull(view) { "fragment.view must not be null, fragment: $this" }
    val window = requireNotNull(activity?.window) { "fragment's activity must be not null" }
    EdgeToEdge(rootView, window).also(block).build()
}

inline fun Dialog.edgeToEdge(block: EdgeToEdge.() -> Unit) {
    val window = requireWindow()
    EdgeToEdge(window.decorView, window).also(block).build()
}

inline fun Activity.edgeToEdge(block: EdgeToEdge.() -> Unit) {
    val window = requireNotNull(window) { "Dialog's window must be not null" }
    EdgeToEdge(window.decorView, window).also(block).build()
}

fun Dialog.requireWindow() = requireNotNull(window) { "Dialog's window must be not null" }

fun Window.setEdgeToEdgeFlags() {
    with(decorView) {
        systemUiVisibility = systemUiVisibility or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}
