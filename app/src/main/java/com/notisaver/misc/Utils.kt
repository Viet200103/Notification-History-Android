package com.notisaver.misc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.notisaver.BuildConfig
import com.notisaver.R
import com.notisaver.database.NotisaveDatabase
import com.notisaver.database.entities.AppInformation
import com.notisaver.main.log.core.NotisaveListenerService
import com.notisaver.main.NotisaveApplication
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// fill 0 or 1, weight 600, optical size 48px

internal fun isNotificationAccessEnabled(context: Context): Boolean {
    try {
        val contentResolver = context.contentResolver
        val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return !(listeners == null || !listeners.contains(BuildConfig.APPLICATION_ID + "/"))
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) e.printStackTrace()
    }
    return false
}

internal fun changeVisibleView(view: View, isVisible: Boolean) {
    if (view.isVisible != isVisible) {
        view.isVisible = isVisible
    }
}

@Throws
internal fun <V : View> RecyclerView.ViewHolder.findView(@IdRes viewId: Int): V {
    return itemView.findViewById(viewId)
}

@Throws
internal fun <V : View> View.findView(@IdRes viewId: Int): V {
    return findViewById(viewId)
}

@Throws
internal fun <V : View> Fragment.findView(@IdRes viewId: Int): V {
    return view!!.findViewById(viewId)
}

internal fun Any?.toStringOrNullString(): String {
    return this?.toString() ?: "null"
}

internal fun hashString131(s: String): String {
    var h = 0L
    for (i in s.indices) {
        h = 131 * h + s[i].code
    }
    return h.toString()
}

internal fun createPackageHashcode(packageName: String) = hashString131(packageName)

internal fun createUUID(removeDash: Boolean): String {
    val uuid = UUID.randomUUID()
    return if (removeDash) {
        uuid.toString().replace("-", "")
    } else {
        uuid.toString()
    }
}

@Throws
internal fun <T : Parcelable> Bundle.getParcelableCompat(key: String?, clazz: Class<T>): T? {
    if (key == null) return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelable(key)
    }
}

@Throws
internal fun <T : Parcelable> Intent.getParcelableExtraCompat(key: String?, clazz: Class<T>): T? {
    if (key == null) return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(key)
    }
}

internal fun calculateNoOfColumns(
    context: Context,
    columnWidthDp: Float
): Int {
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / columnWidthDp).toInt()
}

internal fun Context.asNotisaveApplication(): NotisaveApplication =
    this.applicationContext as NotisaveApplication

internal fun Context.requireAppNotNull(appInfo: AppInformation?, action: (AppInformation) -> Unit) {
    if (appInfo == null) {
        Toast.makeText(this, R.string.error_app_unknown, Toast.LENGTH_SHORT).show()
    } else {
        action.invoke(appInfo)
    }
}

internal fun LifecycleOwner.collectWhenStarted(
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend () -> Unit
) = collectWithState(Lifecycle.State.STARTED, context, action)

internal fun LifecycleOwner.collectWhenCreated(
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend () -> Unit
) = collectWithState(Lifecycle.State.CREATED, context, action)

internal fun LifecycleOwner.collectWithState(
    state: Lifecycle.State,
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend () -> Unit
) = lifecycleScope.launch(context) {
    repeatOnLifecycle(state) {
        action()
    }
}

internal fun LifecycleOwner.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context, start, block)

internal fun createSbnKeyHashcode(sbnKey: String) = hashString131(sbnKey)

@Throws
internal fun createNotificationSettingIntent(packageName: String): Intent {

    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    return intent
}

internal fun createDetailsSettingIntent(packageName: String): Intent {
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
    }
}

@Throws
internal fun startLauncherIntent(context: Context, intentLaunch: Intent?) {
    try {
        context.startActivity(intentLaunch)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            R.string.package_not_found_error,
            Toast.LENGTH_SHORT
        ).show()
    }
}

internal fun RecyclerView.addHideExtendedFloatingButton(extendedButton: ExtendedFloatingActionButton) {
    addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        val canExpand = !recyclerView.canScrollVertically(-1)
                        if (canExpand && !extendedButton.isExtended) {
                            extendedButton.extend()
                        }
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        if (extendedButton.isExtended) {
                            extendedButton.shrink()
                        }
                    }
                }
            }
        }
    )
}

internal fun createPageConfig(enablePlaceholders: Boolean) = PagingConfig(
    pageSize = NotisaveDatabase.DEFAULT_PER_PAGE,
    enablePlaceholders = enablePlaceholders
)

internal fun Activity.makeSnackBar(
    text: String,
    @BaseTransientBottomBar.Duration duration: Int
): Snackbar? {
    val view = window.findViewById<View>(
        android.R.id.content
    )
    view?.let {
        return Snackbar.make(it, text, duration)
    }
    return null
}

internal fun createAccessNotificationIntent(context: Context): Intent {

    val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
    val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"

    val showArgs = context.packageName + "/" + NotisaveListenerService::class.java.name
    val bundle = Bundle().apply {
        putString(EXTRA_FRAGMENT_ARG_KEY, showArgs)
    }
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        putExtra(EXTRA_FRAGMENT_ARG_KEY, showArgs)
        putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
    }

    return intent
}

internal fun createNormalAccessNotificationIntent() = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

internal fun Context.getDimensionPixelSize(@DimenRes idDimen: Int) = try {
    this.resources.getDimensionPixelSize(idDimen)
} catch (e: Resources.NotFoundException) {
    0
}
