# 📐 Edge-to-Edge 
Android library for controlling edge-to-edge content and insetting views using simple DSL

# How

1. Configure transparent colors of the status and navigation bars in `res/themes.xml`.

```xml
<resources>
    <style name="AppTheme" parent="Theme.MaterialComponents.Light.NoActionBar">
        ...
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
        <item name="android:windowTranslucentStatus">false</item>
        <item name="android:windowTranslucentNavigation">false</item>
        <item name="android:statusBarColor">@color/statusBar</item>
        <item name="android:navigationBarColor">@color/navigationBar</item>
    </style>
    
    <color name="statusBar">@android:color/transparent</color>
    <color name="navigationBar">@android:color/transparent</color>
</resources>
```

2. Enable edge-to-edge content flags in the Activity.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    enableEdgeToEdge()
    ...    
}
```

3. Fit top and bottom views of each fragment as UI design requires.

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    edgeToEdge {
        appbar.fit { Edge.Top }
        recycler.fit { Edge.Bottom }
    }
}
```

See sample app for more examples.
