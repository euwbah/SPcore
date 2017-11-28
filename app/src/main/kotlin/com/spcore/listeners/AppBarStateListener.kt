package com.spcore.listeners

import android.os.Handler
import android.support.design.widget.AppBarLayout

fun AppBarStateListener(onOffsetChanged: (AppBarStateListener.State, AppBarStateListener.State) -> Unit) : AppBarStateListener {
    return object : AppBarStateListener() {
        override fun onOffsetChanged(state: State, prevStableState: State) {
            onOffsetChanged(state, prevStableState)
        }
    }
}

abstract class AppBarStateListener : AppBarLayout.OnOffsetChangedListener {

    /**
     * The amount of ms to wait before considering it stuck
     */
    private val STUCK_THRESH = 75L

    private var prevExpandedness = 0.0
    private var prevMS = System.currentTimeMillis()

    private var scheduler = Handler()

    private var inTheMidstOfPreventingAWormholeCollapse = false

    private var previousStableState: State = State.COLLAPSED

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val currMS = System.currentTimeMillis()
        val range = appBarLayout.totalScrollRange
        val expandedness = 1.0 * (verticalOffset + range) / range

        // Passing null will remove everything that was scheduled
        scheduler.removeCallbacksAndMessages(null)

        val delta = (expandedness - prevExpandedness) / ((currMS - prevMS) * 1000.0)

        val currState = when (expandedness) {
            0.0 -> State.COLLAPSED
            1.0 -> State.EXPANDED
            else -> State.QUANTUM_FLUX_SUPERPOSITION(expandedness)
        }

        onOffsetChanged(currState, previousStableState)

        if (currState !is State.QUANTUM_FLUX_SUPERPOSITION) {
            inTheMidstOfPreventingAWormholeCollapse = false
            previousStableState = currState
        }

        prevExpandedness = expandedness
        prevMS = currMS

        val performThisTaskIfStationary = {
            if(currState is State.QUANTUM_FLUX_SUPERPOSITION && !inTheMidstOfPreventingAWormholeCollapse) {
                inTheMidstOfPreventingAWormholeCollapse = true
                onOffsetChanged(State.STUCK_IN_FUTURE_TIMELINE_SUPERPOSITION(expandedness), previousStableState)
            }
        }

        scheduler.postDelayed(performThisTaskIfStationary, STUCK_THRESH)
    }

    /**
     * @param state The current state the AppBarLayout is in
     * @param prevStableState The previous normal state the AppBarLayout was in.
     *                        This can only be either one of the `COLLAPSED` or the `EXPANDED` states
     */
    abstract fun onOffsetChanged(state: State, prevStableState: State)


    sealed class State {
        /**
         * Represents the expanded state of the AppBar
         */
        object EXPANDED : State()

        /**
         * Represents the collapsed state of the AppBar
         */
        object COLLAPSED : State()

        /**
         *
         * Represents the moving state of the AppBar
         *
         * @property expandedness A number after 0 until 1 representing how much the AppBarLayout has expanded
         */
        class QUANTUM_FLUX_SUPERPOSITION(val expandedness: Double) : State()

        /**
         * Represents the stationary state of the AppBar
         *
         * More specifically, it is only considered stationary if no onOffsetChanged events
         * were received for a duration of STUCK_THRESH in milliseconds
         *
         * @property originalExpandedness A number after 0 until 1 representing how much the AppBarLayout
         *                                has expanded before it became stationary
         */
        class STUCK_IN_FUTURE_TIMELINE_SUPERPOSITION(val originalExpandedness: Double) : State()

//        /**
//         *
//         * Represents approaching the idling state of the AppBar
//         *
//         * More specifically, when the timed-delta of expandedness is less than or equal to
//         * `MOVING_DELTA_THRESH`
//         *
//         * @property expandedness a number after 0 until 1 representing how much the AppBarLayout has expanded
//         */
//        class GOING_TO_IDLE(val expandedness: Double) : State()

    }


}