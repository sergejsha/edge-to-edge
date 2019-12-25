# 🌖 Edge-to-Edge 
Android library for controlling [edge-to-edge content](https://developer.android.com/guide/navigation/gesturenav) and insetting views using simple DSL

# How

1. Remove `android:fitSystemWindows` attrobute everywhere from layouts, if present.
2. Configure transparent colors of the status and navigation bars in `res/values/styles.xml`.

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

3. Enable edge-to-edge window for the Activity.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    enableEdgeToEdge()
    ...    
}
```

4. Fit top and bottom views of each fragment as UI design requires.

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    edgeToEdge {
        appbar.fit { Edge.Top }
        recycler.fit { Edge.Bottom }
    }
    ...
}
```

See sample app for more examples.
