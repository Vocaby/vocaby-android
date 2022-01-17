package com.vocaby.searchview.util
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.appcompat.view.menu.*
import androidx.appcompat.widget.ListPopupWindow
import com.vocaby.searchview.R
import java.util.*

/**
 * Presents a menu as a small, simple popup anchored to another view.
 */
@SuppressLint("RestrictedApi")
class MenuPopupHelper(
    private val mContext: Context, menu: MenuBuilder, anchorView: View?,
    overflowOnly: Boolean, popupStyleAttr: Int, popupStyleRes: Int
) : AdapterView.OnItemClickListener, View.OnKeyListener, ViewTreeObserver.OnGlobalLayoutListener, PopupWindow.OnDismissListener,
    MenuPresenter {
    private val mInflater: LayoutInflater
    private val mMenu: MenuBuilder
    private val mAdapter: MenuAdapter
    private val mOverflowOnly: Boolean
    private val mPopupMaxWidth: Int
    private val mPopupStyleAttr: Int
    private val mPopupStyleRes: Int
    private var mAnchorView: View?
    private var mPopup: ListPopupWindow? = null
    private var mTreeObserver: ViewTreeObserver? = null
    private var mPresenterCallback: MenuPresenter.Callback? = null
    var mForceShowIcon = false
    private var mMeasureParent: ViewGroup? = null

    /** Whether the cached content width value is valid.  */
    private var mHasContentWidth = false

    /** Cached content width from [.measureContentWidth].  */
    private var mContentWidth = 0
    var gravity: Int = Gravity.NO_GRAVITY

    constructor(context: Context, menu: MenuBuilder) : this(
        context,
        menu,
        null,
        false,
        R.attr.popupMenuStyle
    )

    constructor(context: Context, menu: MenuBuilder, anchorView: View?) : this(
        context,
        menu,
        anchorView,
        false,
        R.attr.popupMenuStyle
    )

    constructor(
        context: Context, menu: MenuBuilder, anchorView: View?,
        overflowOnly: Boolean, popupStyleAttr: Int
    ) : this(context, menu, anchorView, overflowOnly, popupStyleAttr, 0)

    var mOffsetX = 0f
    var mOffsetY = 0f

    fun setForceShowIcon(forceShow: Boolean) {
        mForceShowIcon = forceShow
    }

    fun show() {
        check(tryShow()) { "MenuPopupHelper cannot be used without an anchor" }
    }

    val popup: ListPopupWindow? get() = mPopup

    private fun tryShow(): Boolean {
        mPopup = ListPopupWindow(mContext, null, mPopupStyleAttr, mPopupStyleRes)
        mPopup?.setOnDismissListener(this)
        mPopup?.setOnItemClickListener(this)
        mPopup?.setAdapter(mAdapter)
        mPopup?.isModal = true
        val anchor = mAnchorView
        if (anchor != null) {
            val addGlobalListener = mTreeObserver == null
            mTreeObserver = anchor.viewTreeObserver // Refresh to latest
            if (addGlobalListener) mTreeObserver?.addOnGlobalLayoutListener(this)
            mPopup?.anchorView = anchor
            mPopup?.setDropDownGravity(gravity)
        } else {
            return false
        }
        if (!mHasContentWidth) {
            mContentWidth = measureContentWidth()
            mHasContentWidth = true
        }
        mPopup?.setContentWidth(mContentWidth)
        mPopup?.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        val vertOffset = -mAnchorView!!.height + Util.dpToPx(4)
        val horizontalOffset = -mContentWidth + mAnchorView!!.width
        mPopup?.verticalOffset = vertOffset
        mPopup?.horizontalOffset = horizontalOffset
        mPopup?.show()
        mPopup?.listView?.setOnKeyListener(this)
        return true
    }

    fun dismiss() {
        if (isShowing) {
            mPopup?.dismiss()
        }
    }

    override fun onDismiss() {
        mPopup = null
        mMenu.close()
        if (mTreeObserver != null) {
            if (!mTreeObserver!!.isAlive) mTreeObserver = mAnchorView!!.viewTreeObserver
            mTreeObserver?.removeOnGlobalLayoutListener(this)
            mTreeObserver = null
        }
    }

    val isShowing: Boolean
        get() = mPopup != null && mPopup?.isShowing == true

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val adapter = mAdapter
        adapter.mAdapterMenu.performItemAction(adapter.getItem(position), 0)
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
            dismiss()
            return true
        }
        return false
    }

    private fun measureContentWidth(): Int {
        // Menus don't tend to be long, so this is more sane than it looks.
        var maxWidth = 0
        var itemView: View? = null
        var itemType = 0
        val adapter: ListAdapter? = mAdapter
        val widthMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val count = adapter!!.count
        for (i in 0 until count) {
            val positionType = adapter.getItemViewType(i)
            if (positionType != itemType) {
                itemType = positionType
                itemView = null
            }
            if (mMeasureParent == null) {
                mMeasureParent = FrameLayout(mContext)
            }
            itemView = adapter.getView(i, itemView, mMeasureParent)
            itemView.measure(widthMeasureSpec, heightMeasureSpec)
            val itemWidth = itemView.measuredWidth
            if (itemWidth >= mPopupMaxWidth) {
                return mPopupMaxWidth
            } else if (itemWidth > maxWidth) {
                maxWidth = itemWidth
            }
        }
        return maxWidth
    }

    override fun onGlobalLayout() {
        if (isShowing) {
            val anchor = mAnchorView
            if (anchor == null || !anchor.isShown) {
                dismiss()
            } else if (isShowing) {
                // Recompute window size and position
                mPopup?.show()
            }
        }
    }

    override fun initForMenu(context: Context?, menu: MenuBuilder?) {
        // Don't need to do anything; we added as a presenter in the constructor.
    }

    override fun getMenuView(root: ViewGroup?): androidx.appcompat.view.menu.MenuView? {
        throw UnsupportedOperationException("MenuPopupHelpers manage their own views")
    }

    override fun updateMenuView(cleared: Boolean) {
        mHasContentWidth = false
        mAdapter.notifyDataSetChanged()
    }

    override fun setCallback(cb: MenuPresenter.Callback?) {
        mPresenterCallback = cb
    }

    override fun onSubMenuSelected(subMenu: SubMenuBuilder): Boolean {
        if (subMenu.hasVisibleItems()) {
            val subPopup = MenuPopupHelper(mContext, subMenu, mAnchorView)
            subPopup.setCallback(mPresenterCallback)
            var preserveIconSpacing = false
            val count: Int = subMenu.size()
            for (i in 0 until count) {
                val childItem: MenuItem = subMenu.getItem(i)
                if (childItem.isVisible && childItem.icon != null) {
                    preserveIconSpacing = true
                    break
                }
            }
            subPopup.setForceShowIcon(preserveIconSpacing)
            if (subPopup.tryShow()) {
                mPresenterCallback?.onOpenSubMenu(subMenu)
                return true
            }
        }
        return false
    }

    override fun onCloseMenu(menu: MenuBuilder, allMenusAreClosing: Boolean) {
        // Only care about the (sub)menu we're presenting.
        if (menu !== mMenu) return
        dismiss()
        mPresenterCallback?.onCloseMenu(menu, allMenusAreClosing)
    }

    override fun flagActionItems(): Boolean {
        return false
    }

    override fun expandItemActionView(menu: MenuBuilder?, item: MenuItemImpl?): Boolean {
        return false
    }

    override fun collapseItemActionView(menu: MenuBuilder?, item: MenuItemImpl?): Boolean {
        return false
    }

    override fun getId(): Int {
        return 0
    }

    override fun onSaveInstanceState(): Parcelable? {
        return null
    }

    override fun onRestoreInstanceState(state: Parcelable?) {}
    private inner class MenuAdapter(menu: MenuBuilder) : BaseAdapter() {
        val mAdapterMenu: MenuBuilder = menu

        private var mExpandedIndex = -1

        override fun getCount(): Int {
            val items: ArrayList<MenuItemImpl> =
                if (mOverflowOnly) mAdapterMenu.nonActionItems else mAdapterMenu.visibleItems
            return if (mExpandedIndex < 0) {
                items.size
            } else items.size - 1
        }

        override fun getItem(position: Int): MenuItemImpl {
            var i = position
            val items: ArrayList<MenuItemImpl> =
                if (mOverflowOnly) mAdapterMenu.nonActionItems else mAdapterMenu.visibleItems
            if (mExpandedIndex in 0..i) {
                i++
            }
            return items[i]
        }

        override fun getItemId(position: Int): Long {
            // Since a menu item's ID is optional, we'll use the position as an
            // ID for the item in the AdapterView
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val view = convertView
            val itemView: MenuView.ItemView = view as MenuView.ItemView
            if (mForceShowIcon) {
                (view as ListMenuItemView).setForceShowIcon(true)
            }
            itemView.initialize(getItem(position), 0)
            return view
        }

        fun findExpandedIndex() {
            val expandedItem: MenuItemImpl = mMenu.expandedItem
            val items: ArrayList<MenuItemImpl> = mMenu.nonActionItems
            val count = items.size
            for (i in 0 until count) {
                val item: MenuItemImpl = items[i]
                if (item === expandedItem) {
                    mExpandedIndex = i
                    return
                }
            }
            mExpandedIndex = -1
        }

        override fun notifyDataSetChanged() {
            findExpandedIndex()
            super.notifyDataSetChanged()
        }

        init {
            findExpandedIndex()
        }
    }

    companion object {
        private const val TAG = "MenuPopupHelper"
        val ITEM_LAYOUT: Int = R.layout.abc_popup_menu_item_layout
    }

    init {
        mInflater = LayoutInflater.from(mContext)
        mMenu = menu
        mAdapter = MenuAdapter(mMenu)
        mOverflowOnly = overflowOnly
        mPopupStyleAttr = popupStyleAttr
        mPopupStyleRes = popupStyleRes
        val res = mContext.resources
        mPopupMaxWidth = Math.max(
            res.displayMetrics.widthPixels / 2,
            res.getDimensionPixelSize(R.dimen.abc_config_prefDialogWidth)
        )
        mAnchorView = anchorView
        // Present the menu using our context, not the menu builder's context.
        menu.addMenuPresenter(this, mContext)
    }
}