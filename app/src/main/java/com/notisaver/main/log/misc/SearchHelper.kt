package com.notisaver.main.log.misc

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.notisaver.R
import com.notisaver.misc.findView


class SupportSearchBarBinding private constructor(private val rootView: View) {

    private val closeButton: AppCompatImageView
    private val clearTextButton: AppCompatImageView
    private val enterView: AppCompatEditText

    private var queryChangeListener: ((String?) -> Unit)? = null

    init {
        enterView = findView(R.id.search_bar_enter_view)
        clearTextButton = findView(R.id.search_bar_clear_text_button)
        closeButton = findView(R.id.search_bar_close_button)

        clearTextButton.setOnClickListener {
            clearText()
        }

        enterView.addTextChangedListener {
            val query = it?.trim()?.toString()

            clearTextButton.isVisible = query?.length != 0

            queryChangeListener?.invoke(query)
        }
    }

    internal fun setOnQueryChange(action: (String?) -> Unit) {
        queryChangeListener = action
    }

    internal fun setOnClickCloseListener(listener: View.OnClickListener) {
        closeButton.setOnClickListener(listener)
    }

    internal fun clearText() {
        enterView.editableText.clear()
    }

    internal fun setContainerVisibility(visibility: Int) {
        rootView.visibility = visibility
    }

    internal fun requestFocus() {
        enterView.requestFocus()
        (rootView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            enterView,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    internal fun hideKeyboard() {
        (rootView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            enterView.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    @Throws
    private fun <T : View> findView(@IdRes id: Int): T {
        return rootView.findView(id)
    }

    companion object {

        @JvmStatic
        fun bind(containerLayout: View): SupportSearchBarBinding {
            return SupportSearchBarBinding(containerLayout)
        }
    }
}

class SearchSupport : ISearchSupport {
    var searchObserver: ISearchSupport.ISearchObserver? = null
    var trigger: ISearchSupport.ITriggerSearchObserver? = null

    override fun turnOnSearchMode() {
        trigger?.onSearchModeOn()
    }

    override fun closeSearch() {
        trigger?.onSearchClose()
    }

    override fun submitQuery(submit: String?) {
        searchObserver?.onQuerySubmitChange(
            submit.orEmpty()
        )
    }

    override fun onQueryChange(newText: String?) {
        searchObserver?.onQueryChange(
            newText.orEmpty()
        )
    }
}

interface ISearchSupport {
    interface ISearchObserver {
        fun onQueryChange(newText: String)
        fun onQuerySubmitChange(submit: String)
    }

    interface ITriggerSearchObserver {
        var isSearching: Boolean
        fun onSearchModeOn()
        fun onSearchClose()
    }

    fun turnOnSearchMode()
    fun closeSearch()
    fun submitQuery(submit: String?)
    fun onQueryChange(newText: String?)
}