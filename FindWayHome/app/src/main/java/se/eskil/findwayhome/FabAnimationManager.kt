/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.animation.Animator
import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabAnimationManager(val activity: Activity) {
    public interface ClickListener {
        fun onAboutClicked()
        fun onSetDestinationClicked()
        fun onFabOpened()
    }

    private lateinit var fab: FloatingActionButton
    private lateinit var fabBGLayout: View
    private lateinit var fabAboutLayout: LinearLayout
    private lateinit var fabSetDestinationLayout: LinearLayout
    private lateinit var fabAbout: FloatingActionButton
    private lateinit var fabSetDestination: FloatingActionButton

    public var isOpen = false

    public fun enableFab(clickListener: ClickListener) {
        fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        fabBGLayout = activity.findViewById<View>(R.id.fabBGLayout)
        fabAboutLayout = activity.findViewById<LinearLayout>(R.id.fabAboutLayout)
        fabAbout = activity.findViewById<FloatingActionButton>(R.id.fabAbout)
        fabSetDestinationLayout = activity.findViewById<LinearLayout>(R.id.fabSetDestinationLayout)
        fabSetDestination = activity.findViewById<FloatingActionButton>(R.id.fabSetDestination)


        fab.setOnClickListener {
            if (View.GONE == fabBGLayout.visibility) {
                showFABMenu()
                clickListener.onFabOpened()
            } else {
                closeFABMenu()
            }
        }

        fabBGLayout.setOnClickListener { closeFABMenu() }

        fabAbout.setOnClickListener {
            clickListener.onAboutClicked()
            closeFABMenu()}

        fabSetDestination.setOnClickListener {
            clickListener.onSetDestinationClicked()
            closeFABMenu()
        }
    }

    public fun disableFab() {
        closeFABMenu()
    }

    private fun showFABMenu() {
        fabAboutLayout.visibility = View.VISIBLE
        fabSetDestinationLayout.visibility = View.VISIBLE
        fabBGLayout.visibility = View.VISIBLE
        fab.animate().rotationBy(180F)
        fabAboutLayout.animate().translationY(-activity.resources.getDimension(R.dimen.standard_60))
        fabSetDestinationLayout.animate().translationY(-activity.resources.getDimension(R.dimen.standard_120))
        isOpen = true

    }

    private fun closeFABMenu() {
        fabBGLayout.visibility = View.GONE
        fab.animate().rotation(0F)
        fabAboutLayout.animate().translationY(0f)
        fabSetDestinationLayout.animate().translationY(0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        if (View.GONE == fabBGLayout.visibility) {
                            fabAboutLayout.visibility = View.GONE
                            fabSetDestinationLayout.visibility = View.GONE
                        }
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
        isOpen = false
    }


}