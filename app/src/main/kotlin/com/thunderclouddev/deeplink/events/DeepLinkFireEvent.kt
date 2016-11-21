package com.thunderclouddev.deeplink.events

import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.models.ResultType

class DeepLinkFireEvent(val resultType: ResultType, val info: DeepLinkInfo) {
    enum class FAILURE_REASON {
        NO_ACTIVITY_FOUND,
        IMPROPER_URI,
        UNKNOWN
    }

    var failureReason: FAILURE_REASON = FAILURE_REASON.UNKNOWN

    constructor(resultType: ResultType, deepLinkInfo: DeepLinkInfo, failureReason: FAILURE_REASON)
            : this(resultType, deepLinkInfo) {
        this.failureReason = failureReason
    }
}