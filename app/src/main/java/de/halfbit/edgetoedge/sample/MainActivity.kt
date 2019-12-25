package de.halfbit.edgetoedge.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.halfbit.edgetoedge.enableEdgeToEdgeWindow

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, MainFragment())
                .commit()
        }
        enableEdgeToEdgeWindow()
    }
}
