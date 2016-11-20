package com.manoj.dlt.events

import com.manoj.dlt.models.DeepLinkInfo
import com.manoj.dlt.models.ResultType

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