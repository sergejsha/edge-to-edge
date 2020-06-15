package de.halfbit.edgetoedge.sample.examples

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.R

class FullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)
        edgeToEdge {
            findViewById<View>(R.id.message).fit { Edge.BottomArc }
        }
    }
}
