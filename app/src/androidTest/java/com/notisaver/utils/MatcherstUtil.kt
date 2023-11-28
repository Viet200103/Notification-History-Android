package com.notisaver.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


internal fun atPosition(position: Int, @NonNull itemMatcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val viewHolder = view.findViewHolderForAdapterPosition(position)
                ?: // has no item on such position
                return false
            return itemMatcher.matches(viewHolder.itemView)
        }
    }
}

fun clickChildViewWithId(id: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return null
        }

        override fun getDescription(): String {
            return "Click on a child view with specified id."
        }

        override fun perform(uiController: UiController?, view: View) {
            val v = view.findViewById<View>(id)
            v.performClick()
        }
    }
}

class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }

        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        ViewMatchers.assertThat(adapter!!.itemCount, `is`(expectedCount))
    }
}

internal fun setState(@BottomSheetBehavior.State state: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isBottomSheet()
        }

        override fun getDescription(): String {
            return "set state to $state"
        }

        override fun perform(uiController: UiController, view: View) {
            BottomSheetBehavior.from(view).state = state
        }
    }
}

internal fun isBottomSheet() = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("is a bottom sheet");
    }

    override fun matchesSafely(item: View?): Boolean {
        return item?.let {
            BottomSheetBehavior.from(it)
        } != null
    }
}

class DrawableMatcher internal constructor(private val expectedId: Int) :
    TypeSafeMatcher<View>(View::class.java) {
    private var resourceName: String? = null
    override fun matchesSafely(target: View): Boolean {
        if (target !is ImageView) {
            return false
        }
        val imageView: ImageView = target
        if (expectedId == EMPTY) {
            return imageView.drawable == null
        }
        if (expectedId == ANY) {
            return imageView.drawable != null
        }
        val resources: Resources = target.context.resources
        val expectedDrawable: Drawable? = ContextCompat.getDrawable(target.context, expectedId)
        resourceName = resources.getResourceEntryName(expectedId)
        if (expectedDrawable == null) {
            return false
        }
        val bitmap = getBitmap(imageView.drawable)
        val otherBitmap = getBitmap(expectedDrawable)
        return bitmap.sameAs(otherBitmap)
    }


    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ")
        description.appendValue(expectedId)
        if (resourceName != null) {
            description.appendText("[")
            description.appendText(resourceName)
            description.appendText("]")
        }
    }

    companion object {
        const val EMPTY = -1
        const val ANY = -2
    }
}

private fun getBitmap(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}


fun withDrawable(resourceId: Int): Matcher<View?> {
    return DrawableMatcher(resourceId)
}

fun noDrawable(): Matcher<View?> {
    return DrawableMatcher(DrawableMatcher.EMPTY)
}

fun hasDrawable(): Matcher<View?> {
    return DrawableMatcher(DrawableMatcher.ANY)
}

fun withIndex(matcher: Matcher<View>,  index: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        var currentIndex = 0

        override fun describeTo(description: Description) {
            description.appendText("with index: ");
            description.appendValue(index);
            matcher.describeTo(description);
        }

        override fun matchesSafely(view: View): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}
