package de.halfbit.edgetoedge

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment

inline fun Fragment.edgeToEdge(block: EdgeToEdge.() -> Unit) {
    val rootView = requireNotNull(view) { "fragment.view must not be null, fragment: $this" }
    EdgeToEdge(rootView).also(block).build()
}

inline fun View.edgeToEdge(block: EdgeToEdge.() -> Unit) {
    EdgeToEdge(this).also(block).build()
}

fun Activity.enableEdgeToEdge() {
    window.enableEdgeToEdge()
}

fun Dialog.enableEdgeToEdge() {
    requireNotNull(window) { "Dialog must be attached to the Window" }.enableEdgeToEdge()
}

private fun Window.enableEdgeToEdge() {
    with(decorView) {
        systemUiVisibility = systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}
