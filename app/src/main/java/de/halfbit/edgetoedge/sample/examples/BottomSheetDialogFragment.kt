package de.halfbit.edgetoedge.sample.examples

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import de.halfbit.edgetoedge.setEdgeToEdgeFlags
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment as MaterialBSDFragment

class BottomSheetDialogFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_bottom_sheet_dialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDialog.setOnClickListener { showDialog() }
        showDialogFragment.setOnClickListener { showDialogFragment() }
    }

    private fun showDialog() {
        BottomSheetDialog(requireContext()).apply {
            layoutInflater
                .inflate(R.layout.fragment_bottom_sheet_dialog_options, null)
                .also { optionsView ->

                    listOf(
                        optionsView.findViewById<View>(R.id.option1),
                        optionsView.findViewById<View>(R.id.option2),
                        optionsView.findViewById<View>(R.id.option3)
                    ).forEach {
                        it.setOnClickListener { dismiss() }
                    }

                    setCancelable(true)
                    setContentView(optionsView)

                    optionsView.unfitSystemWindow()
                    edgeToEdge {
                        optionsView.fit { Edge.Bottom }
                    }
                }
        }.show()
    }

    private fun showDialogFragment() {
        BSDFragment().show(requireFragmentManager(), null)
    }
}

class BSDFragment : MaterialBSDFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_bottom_sheet_dialog_options, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        requireNotNull(dialog.window).setEdgeToEdgeFlags()
        return dialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val optionsView = checkNotNull(view)

        listOf(
            optionsView.findViewById<View>(R.id.option1),
            optionsView.findViewById<View>(R.id.option2),
            optionsView.findViewById<View>(R.id.option3)
        ).forEach {
            it.setOnClickListener { dismiss() }
        }

        optionsView.unfitSystemWindow()
        edgeToEdge {
            optionsView.fit { Edge.Bottom }
        }
    }
}

private fun View.unfitSystemWindow() {
    // disable `fitsSystemWindows` in `material/design_bottom_sheet_dialog.xml`
    with(parent?.parent as View) {
        fitsSystemWindows = false
        (parent as View).fitsSystemWindows = false
    }
}