package com.nearbychen.flutter_hpplay.utils

import android.util.Log
import io.flutter.BuildConfig

/**
 * Created by Zippo on 2018/5/26.
 * Date: 2018/5/26
 * Time: 11:37:49
 */
private const val TAG = "xyzeng"
var isDebug = false
fun logV(msg: String?) {
    val message = formatMessage(msg)
    if (isDebug)
        Log.v(TAG, message)
}

fun logD(msg: String?) {
    val message = formatMessage(msg)
    if (isDebug) Log.d(TAG, message)
}

fun logI(msg: String?) {
    val message = formatMessage(msg)
    if (isDebug) Log.i(TAG, message)
}

fun logW(msg: String?) {
    val message = formatMessage(msg)
    if (isDebug) Log.w(TAG, message)
}

fun logW(msg: String?, tr: Throwable?) {
    val message = formatMessage(msg)
    if (isDebug) Log.w(TAG, message, tr)
}

fun logW(tr: Throwable?) {
    val message = formatMessage(null)
    if (isDebug) Log.w(TAG, message, tr)
}

fun logE(msg: String?) {
    val message = formatMessage(msg)
    if (isDebug) Log.e(TAG, message)
}

fun logE(msg: String?, tr: Throwable?) {
    val message = formatMessage(msg)
    if (isDebug) Log.e(TAG, message, tr)
}

fun logTest(msg: String?) {
    val message = formatMessage(msg)
    if (isDebug) Log.i(TAG, message)
}

private fun formatMessage(msg: String?): String {
    var msg = msg
    if (msg == null) {
        msg = ""
    }
    var ret = "xyzeng:$msg"
    ret = "[" + Thread.currentThread().name + "]:" + ret
    return ret
}