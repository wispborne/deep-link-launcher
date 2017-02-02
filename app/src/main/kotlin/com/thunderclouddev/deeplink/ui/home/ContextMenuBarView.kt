package com.thunderclouddev.deeplink.ui.home

import android.support.v4.content.res.ResourcesCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.ui.attribute
import com.thunderclouddev.deeplink.utils.tint
import org.jetbrains.anko.*

/**
 * A horizontal menu that responds to clicks.
 * Created by David Whitman on 15 Jan, 2017.
 */
class ContextMenuBarView(private val menu: Menu) : AnkoComponent<LinearLayout> {
    var onMenuItemClickListener: MenuItem.OnMenuItemClickListener? = null

    override fun createView(ui: AnkoContext<LinearLayout>): View {
        var i = 0

        return with(ui) {
            linearLayout {
                orientation = LinearLayout.HORIZONTAL

                while (i < menu.size()) {
                    val menuItem = menu.getItem(i)

                    imageView {
                        lparams {
                            height = dimen(R.dimen.spacingLarge)
                            width = dip(0)
                            weight = 1.0f
                        }

                        id = menuItem.itemId
                        image = menuItem.icon.tint(ResourcesCompat.getColor(ui.resources, R.color.white, ui.ctx.theme))
                        backgroundResource = context.attribute(android.R.attr.selectableItemBackgroundBorderless).resourceId
                        contentDescription = menuItem.title

                        onClick { onMenuItemClickListener?.onMenuItemClick(menuItem) }
                    }

                    i++
                }
            }
        }
    }
}