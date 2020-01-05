# 0.7 (04.01.2020)
* Improved detection of default `adjustment` and `clipToPading` values.
* `View.fitPadding`, `View.fitHeight` and `View.fitMargin` are inline methods now.
* More documentation added.

# 0.6 (02.01.2020)
* New `view.fitMargin`, `view.fitPadding` and `view.fitHeight` convenience methods.
* View fitting cannot consume window insets anymore. 
* View property is a weak reference in the fitting class.

# 0.5 (01.01.2020)
* New `Fragment.fitEdgeToEdge()` function.
* New `edgeToEdge { view.unfit() }` function.  
* New `ConstraintLayout + Transition` sample screen.
* `edgeToEdge {}` can be applied multiple times.
* Some documentation added.

# 0.4 (29.12.2019)
* New public `Window.setEdgeToEdgeFlags()`.
* New samples for `BottomSheetDialog` and `BottomSheetDialogFragment`. 

# 0.3 (27.12.2019)
* Edge-to-Edge system window flags are set automatically.
* New `fit { adjustment = Adjustment.Margin }`.
* Insets are not consumed by default, but can be consumed by setting `fit { consumeInsets = true }`.

# 0.2 (26.12.2019)
* New property `fit { consumeInsets: Boolean }` for suppressing insets consumption.
* New `Toolbar + ViewPager in ConstraintLayout` sample screen.

# 0.1 (26.12.2019)
* Initial release
