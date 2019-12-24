# 🧗🏻‍♀️ Edge-to-Edge 
Android library for controlling edge-to-edge content and insetting views using simple DSL

```kotlin
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge {
            appbar.fit { Edge.Top }
            recycler.fit { Edge.Bottom }
        }
    }
```
