package com.nearbychen.flutter_hpplay

import com.nearbychen.flutter_hpplay.bean.MessageDeatail

/**
 * Created by Zippo on 2018/5/16.
 * Date: 2018/5/16
 * Time: 14:44:20
 */
interface IUIUpdateListener {
    fun onUpdate(what: Int, deatail: MessageDeatail?)

    companion object {
        const val STATE_SEARCH_SUCCESS = 1
        const val STATE_SEARCH_ERROR = 2
        const val STATE_SEARCH_NO_RESULT = 3
        const val STATE_CONNECT_SUCCESS = 10
        const val STATE_DISCONNECT = 11 // 连接断开
        const val STATE_CONNECT_FAILURE = 12 // 连接失败
        const val STATE_PLAY = 20
        const val STATE_PAUSE = 21
        const val STATE_COMPLETION = 22
        const val STATE_STOP = 23
        const val STATE_SEEK = 24
        const val STATE_POSITION_UPDATE = 25
        const val STATE_PLAY_ERROR = 26
        const val STATE_LOADING = 27
        const val STATE_INPUT_SCREENCODE = 28
        const val RELEVANCE_DATA_UNSUPPORT = 29
        const val STATE_SCREENSHOT = 30
    }
}