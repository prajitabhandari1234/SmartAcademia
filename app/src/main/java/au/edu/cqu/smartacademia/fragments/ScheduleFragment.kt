package au.edu.cqu.smartacademia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ScheduleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val textView = TextView(requireContext())
        textView.text = "Smart Schedule will be added in Phase 6"
        textView.textSize = 20f
        textView.gravity = android.view.Gravity.CENTER
        return textView
    }
}