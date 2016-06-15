package com.manoj.dlt.events;

import com.manoj.dlt.models.DeepLinkInfo;
import com.manoj.dlt.models.ResultType;

public class DeepLinkFireEvent {

    public enum FAILURE_REASON
    {
        NO_ACTIVITY_FOUND, IMPROPER_URI
    }

    private ResultType _resultType;
    private DeepLinkInfo _deepLinkInfo;
    private FAILURE_REASON _failureReason;

    public DeepLinkFireEvent(ResultType resultType, DeepLinkInfo info)
    {
        _deepLinkInfo = info;
        _resultType = resultType;
    }

    public DeepLinkFireEvent(ResultType resultType, DeepLinkInfo deepLinkInfo, FAILURE_REASON failureReason)
    {
        _deepLinkInfo = deepLinkInfo;
        _failureReason = failureReason;
        _resultType = resultType;
    }

    public FAILURE_REASON getFailureReason()
    {
        return _failureReason;
    }

    public DeepLinkInfo getDeepLinkInfo()
    {
        return _deepLinkInfo;
    }

    public ResultType getResultType()
    {
        return _resultType;
    }

}
