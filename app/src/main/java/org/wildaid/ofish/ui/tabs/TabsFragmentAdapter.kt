package org.wildaid.ofish.ui.tabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsFragmentAdapter(
    private val fragmentFactory: TabsFragmentFactory,
    fm: FragmentManager,
    lc: Lifecycle
) : FragmentStateAdapter(fm, lc) {
    override fun getItemCount(): Int {
        return 8
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentFactory.createFragmentForPosition(position)
    }
}