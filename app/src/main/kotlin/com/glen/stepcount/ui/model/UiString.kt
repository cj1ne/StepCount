package com.glen.stepcount.ui.model

import android.content.Context
import androidx.annotation.StringRes

sealed class UiString {

    data class Resource(@StringRes val resId: Int) : UiString()
    class ResourceFormat(@StringRes val resId: Int, vararg val formatArgs: Any) : UiString()
    data class Text(val value: String) : UiString()

    fun toString(context: Context): String {
        return when (this) {
            is Resource -> context.getString(resId)
            is ResourceFormat -> context.getString(
                resId,
                *(formatArgs.map { if (it is UiString) it.toString(context) else it }.toTypedArray())
            )
            is Text -> value
        }
    }
}
