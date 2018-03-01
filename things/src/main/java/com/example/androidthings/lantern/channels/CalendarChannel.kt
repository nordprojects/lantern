package com.example.androidthings.lantern.channels

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import biweekly.Biweekly
import biweekly.component.VEvent
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R
import java.io.InputStream
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.fixedRateTimer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.calendar_clock_channel.*

/**
 * Shows a day's appointments around a real-world clock.
 *
 * Config parameters:
 *   - "url"
 *       The URL to a public calendar in the iCal format.
 */
@SuppressLint("RtlHardcoded")
class CalendarChannel : Channel() {
    private var events by Delegates.observable<List<Event>?>(null) {
        _, old, new ->
        if (old != new) {
            recreateEventViews()
        }
    }
    private var refreshError: Exception? = null
    private val textViews = mutableListOf<TextView>()
    private val notchViews = mutableListOf<View>()
    private var visibilityAnimations = listOf<OverlappingVisibilityAnimation>()

    private var refreshEventsTimer: Timer? = null
    private val iCalURL: URL? by lazy {
        val urlString = config.secrets?.opt("url") as? String
        urlString?.let { URL(it) }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.calendar_clock_channel, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recreateEventViews()
    }

    override fun onStart() {
        super.onStart()
        refreshEventsTimer = fixedRateTimer("$this refresh", false, Date(), 10000) {
            Handler(Looper.getMainLooper()).post({
                update()
                refreshEvents()
            })
        }
    }

    override fun onStop() {
        refreshEventsTimer?.cancel()
        refreshEventsTimer = null
        super.onStop()
    }

    private fun refreshEvents() {
        val url = iCalURL ?: return
        RefreshEventsTask().also {
            it.onComplete = { events, error ->
                refreshError = error

                if (events == null) {
                    Log.e(TAG, "null result from RefreshEventsTask. $error")
                } else {
                    this.events = events
                }
            }
        }.execute(url)
    }

    private fun recreateEventViews() {
        Log.d(TAG, "Events: $events")

        if (eventsView == null) return

        val notchRadius = eventsView.height/2.0f * 0.73f
        val textRadius = eventsView.height/2.0f * 0.78f

        eventsView.removeAllViews()
        textViews.clear()
        notchViews.clear()
        visibilityAnimations.forEach { it.stop() }

        val events = events ?: return

        for (event in events) {
            val textView = TextView(context).apply {
                typeface = resources.getFont(R.font.pt_sans)
                textSize = 24f
                setTextColor(Color.WHITE)
                maxLines = 2
                text = event.name
                gravity = gravityForAngle(event.angleDeg)
            }
            val notchView = View(context).apply {
                setBackgroundColor(Color.WHITE)
            }
            eventsView.addView(textView, RelativeLayout.LayoutParams(TEXT_VIEW_WIDTH, TEXT_VIEW_HEIGHT).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            })
            eventsView.addView(notchView, RelativeLayout.LayoutParams(3, 22).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            })

            val widthOffset = when(horizontalGravityForAngle(event.angleDeg)) {
                Gravity.LEFT -> 100
                Gravity.RIGHT -> -100
                else -> 0
            }
            val heightOffset = when(verticalGravityForAngle(event.angleDeg)) {
                Gravity.TOP -> 35
                Gravity.BOTTOM -> -35
                else -> 0
            }

            textView.translationX = textRadius * sin(event.angle) + widthOffset
            textView.translationY = textRadius * -cos(event.angle) + heightOffset
            notchView.translationX = notchRadius * sin(event.angle)
            notchView.translationY = notchRadius * -cos(event.angle)
            notchView.rotation = event.angleDeg

            textViews.add(textView)
            notchViews.add(notchView)
        }

        visibilityAnimations = animationsForOverlappingGroupsOfViews(textViews)
        visibilityAnimations.forEach { it.start() }

        update()
    }

    private fun update() {
        if (view == null) return

        val events = events

        if (events != null) {
            // add strikethough to textviews whose events are in the past
            val now = Date()
            for ((index, event) in events.withIndex()) {
                val textView = textViews[index]

                if (event.startDate < now) {
                    // add strikethrough flag
                    textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    // remove strikethrough flag
                    textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }

        // update status text
        statusTextView.text = if (events == null && refreshError != null) {
            "An error occurred while getting calendar events.\n\n$refreshError"
        } else if (events == null) {
            "Loading..."
        } else if (events.isEmpty()) {
            "No events."
        } else {
            ""
        }
    }

    private fun gravityForAngle(deg: Float): Int {
        val horizontal = horizontalGravityForAngle(deg)
        val vertical = verticalGravityForAngle(deg)
        return (horizontal or vertical)
    }

    private fun verticalGravityForAngle(deg: Float): Int {
        return when (deg) {
            in 0..60 -> Gravity.BOTTOM
            in 60..120 -> Gravity.CENTER_VERTICAL
            in 120..240 -> Gravity.TOP
            in 240..300 -> Gravity.CENTER_VERTICAL
            in 300..360 -> Gravity.BOTTOM
            else -> Gravity.CENTER_VERTICAL
        }
    }

    private fun horizontalGravityForAngle(deg: Float): Int {
        return when (deg) {
            in 0..30 -> Gravity.CENTER_HORIZONTAL
            in 30..150 -> Gravity.LEFT
            in 150..210 -> Gravity.CENTER_HORIZONTAL
            in 210..330 -> Gravity.RIGHT
            else -> Gravity.CENTER_HORIZONTAL
        }
    }

    private fun animationsForOverlappingGroupsOfViews(views: Collection<View>): List<OverlappingVisibilityAnimation> {
        // Split the views into groups that are 'touching' - that overlap. For example:
        // ┌---------┐                                          ┌---------┐
        // │    A ┌--┼------┐                       ┌---------┐ │    G    │
        // └------┼--┘ B ┌--┼------┐        ┌-------┼-┐  F    │ └---------┘
        //        └------┼--┘ C ┌--┼------┐ │    E  └-┼-------┘
        //               └------┼--┘ D    │ └---------┘
        //                      └---------┘
        // In this example, the groups are [A,B,C,D] and [E,F]. 'G' is ignored
        // because it doesn't overlap anything, so doesn't need animating.

        val ungroupedTextViews = views.toMutableList()
        val overlappingGroups = mutableListOf<Set<View>>()

        // make sure the views know their sizes
        textViews.forEach { it.measure(TEXT_VIEW_WIDTH, TEXT_VIEW_HEIGHT) }

        while (ungroupedTextViews.count() > 0) {
            val group = mutableSetOf(ungroupedTextViews.first())
            for (textView in ungroupedTextViews) {
                if (group.any { viewsIntersect(it, textView) }) {
                    // this view intersects with one of the views in this group, so make it
                    // part of the group.
                    group.add(textView)
                }
            }
            ungroupedTextViews.removeAll(group)
            overlappingGroups.add(group)
        }

        // groups with just one view can be ignored, no animations necessary
        overlappingGroups.removeAll { it.count() < 2 }

        return overlappingGroups.map { OverlappingVisibilityAnimation(it) }
    }

    private inner class OverlappingVisibilityAnimation(private val views: Collection<View>) {
        private val visibleSets: List<Set<View>>

        init {
            // Separate the views into visible sets. 'Visible sets' are sets of views
            // that don't intersect. For example:
            // ┌---------┐
            // │    A ┌--┼------┐
            // └------┼--┘ B ┌--┼------┐
            //        └------┼--┘ C ┌--┼------┐
            //               └------┼--┘ D    │
            //                      └---------┘
            // In this example, the sets are [A,C] and [B,D].
            //
            // Each view must be in at least one set. The sets are the visible views in
            // each step of the animation.

            val visibleSets = mutableListOf<Set<View>>()
            val iterationComplete = {
                // each text view is in at least one visible set
                 views.all { view ->
                     visibleSets.any { it.contains(view) }
                 }
            }

            while (!iterationComplete()) {
                val visibleSet = mutableSetOf<View>()
                val views = this.views.toMutableList()

                // sort the list such that views not already in a visible set are first, to
                // give them priority
                views.sortBy { view -> visibleSets.count { it.contains(view) } }

                // build the set by taking an element from the list one-by-one and adding it
                // if it doesn't overlap anything already in the set.
                for (view in views) {
                    val intersectsAViewInTheSet = visibleSet.any { viewsIntersect(it, view) }

                    if (!intersectsAViewInTheSet) {
                        visibleSet.add(view)
                    }
                }

                visibleSets.add(visibleSet)
            }

            this.visibleSets = visibleSets
        }

        private var step = 0
        private var animator: Animator? = null
        private val visibleSet get() = visibleSets[step]

        fun start() {
            stop()
            views.forEach { textView ->
                textView.alpha = if (visibleSet.contains(textView)) 1f else 0f
            }
            animateToNextStep()
        }

        fun animateToNextStep() {
            step += 1
            if (step >= visibleSets.count()) {
                step = 0
            }

            stop()
            animator = AnimatorSet().apply {
                startDelay = 5000
                playTogether(views.flatMap { textView ->
                    val textViewVisible = visibleSet.contains(textView)
                    val textViewAlpha = if (textViewVisible) 1f else 0f
                    val notchView = notchViewForTextView(textView)
                    val notchViewAlpha = if (textViewVisible) 1f else 0.5f
                    listOf(
                            ObjectAnimator.ofFloat(textView, "alpha", textViewAlpha).apply {
                                duration = 3000
                                interpolator = AccelerateDecelerateInterpolator()
                            },
                            ObjectAnimator.ofFloat(notchView, "alpha", notchViewAlpha).apply {
                                duration = 3000
                                interpolator = AccelerateDecelerateInterpolator()
                            }
                    )
                })
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator?) {
                        animateToNextStep()
                    }
                })
                start()
            }
        }

        fun stop() {
            animator?.removeAllListeners()
            animator?.end()
            animator = null
        }
    }

    private data class Event(val name: String, val startDate: Date) {
        companion object {
            val calendar: Calendar = Calendar.getInstance()
        }
        val angle: Float
        val angleDeg: Float

        init {
            calendar.time = startDate
            val hour = calendar.get(Calendar.HOUR_OF_DAY) % 12
            val minute = calendar.get(Calendar.MINUTE)
            angle = (hour + minute/60.0f) * 2.0f*PI.toFloat() / 12.0f
            angleDeg = angle * 180f/PI.toFloat()
        }
    }

    private fun viewsIntersect(view1: View, view2: View): Boolean {
        val view1Position = IntArray(2)
        view1.getLocationOnScreen(view1Position)
        val view2Position = IntArray(2)
        view2.getLocationOnScreen(view2Position)

        return Rect.intersects(
                Rect(
                        view1Position[0], view1Position[1],
                        view1Position[0] + TEXT_VIEW_WIDTH, view1Position[1] + TEXT_VIEW_HEIGHT
                ),
                Rect(
                        view2Position[0], view2Position[1],
                        view2Position[0] + TEXT_VIEW_WIDTH, view2Position[1] + TEXT_VIEW_HEIGHT
                )
        )
    }

    private fun notchViewForTextView(view: View): View? {
        val index = textViews.indexOf(view)
        if (index == -1) return null

        return notchViews.getOrNull(index)
    }

    private class RefreshEventsTask : AsyncTask<URL, Void, List<Event>?>() {
        var error: Exception? = null
        var onComplete: ((List<Event>?, Exception?) -> Unit)? = null

        override fun doInBackground(vararg params: URL?): List<Event>? {
            try {
                val url = params.first()!!

                val events = downloadEventsFromURL(url) ?: return null
                return filteredEventsWithinA12HourPeriod(events, Date())
            }
            catch (e: Exception) {
                error = e
                return null
            }
        }

        override fun onPostExecute(result: List<Event>?) {
            onComplete?.invoke(result, error)
        }

        private fun filteredEventsWithinA12HourPeriod(events: List<VEvent>, date: Date): List<Event> {
            val result = mutableListOf<Event>()
            // start the 12 hour period at 8am or 8pm, whichever is preceding `date`
            val calendar = Calendar.getInstance()
            calendar.time = date
            when (calendar.get(Calendar.HOUR_OF_DAY)) {
                in 0..8 -> {
                    // knock it back to 8pm the previous day
                    calendar.timeInMillis -= 12*60*60*1000
                    calendar.set(Calendar.HOUR_OF_DAY, 20)
                }
                in 8..20 -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 8)
                }
                else -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 20)
                }
            }
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            val startDate = calendar.time
            val endDate = Date(calendar.timeInMillis + 12*60*60*1000)

            for (event in events) {
                val dateIterator = event.getDateIterator(TimeZone.getDefault())
                dateIterator.advanceTo(startDate)

                // if the next occurrence is before the endDate, include this event
                if (dateIterator.hasNext()) {
                    val nextOccurrence = dateIterator.next()
                    if (nextOccurrence.before(endDate)) {
                        result.add(Event(event.summary.value, nextOccurrence))
                    }
                }
            }

            return result
        }

        private fun downloadEventsFromURL(url: URL): List<VEvent>? {
            var connection: HttpsURLConnection? = null
            var inputStream: InputStream? = null

            try {
                connection = url.openConnection() as HttpsURLConnection
                connection.connect()
                if (connection.responseCode !in 200..299) {
                    Log.e(TAG, "HTTP error ${connection.responseCode}")
                    return null
                }

                inputStream = connection.inputStream
                val calendar = Biweekly.parse(inputStream).first()
                return calendar.events
            }
            finally {
                inputStream?.close()
                connection?.disconnect()
            }
        }
    }

    companion object {
        private const val TEXT_VIEW_HEIGHT = 70
        private const val TEXT_VIEW_WIDTH = 200
        private val TAG = this::class.java.simpleName
    }
}
