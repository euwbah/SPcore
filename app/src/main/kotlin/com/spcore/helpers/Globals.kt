package com.spcore.helpers

internal const val SPLASH_SCREEN_MIN_DUR: Long = 500


internal const val HARDCODE_MODE = true

// Broadcasts intent names

/** Upon successful ATS entry */
internal const val BROADCAST_ATS_SUCCESS = "com.spcore.broadcast.ATS_SUCCESS"

/**
 * Upon failed ATS entry
 *
 * Intent parameters:
 * - `error`: String error message
 */
internal const val BROADCAST_ATS_FAILURE = "com.spcore.broadcast.ATS_FAILURE"

/**
 * Broadcast this message to get a Snackbar to appear in whichever activity
 * is active.
 *
 * Intent parameters:
 * - `type`: String - The specific snackbar to show. Possible values are:
*      - "ats error"
 *          - `errmsg`: String - Error message
 */
internal const val BROADCAST_CREATE_SNACKBAR = "com.spcore.broadcast.CREATE_SNACKBAR"