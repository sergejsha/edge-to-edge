package de.halfbit.edgetoedge.sample.examples.issue4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.R
import kotlinx.android.synthetic.main.activity_issue4_coordinator.*

class Issue4CoordinatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue4_coordinator)
        edgeToEdge {
            coordinatorContainer.fit { Edge.TopArc }
        }
    }
}
