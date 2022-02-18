package com.vocaby.vocabywidgets.searchview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import androidx.core.view.children
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator
import com.vocaby.vocabywidgets.R
import com.vocaby.vocabywidgets.searchview.suggestions.SearchSuggestionsAdapter
import com.vocaby.vocabywidgets.searchview.suggestions.model.SearchSuggestion
import com.vocaby.vocabywidgets.searchview.util.afterMeasured
import com.vocaby.vocabywidgets.searchview.util.view.SearchInputView
import com.vocaby.vocabywidgets.searchview.util.Util
import com.vocaby.vocabywidgets.searchview.util.adapter.GestureDetectorListenerAdapter
import com.vocaby.vocabywidgets.searchview.util.adapter.OnItemTouchListenerAdapter
import kotlin.math.abs

/**
 * A search UI widget that implements a floating search box also called persistent
 * search.
 */
class SearchView(context: Context, attrs: AttributeSet? = null): FrameLayout(context, attrs) {
    private var mHostActivity: Activity? = null
    private lateinit var mMainLayout: View
    private lateinit var mSearchViewBackgroundDrawable: Drawable

    private var mDimBackground = false
    private var mDismissOnOutsideTouch = true
    var isSearchBarFocused = false
    private var mFocusChangeListener: OnFocusChangeListener? = null
    private var mDismissFocusOnItemSelection = ATTRS_DISMISS_FOCUS_ON_ITEM_SELECTION_DEFAULT
    private lateinit var mSearchBar: CardView
    private var mSearchListener: OnSearchListener? = null
    private lateinit var mSearchInput: SearchInputView
    private var mCloseSearchOnSofteKeyboardDismiss = false
    private var mTitleText: String? = null
    private var mIsTitleSet = false
    private var mSearchInputTextColor = -1
    private var mSearchInputHintColor = -1
    private lateinit var mSearchInputParent: View

    private lateinit var mLeftAction: ImageView
    private lateinit var mSearchProgress: ProgressBar

    private lateinit var mIconBackArrow: Drawable
    private lateinit var mIconSearch: Drawable

    private var mSearchHint: String? = null
    private lateinit var mClearButton: ImageView
    private var mClearBtnColor = 0
    private lateinit var mIconClear: Drawable
    private var mBackgroundColor = 0
    private var mSkipQueryFocusChangeEvent = false
    private var mSkipTextChangeEvent = false
    private lateinit var mDivider: View
    private lateinit var mSuggestionsSection: ConstraintLayout
    private lateinit var mSuggestionListContainer: View
    private lateinit var mSuggestionsList: RecyclerView
    private lateinit var mSuggestionsAdapter: SearchSuggestionsAdapter
    private var mIsInitialLayout = true
    private var mIsSuggestionsSectionHeightSet = false
    private var mShowMoveUpSuggestion = ATTRS_SHOW_MOVE_UP_SUGGESTION_DEFAULT
    private var mSuggestionSectionAnimDuration: Long = 0

    private var mOnSuggestionsListHeightChanged: OnSuggestionsListHeightChanged? = null
    private var mOnClearSearchActionListener: OnClearSearchActionListener? = null
    private var mQueryListener: OnQueryChangeListener? = null
    var query: String = ""

    //An interface for implementing a listener that will get notified when the suggestions
    //section's height is set. This is to be used internally only.
    private interface OnSuggestionSecHeightSetListener {
        fun onSuggestionSecHeightSet()
    }

    private var mSuggestionSecHeightListener: OnSuggestionSecHeightSetListener? = null

    /**
     * Interface for implementing a listener to listen to
     * changes in the suggestion list height that occur when the list is expands/shrinks
     * following calls to [SearchView.swapSuggestions]
     */
    interface OnSuggestionsListHeightChanged {
        fun onSuggestionsListHeightChanged(newHeight: Float)
    }

    /**
     * Interface for implementing a listener to listen
     * to state changes in the query text.
     */
    interface OnQueryChangeListener {
        /**
         * Called when the query has changed. It will
         * be invoked when one or more characters in the
         * query was changed.
         *
         * @param oldQuery the previous query
         * @param newQuery the new query
         */
        fun onSearchTextChanged(oldQuery: String, newQuery: String)
    }

    /**
     * Interface for implementing a listener to listen
     * to when the current search has completed.
     */
    interface OnSearchListener {
        /**
         * Called when a suggestion was clicked indicating
         * that the current search has completed.
         *
         * @param searchSuggestion
         */
        fun onSuggestionClicked(searchSuggestion: SearchSuggestion)

        /**
         * Called when the current search has completed
         * as a result of pressing search key in the keyboard.
         *
         * @param currentQuery the text that is currently set in the query TextView
         */
        fun onSearchAction(currentQuery: String)
    }


    /**
     * Interface for implementing a listener to listen
     * to for focus state changes.
     */
    interface OnFocusChangeListener {
        /**
         * Called when the search bar has gained focus
         * and listeners are now active.
         */
        fun onFocus()

        /**
         * Called when the search bar has lost focus
         * and listeners are no more active.
         */
        fun onFocusCleared()
    }

    /**
     * Interface for implementing a callback to be
     * invoked when the clear search text action button
     * (the x to the right of the text) is clicked.
     */
    interface OnClearSearchActionListener {
        /**
         * Called when the clear search text button
         * was clicked.
         */
        fun onClearSearchClicked()
    }

    private fun init(attrs: AttributeSet?) {
        mHostActivity = Util.getHostActivity(context)
        val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        mMainLayout = layoutInflater.inflate(R.layout.search_view_floating_search_layout, this)
        mSearchViewBackgroundDrawable = ColorDrawable(Color.BLACK)
        mSearchBar = findViewById(R.id.search_query_section)
        mClearButton = findViewById(R.id.clear_btn)
        mSearchInput = findViewById(R.id.search_bar_text)
        mSearchInputParent = findViewById(R.id.search_input_parent)
        mLeftAction = findViewById(R.id.left_action)
        mSearchProgress = findViewById(R.id.search_bar_search_progress)
        initDrawables()

        mClearButton.setImageDrawable(mIconClear)
        mDivider = findViewById(R.id.divider)
        mSuggestionsSection = findViewById(R.id.search_suggestions_section)
        mSuggestionListContainer = findViewById(R.id.suggestions_list_container)
        mSuggestionsList = findViewById(R.id.suggestions_list)
        setupViews(attrs)
    }

    private fun initDrawables() {
        mIconClear = Util.getWrappedDrawable(context, R.drawable.ic_clear_black_24dp)
        mIconBackArrow = Util.getWrappedDrawable(context, R.drawable.ic_arrow_back_black_24dp)
        mIconSearch = Util.getWrappedDrawable(context, R.drawable.ic_search_black_24dp)
    }

    // Needed for config changes
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mIsInitialLayout) {
            //we need to add 5dp to the mSuggestionsSection because we are
            //going to move it up by 5dp in order to cover the search bar's
            //shadow padding and rounded corners. We also need to add an additional 10dp to
            //mSuggestionsSection in order to hide mSuggestionListContainer's
            //rounded corners and shadow for both, top and bottom.

            val addedHeight: Int = 3 * Util.dpToPx(
                SEARCH_CARD_VIEW_CORNERS_AND_TOP_BOTTOM_SHADOW_HEIGHT
            )
            val finalHeight: Int = mSuggestionsSection.height + addedHeight
            mSuggestionsSection.layoutParams.height = finalHeight
            mSuggestionsSection.requestLayout()
            val vto: ViewTreeObserver = mSuggestionListContainer.viewTreeObserver

            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (mSuggestionsSection.height == finalHeight) {
                        Util.removeGlobalLayoutObserver(mSuggestionListContainer, this)
                        mIsSuggestionsSectionHeightSet = true
                        moveSuggestListToInitialPos()
                        if (mSuggestionSecHeightListener != null) {
                            mSuggestionSecHeightListener!!.onSuggestionSecHeightSet()
                            mSuggestionSecHeightListener = null
                        }
                    }
                }
            })

            mIsInitialLayout = false
            refreshDimBackground()
        }
    }

    private fun setupViews(attrs: AttributeSet?) {
        mSuggestionsSection.isEnabled = false
        attrs?.let {
            applyXmlAttributes(it)
        }

        background = mSearchViewBackgroundDrawable

        setupQueryBar()

        if (!isInEditMode) { setupSuggestionSection() }
    }

    private fun applyXmlAttributes(attrs: AttributeSet) {
        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchView)

        try {
            val searchBarWidth: Int = attributes.getDimensionPixelSize(
                R.styleable.SearchView_searchBarWidth,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mSearchBar.layoutParams.width = searchBarWidth
            mDivider.layoutParams.width = searchBarWidth
            mSuggestionListContainer.layoutParams.width = searchBarWidth

            val searchBarLeftMargin: Int = attributes.getDimensionPixelSize(
                R.styleable.SearchView_searchBarMarginLeft,
                ATTRS_SEARCH_BAR_MARGIN_DEFAULT
            )

            val searchBarTopMargin: Int = attributes.getDimensionPixelSize(
                R.styleable.SearchView_searchBarMarginTop,
                ATTRS_SEARCH_BAR_MARGIN_DEFAULT
            )

            val searchBarRightMargin: Int = attributes.getDimensionPixelSize(
                R.styleable.SearchView_searchBarMarginRight,
                ATTRS_SEARCH_BAR_MARGIN_DEFAULT
            )

            val querySectionLP: LayoutParams = mSearchBar.layoutParams as LayoutParams
            val dividerLP: LayoutParams = mDivider.layoutParams as LayoutParams
            val suggestListSectionLP: LinearLayout.LayoutParams = mSuggestionsSection.layoutParams as LinearLayout.LayoutParams
            val cardPadding: Int = Util.dpToPx(CARD_VIEW_TOP_BOTTOM_SHADOW_HEIGHT)

            querySectionLP.setMargins(
                searchBarLeftMargin,
                searchBarTopMargin,
                searchBarRightMargin,
                0
            )

            dividerLP.setMargins(
                searchBarLeftMargin + cardPadding + 2,
                0,
                searchBarRightMargin + cardPadding + 2,
                (mDivider.layoutParams as MarginLayoutParams).bottomMargin
            )

            suggestListSectionLP.setMargins(
                searchBarLeftMargin,
                0,
                searchBarRightMargin,
                0
            )

            mSearchBar.layoutParams = querySectionLP
            mDivider.layoutParams = dividerLP
            mSuggestionsSection.layoutParams = suggestListSectionLP

            setSearchHint(attributes.getString(R.styleable.SearchView_searchHint))

            setCloseSearchOnKeyboardDismiss(
                attributes.getBoolean(
                    R.styleable.SearchView_close_search_on_keyboard_dismiss,
                    ATTRS_DISMISS_ON_KEYBOARD_DISMISS_DEFAULT
                )
            )

            setDismissOnOutsideClick(
                attributes.getBoolean(
                    R.styleable.SearchView_dismissOnOutsideTouch,
                    ATTRS_DISMISS_ON_OUTSIDE_TOUCH_DEFAULT
                )
            )

            setDismissFocusOnItemSelection(
                attributes.getBoolean(
                    R.styleable.SearchView_dismissFocusOnItemSelection,
                    ATTRS_DISMISS_FOCUS_ON_ITEM_SELECTION_DEFAULT
                )
            )

            setDimBackground(
                attributes.getBoolean(
                    R.styleable.SearchView_dimBackground,
                    ATTRS_SHOW_DIM_BACKGROUND_DEFAULT
                )
            )

            mSuggestionSectionAnimDuration = attributes.getInt(
                R.styleable.SearchView_suggestionsListAnimDuration,
                ATTRS_SUGGESTION_ANIM_DURATION_DEFAULT
            ).toLong()

            setBackgroundColor(
                attributes.getColor(
                    R.styleable.SearchView_backgroundColor,
                    Util.getColor(
                        context,
                        R.color.background
                    )
                )
            )

            setClearBtnColor(
                attributes.getColor(
                    R.styleable.SearchView_clearBtnColor,
                    Util.getColor(
                        context,
                        R.color.clear_btn_color
                    )
                )
            )

            val viewTextColor: Int = attributes.getColor(
                R.styleable.SearchView_viewTextColor,
                Util.getColor(context, R.color.dark_gray)
            )

            setQueryTextColor(
                attributes.getColor(
                    R.styleable.SearchView_viewSearchInputTextColor,
                    viewTextColor
                )
            )

            setHintTextColor(
                attributes.getColor(
                    R.styleable.SearchView_hintTextColor,
                    Util.getColor(
                        context,
                        R.color.hint_color
                    )
                )
            )

            setLeftIconColor(
                attributes.getColor(
                    R.styleable.SearchView_leftActionColor,
                    Util.getColor(
                        context,
                        R.color.gray_active_icon
                    )
                )
            )
        } finally {
            attributes.recycle()
        }
    }

    private fun setupQueryBar() {
        mSearchInput.setTextColor(mSearchInputTextColor)
        mSearchInput.setHintTextColor(mSearchInputHintColor)
        if (!isInEditMode && mHostActivity != null) {
            mHostActivity?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
        val vto: ViewTreeObserver = mSearchBar.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                Util.removeGlobalLayoutObserver(
                    mSearchBar,
                    this
                )
            }
        })

        mClearButton.visibility = View.INVISIBLE
        mClearButton.setOnClickListener {
            mSearchInput.setText("")
            if (mOnClearSearchActionListener != null) {
                mOnClearSearchActionListener!!.onClearSearchClicked()
            }
        }

        mSearchInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //todo investigate why this is called twice when pressing back on the keyboard
                if (mSkipTextChangeEvent || !isSearchBarFocused) {
                    mSkipTextChangeEvent = false
                } else {
                    if (mSearchInput.text.toString().isNotEmpty() &&
                        mClearButton.visibility == View.INVISIBLE
                    ) {
                        mClearButton.alpha = 0.0f
                        mClearButton.visibility = View.VISIBLE
                        ViewCompat.animate(mClearButton).alpha(1.0f).setDuration(
                            CLEAR_BTN_FADE_ANIM_DURATION
                        ).start()
                    } else if (mSearchInput.text.toString().isEmpty()) {
                        mClearButton.visibility = View.INVISIBLE
                    }
                    if (mQueryListener != null && isSearchBarFocused && query != mSearchInput.text
                            .toString()
                    ) {
                        mQueryListener!!.onSearchTextChanged(
                            query,
                            mSearchInput.text.toString()
                        )
                    }
                }

                query = mSearchInput.text.toString()
            }
        })

        mSearchInput.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (mSkipQueryFocusChangeEvent) {
                    mSkipQueryFocusChangeEvent = false
                } else if (hasFocus != isSearchBarFocused) {
                    setSearchFocusedInternal(hasFocus)
                }
            }
        }

        mSearchInput.setOnKeyboardDismissedListener(object :
            SearchInputView.OnKeyboardDismissedListener {
            override fun onKeyboardDismissed() {
                if (mCloseSearchOnSofteKeyboardDismiss) {
                    setSearchFocusedInternal(false)
                }
            }
        })

        mSearchInput.setOnSearchKeyListener(object :
            SearchInputView.OnKeyboardSearchKeyClickListener {
            override fun onSearchKeyClicked() {
                mSearchListener?.onSearchAction(query)
                mSkipTextChangeEvent = true
                mSkipTextChangeEvent = true

                if (mIsTitleSet) {
                    setSearchBarTitle(query)
                } else {
                    setSearchText(query)
                }

                setSearchFocusedInternal(false)
            }
        })

        mLeftAction.setOnClickListener {
            if (isSearchBarFocused) {
                setSearchFocusedInternal(false)
            } else {
                setSearchFocusedInternal(true)
            }
        }

        refreshLeftIcon()
    }

    /**
     * Sets the clear button's color.
     *
     * @param color the color to be applied to the
     * clear button.
     */
    fun setClearBtnColor(color: Int) {
        mClearBtnColor = color
        DrawableCompat.setTint(mIconClear, mClearBtnColor)
    }

    /**
     * Sets the background color of the search
     * view including the suggestions section.
     *
     * @param color the color to be applied to the search bar and
     * the suggestion section background.
     */
    override fun setBackgroundColor(color: Int) {
        mBackgroundColor = color
        mSearchBar.setCardBackgroundColor(color)
        mSuggestionsList.setBackgroundColor(color)
    }

    /**
     * Sets whether the search will lose focus when a suggestion item is clicked.
     *
     * @param dismissFocusOnItemSelection
     */
    fun setDismissFocusOnItemSelection(dismissFocusOnItemSelection: Boolean) {
        mDismissFocusOnItemSelection = dismissFocusOnItemSelection
    }

    /**
     * Set the duration for the suggestions list expand/collapse
     * animation.
     *
     * @param duration
     */
    fun setSuggestionsAnimDuration(duration: Long) {
        mSuggestionSectionAnimDuration = duration
    }

    /**
     * Sets the text color of the search text.
     *
     * @param color
     */
    fun setQueryTextColor(color: Int) {
        mSearchInputTextColor = color
        mSearchInput.setTextColor(mSearchInputTextColor)
    }

    /**
     * Sets the text color of the search
     * hint.
     *
     * @param color the color to be applied to the search hint.
     */
    fun setHintTextColor(color: Int) {
        mSearchInputHintColor = color
        mSearchInput.setHintTextColor(color)
    }

    /**
     * Set the tint of the left icon btn
     *
     * @param color
     */
    fun setLeftIconColor(color: Int) {
        DrawableCompat.setTint(mIconBackArrow, color)
        DrawableCompat.setTint(mIconSearch, color)
    }

    private fun refreshLeftIcon() {
        mLeftAction.visibility = View.VISIBLE
        mLeftAction.setImageDrawable(mIconSearch)
        mSearchInputParent.translationX = 0f
    }



    /**
     * Shows a circular progress on top of the
     * menu action button.
     *
     *
     * Call hidProgress()
     * to change back to normal and make the menu
     * action visible.
     */
    fun showProgress() {
        mLeftAction.visibility = View.GONE
        mSearchProgress.alpha = 0.0f
        mSearchProgress.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(mSearchProgress, "alpha", 0.0f, 1.0f).start()
    }

    /**
     * Hides the progress bar after
     * a prior call to showProgress()
     */
    fun hideProgress() {
        mSearchProgress.visibility = View.GONE
        mLeftAction.alpha = 0.0f
        mLeftAction.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(mLeftAction, "alpha", 0.0f, 1.0f).start()
    }

    /**
     * Set a hint that will appear in the
     * search input. Default hint is R.string.abc_search_hint
     * which is "search..." (when device language is set to english)
     *
     * @param searchHint
     */
    fun setSearchHint(searchHint: String?) {
        mSearchHint = searchHint ?: resources.getString(R.string.abc_search_hint)
        mSearchInput.hint = mSearchHint
    }

    /**
     * Sets whether the search will lose focus when the softkeyboard
     * gets closed from a back press
     *
     * @param closeSearchOnKeyboardDismiss
     */
    fun setCloseSearchOnKeyboardDismiss(closeSearchOnKeyboardDismiss: Boolean) {
        mCloseSearchOnSofteKeyboardDismiss = closeSearchOnKeyboardDismiss
    }

    /**
     * Set whether a touch outside of the
     * search bar's bounds will cause the search bar to
     * loos focus.
     *
     * @param enable true to dismiss on outside touch, false otherwise.
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setDismissOnOutsideClick(enable: Boolean) {
        mDismissOnOutsideTouch = enable
        mSuggestionsSection.setOnTouchListener { _, _ -> //todo check if this is called twice
            if (mDismissOnOutsideTouch && isSearchBarFocused) {
                setSearchFocusedInternal(false)
            }
            true
        }
    }

    /**
     * Sets whether a dim background will show when the search is focused
     *
     * @param dimEnabled True to show dim
     */
    fun setDimBackground(dimEnabled: Boolean) {
        mDimBackground = dimEnabled
        refreshDimBackground()
    }

    private fun refreshDimBackground() {
        if (mDimBackground && isSearchBarFocused) {
            mSearchViewBackgroundDrawable.alpha = BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED
            isClickable = true
        } else {
            mSearchViewBackgroundDrawable.alpha = BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED
            isClickable = false
        }
    }

    /**
     * Wrapper implementation for EditText.setFocusable(boolean focusable)
     *
     * @param focusable true, to make search focus when
     * clicked.
     */
    fun setSearchFocusable(focusable: Boolean) {
        mSearchInput.isFocusable = focusable
        mSearchInput.isFocusableInTouchMode = focusable
    }

    /**
     * Sets the title for the search bar.
     *
     *
     * Note that after the title is set, when
     * the search gains focus, the title will be replaced
     * by the search hint.
     *
     * @param title the title to be shown when search
     * is not focused
     */
    fun setSearchBarTitle(title: CharSequence?) {
        mTitleText = title.toString()
        mIsTitleSet = true
        mSearchInput.setText(title)
    }

    /**
     * Sets the search text.
     *
     *
     * Note that this is the different from
     * [setSearchBarTitle][.setSearchBarTitle] in
     * that it keeps the text when the search gains focus.
     *
     * @param text the text to be set for the search
     * input.
     */
    fun setSearchText(text: CharSequence?) {
        mIsTitleSet = false
        setQueryText(text)
    }

    fun clearQuery() {
        mSearchInput.setText("")
    }

    /**
     * Sets whether the search is focused or not.
     *
     * @param focused true, to set the search to be active/focused.
     * @return true if the search was focused and will now become not focused. Useful for
     * calling supper.onBackPress() in the hosting activity only if this method returns false
     */
    fun setSearchFocused(focused: Boolean): Boolean {
        val updatedToNotFocused = !focused && isSearchBarFocused
        if (focused != isSearchBarFocused && mSuggestionSecHeightListener == null) {
            if (mIsSuggestionsSectionHeightSet) {
                setSearchFocusedInternal(focused)
            } else {
                mSuggestionSecHeightListener = object : OnSuggestionSecHeightSetListener {
                    override fun onSuggestionSecHeightSet() {
                        setSearchFocusedInternal(focused)
                        mSuggestionSecHeightListener = null
                    }
                }
            }
        }
        return updatedToNotFocused
    }

    private fun setupSuggestionSection() {
        mSuggestionsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false).apply {
                stackFromEnd = true
        }
        mSuggestionsList.itemAnimator = null

        val gestureDetector = GestureDetector(context,
            object : GestureDetectorListenerAdapter() {
                override fun onScroll(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    mHostActivity?.let { Util.closeSoftKeyboard(it) }
                    return false
                }
            })

        mSuggestionsList.addOnItemTouchListener(object :
            OnItemTouchListenerAdapter() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(e)
                return false
            }
        })

        mSuggestionsAdapter = SearchSuggestionsAdapter(
            object : SearchSuggestionsAdapter.Listener {
                override fun onItemSelected(item: SearchSuggestion) {
                    if (mSearchListener != null) {
                        mSearchListener!!.onSuggestionClicked(item)
                    }

                    if (mDismissFocusOnItemSelection) {
                        isSearchBarFocused = false
                        mSkipTextChangeEvent = true
                        if (mIsTitleSet) {
                            setSearchBarTitle(item.body)
                        } else {
                            setSearchText(item.body)
                        }

                        setSearchFocusedInternal(false)
                    }
                }

                override fun onMoveItemToSearchClicked(item: SearchSuggestion) {
                    setQueryText(item.body)
                }
            }
        )

        mSuggestionsList.adapter = mSuggestionsAdapter

        val cardViewBottomPadding: Int = Util.dpToPx(
            SEARCH_CARD_VIEW_CORNERS_AND_TOP_BOTTOM_SHADOW_HEIGHT
        )
        //move up the suggestions section enough to cover the search bar
        //card's bottom left and right corners
        mSuggestionsSection.translationY = -cardViewBottomPadding.toFloat()
    }

    private fun setQueryText(text: CharSequence?) {
        mSearchInput.setText(text)
        //move cursor to end of text
        mSearchInput.text?.let { mSearchInput.setSelection(it.length) }
    }

    private fun moveSuggestListToInitialPos() {
        //move the suggestions list to the collapsed position
        //which is translationY of -listContainerHeight
        mSuggestionListContainer.translationY = -mSuggestionListContainer.height.toFloat()
    }

    /**
     * Clears the current suggestions and replaces it
     * with the provided list of new suggestions.
     *
     * @param newSearchSuggestions a list containing the new suggestions
     */
    fun swapSuggestions(newSearchSuggestions: List<SearchSuggestion>) {
        swapSuggestions(newSearchSuggestions, true)
    }

    private fun swapSuggestions(
        newSearchSuggestions: List<SearchSuggestion>,
        withAnim: Boolean
    ) {
        mSuggestionsList.afterMeasured {
            updateSuggestionsSectionHeight(withAnim)
            mSuggestionsList.alpha = 1F
        }

        mSuggestionsList.alpha = 0F
        mSuggestionsAdapter.swapData(newSearchSuggestions)
        mDivider.visibility = if (newSearchSuggestions.isNotEmpty()) View.VISIBLE else View.GONE
    }

    //returns true if the suggestion items occupy the full RecyclerView's height, false otherwise
    private fun updateSuggestionsSectionHeight(withAnim: Boolean): Boolean {
        val cardTopBottomShadowPadding: Int = Util.dpToPx(
            SEARCH_CARD_VIEW_CORNERS_AND_TOP_BOTTOM_SHADOW_HEIGHT
        )
        val cardRadiusSize: Int = Util.dpToPx(CARD_VIEW_TOP_BOTTOM_SHADOW_HEIGHT)
        val visibleSuggestionHeight = calculateSuggestionItemsHeight(mSuggestionListContainer.height)
        val diff = mSuggestionListContainer.height - visibleSuggestionHeight

        val addedTranslationYForShadowOffsets =
            if (diff <= cardTopBottomShadowPadding) -(cardTopBottomShadowPadding - diff)
            else if (diff < mSuggestionListContainer.height - cardTopBottomShadowPadding) cardRadiusSize
            else 0

        val newTranslationY =
            (-mSuggestionListContainer.height + visibleSuggestionHeight + addedTranslationYForShadowOffsets).toFloat()

        val fullyInvisibleTranslationY = (-mSuggestionListContainer.height + cardRadiusSize).toFloat()
        ViewCompat.animate(mSuggestionListContainer).cancel()

        if (withAnim) {
            ViewCompat.animate(mSuggestionListContainer).setInterpolator(
                SUGGEST_ITEM_ADD_ANIM_INTERPOLATOR
            )
            .setDuration(mSuggestionSectionAnimDuration)
            .translationY(newTranslationY)
            .setUpdateListener { view ->
                mOnSuggestionsListHeightChanged?.let {
                    val newSuggestionsHeight = abs(view.translationY - fullyInvisibleTranslationY)
                    it.onSuggestionsListHeightChanged(newSuggestionsHeight)
                }
            }.setListener(object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationCancel(view: View?) {
                    mSuggestionListContainer.translationY = newTranslationY
                }
            }).start()
        } else {
            mSuggestionListContainer.translationY = newTranslationY
            if (mOnSuggestionsListHeightChanged != null) {
                val newSuggestionsHeight = abs(
                    mSuggestionListContainer.translationY - fullyInvisibleTranslationY
                )
                mOnSuggestionsListHeightChanged!!.onSuggestionsListHeightChanged(
                    newSuggestionsHeight
                )
            }
        }

        return mSuggestionListContainer.height == visibleSuggestionHeight
    }

    private fun calculateSuggestionItemsHeight(max: Int): Int {
        var visibleItemsHeight = 0
        for(child in mSuggestionsList.children) {
            visibleItemsHeight += child.height
            if (visibleItemsHeight > max) {
                visibleItemsHeight = max
                break
            }
        }

        return visibleItemsHeight
    }

    /**
     * Collapses the suggestions list and
     * then clears its suggestion items.
     */
    fun clearSuggestions() {
        swapSuggestions(ArrayList())
    }

    fun clearSearchFocus() {
        setSearchFocusedInternal(false)
    }

    private fun setSearchFocusedInternal(focused: Boolean) {
        isSearchBarFocused = focused
        val params = mSearchInput.layoutParams as LayoutParams

        if (focused) {
            mSearchInput.requestFocus()
            moveSuggestListToInitialPos()
            mSuggestionsSection.visibility = View.VISIBLE

            if (mDimBackground) {
                fadeInBackground()
            }

            transitionInLeftSection(true)
            Util.showSoftKeyboard(context, mSearchInput)

            if (mIsTitleSet) {
                mSkipTextChangeEvent = true
                mSearchInput.setText("")
            } else {
                mSearchInput.text?.length?.let { mSearchInput.setSelection(it) }
            }

            mSearchInput.isLongClickable = true
            mClearButton.visibility = if (mSearchInput.text.toString().isEmpty()) View.INVISIBLE else View.VISIBLE
            params.marginEnd = resources.getDimensionPixelSize(R.dimen.square_button_size)

            if (mFocusChangeListener != null) {
                mFocusChangeListener!!.onFocus()
            }
        } else {
            mMainLayout.requestFocus()
            clearSuggestions()
            if (mDimBackground) {
                fadeOutBackground()
            }

            changeLeftIcon(mLeftAction, mIconSearch)
            mClearButton.visibility = View.GONE
            params.marginEnd = 24

            if (mHostActivity != null) {
                Util.closeSoftKeyboard(mHostActivity!!)
            }

            if (mIsTitleSet) {
                mSkipTextChangeEvent = true
                mSearchInput.setText(mTitleText)
            }

            mSearchInput.isLongClickable = false
            if (mFocusChangeListener != null) {
                mFocusChangeListener!!.onFocusCleared()
            }
        }

        mSearchInput.layoutParams = params
        //if we don't have focus, we want to allow the client's views below our invisible
        //screen-covering view to handle touches
        mSuggestionsSection.isEnabled = focused
    }

    private fun changeLeftIcon(imageView: ImageView, newIcon: Drawable) {
        imageView.setImageDrawable(newIcon)
        val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
        alphaAnimation.duration = 300
        imageView.startAnimation(alphaAnimation)
    }

    private fun transitionInLeftSection(withAnim: Boolean) {
        if (mSearchProgress.visibility != View.VISIBLE) {
            mLeftAction.visibility = View.VISIBLE
        } else {
            mLeftAction.visibility = View.INVISIBLE
        }

        mLeftAction.setImageDrawable(mIconBackArrow)

        if (withAnim) {
            mLeftAction.rotation = 45f
            mLeftAction.alpha = 0.0f
            val rotateAnim: ObjectAnimator = ViewPropertyObjectAnimator.animate(mLeftAction).rotation(0F).get()
            val fadeAnim: ObjectAnimator = ViewPropertyObjectAnimator.animate(mLeftAction).alpha(1.0f).get()
            val animSet = AnimatorSet()
            animSet.duration = 200
            animSet.playTogether(rotateAnim, fadeAnim)
            animSet.start()
        }
    }

    /**
     * Sets the listener that will be notified when the suggestion list's height
     * changes.
     *
     * @param onSuggestionsListHeightChanged the new suggestions list's height
     */
    fun setOnSuggestionsListHeightChanged(onSuggestionsListHeightChanged: OnSuggestionsListHeightChanged?) {
        mOnSuggestionsListHeightChanged = onSuggestionsListHeightChanged
    }

    /**
     * Sets the listener that will listen for query
     * changes as they are being typed.
     *
     * @param listener listener for query changes
     */
    fun setOnQueryChangeListener(listener: OnQueryChangeListener?) {
        mQueryListener = listener
    }

    /**
     * Sets the listener that will be called when
     * an action that completes the current search
     * session has occurred and the search lost focus.
     *
     *
     *
     * When called, a client would ideally grab the
     * search or suggestion query from the callback parameter or
     * from [getquery][.getQuery] and perform the necessary
     * query against its data source.
     *
     * @param listener listener for query completion
     */
    fun setOnSearchListener(listener: OnSearchListener?) {
        mSearchListener = listener
    }

    /**
     * Sets the listener that will be called when the focus
     * of the search has changed.
     *
     * @param listener listener for search focus changes
     */
    fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        mFocusChangeListener = listener
    }

    /**
     * Sets the listener that will be called when the
     * clear search text action button (the x to the right
     * of the search text) is clicked.
     *
     * @param listener
     */
    fun setOnClearSearchActionListener(listener: OnClearSearchActionListener?) {
        mOnClearSearchActionListener = listener
    }

    private fun fadeOutBackground() {
        val anim: ValueAnimator = ValueAnimator.ofInt(
            BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED,
            BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED
        )
        anim.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            mSearchViewBackgroundDrawable.alpha = value
        }
        anim.duration = BACKGROUND_FADE_ANIM_DURATION.toLong()
        anim.start()
    }

    private fun fadeInBackground() {
        val anim: ValueAnimator = ValueAnimator.ofInt(
            BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED, BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED
        )
        anim.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            mSearchViewBackgroundDrawable.alpha = value
        }
        anim.duration = BACKGROUND_FADE_ANIM_DURATION.toLong()
        anim.start()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState: Parcelable? = super.onSaveInstanceState()
        val savedState = SavedState(superState)

        savedState.suggestions = mSuggestionsAdapter.dataSet
        savedState.isFocused = isSearchBarFocused
        savedState.query = query
        savedState.searchHint = mSearchHint
        savedState.dismissOnOutsideClick = mDismissOnOutsideTouch
        savedState.showMoveSuggestionUpBtn = mShowMoveUpSuggestion
        savedState.isTitleSet = mIsTitleSet
        savedState.backgroundColor = mBackgroundColor
        savedState.queryTextColor = mSearchInputTextColor
        savedState.searchHintTextColor = mSearchInputHintColor
        savedState.clearBtnColor = mClearBtnColor
        savedState.dimBackground = mDimBackground
        savedState.dismissOnSoftKeyboardDismiss = mDismissOnOutsideTouch
        savedState.dismissFocusOnSuggestionItemClick = mDismissFocusOnItemSelection

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        isSearchBarFocused = savedState.isFocused
        mIsTitleSet = savedState.isTitleSet
        query = savedState.query
        setSearchText(query)
        mSuggestionSectionAnimDuration = savedState.suggestionsSectionAnimSuration
        setDismissOnOutsideClick(savedState.dismissOnOutsideClick)
        setSearchHint(savedState.searchHint)
        setBackgroundColor(savedState.backgroundColor)
        setQueryTextColor(savedState.queryTextColor)
        setHintTextColor(savedState.searchHintTextColor)
        setClearBtnColor(savedState.clearBtnColor)
        setDimBackground(savedState.dimBackground)
        setCloseSearchOnKeyboardDismiss(savedState.dismissOnSoftKeyboardDismiss)
        setDismissFocusOnItemSelection(savedState.dismissFocusOnSuggestionItemClick)
        mSuggestionsSection.isEnabled = isSearchBarFocused

        if (isSearchBarFocused) {
            mSearchViewBackgroundDrawable.alpha = BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED
            mSkipTextChangeEvent = true
            mSkipQueryFocusChangeEvent = true
            mSuggestionsSection.visibility = View.VISIBLE

            //restore suggestions list when suggestion section's height is fully set
            mSuggestionSecHeightListener = object : OnSuggestionSecHeightSetListener {
                override fun onSuggestionSecHeightSet() {
                    swapSuggestions(savedState.suggestions, false)
                    mSuggestionSecHeightListener = null

                    //todo refactor move to a better location
                    transitionInLeftSection(false)
                }
            }

            mClearButton.visibility =
                if (savedState.query.isEmpty()) View.INVISIBLE else View.VISIBLE
            mLeftAction.visibility = View.VISIBLE
            Util.showSoftKeyboard(context, mSearchInput)
        }
    }

    internal class SavedState: BaseSavedState {
        var suggestions: List<SearchSuggestion> = ArrayList()
        var isFocused = false
        var query: String = ""
        var suggestionTextSize = 0
        var searchHint: String? = null
        var dismissOnOutsideClick = false
        var showMoveSuggestionUpBtn = false
        var showSearchKey = false
        var isTitleSet = false
        var backgroundColor = 0
        var suggestionsTextColor = 0
        var queryTextColor = 0
        var searchHintTextColor = 0
        var clearBtnColor = 0
        var dividerColor = 0
        var menuId = 0
        var dimBackground = false
        var suggestionsSectionAnimSuration: Long = 0
        var dismissOnSoftKeyboardDismiss = false
        var dismissFocusOnSuggestionItemClick = false

        constructor(superState: Parcelable?): super(superState)

        private constructor(`in`: Parcel): super(`in`) {
            `in`.readList(suggestions, javaClass.classLoader)
            isFocused = `in`.readInt() != 0
            query = `in`.readString().toString()
            suggestionTextSize = `in`.readInt()
            searchHint = `in`.readString()
            dismissOnOutsideClick = `in`.readInt() != 0
            showMoveSuggestionUpBtn = `in`.readInt() != 0
            showSearchKey = `in`.readInt() != 0
            isTitleSet = `in`.readInt() != 0
            backgroundColor = `in`.readInt()
            suggestionsTextColor = `in`.readInt()
            queryTextColor = `in`.readInt()
            searchHintTextColor = `in`.readInt()
            clearBtnColor = `in`.readInt()
            dividerColor = `in`.readInt()
            menuId = `in`.readInt()
            dimBackground = `in`.readInt() != 0
            suggestionsSectionAnimSuration = `in`.readLong()
            dismissOnSoftKeyboardDismiss = `in`.readInt() != 0
            dismissFocusOnSuggestionItemClick = `in`.readInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeList(suggestions)
            out.writeInt(if (isFocused) 1 else 0)
            out.writeString(query)
            out.writeInt(suggestionTextSize)
            out.writeString(searchHint)
            out.writeInt(if (dismissOnOutsideClick) 1 else 0)
            out.writeInt(if (showMoveSuggestionUpBtn) 1 else 0)
            out.writeInt(if (showSearchKey) 1 else 0)
            out.writeInt(if (isTitleSet) 1 else 0)
            out.writeInt(backgroundColor)
            out.writeInt(suggestionsTextColor)
            out.writeInt(queryTextColor)
            out.writeInt(searchHintTextColor)
            out.writeInt(clearBtnColor)
            out.writeInt(dividerColor)
            out.writeInt(menuId)
            out.writeInt(if (dimBackground) 1 else 0)
            out.writeLong(suggestionsSectionAnimSuration)
            out.writeInt(if (dismissOnSoftKeyboardDismiss) 1 else 0)
            out.writeInt(if (dismissFocusOnSuggestionItemClick) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        //remove any ongoing animations to prevent leaks
        //todo investigate if correct
        ViewCompat.animate(mSuggestionListContainer).cancel()
    }

    companion object {
        private val TAG = SearchView::class.java.simpleName

        //The CardView's top or bottom height used for its shadow
        private const val CARD_VIEW_TOP_BOTTOM_SHADOW_HEIGHT = 3

        //The CardView's (default) corner radius height
        private const val SEARCH_CARD_VIEW_CORNERS_HEIGHT = 4
        private const val SEARCH_CARD_VIEW_CORNERS_AND_TOP_BOTTOM_SHADOW_HEIGHT = CARD_VIEW_TOP_BOTTOM_SHADOW_HEIGHT + SEARCH_CARD_VIEW_CORNERS_HEIGHT

        private const val CLEAR_BTN_FADE_ANIM_DURATION: Long = 500
        private const val BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED = 150
        private const val BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED = 0
        private const val BACKGROUND_FADE_ANIM_DURATION = 250
        private val SUGGEST_ITEM_ADD_ANIM_INTERPOLATOR: Interpolator = LinearInterpolator()

        private const val ATTRS_SHOW_MOVE_UP_SUGGESTION_DEFAULT = false
        private const val ATTRS_DISMISS_ON_OUTSIDE_TOUCH_DEFAULT = true
        private const val ATTRS_DISMISS_ON_KEYBOARD_DISMISS_DEFAULT = false
        private const val ATTRS_SEARCH_BAR_SHOW_SEARCH_KEY_DEFAULT = true
        private const val ATTRS_SHOW_DIM_BACKGROUND_DEFAULT = true
        private const val ATTRS_SUGGESTION_ANIM_DURATION_DEFAULT = 250
        private const val ATTRS_SEARCH_BAR_MARGIN_DEFAULT = 0
        private const val ATTRS_DISMISS_FOCUS_ON_ITEM_SELECTION_DEFAULT = false
    }

    init {
        init(attrs)
    }
}