package com.manoj.dlt.models;

public class DeepLinkInfo
{
    private String _activityName;
    private String _activitylabel;
    private String _packageName;
    private int _iconRes;
    private String _deepLink;

    public DeepLinkInfo(String activityName, String activityLabel, String packageName, int iconRes, String deepLink)
    {
        _activityName = activityName;
        _activitylabel = activityLabel;
        _packageName = packageName;
        _iconRes = iconRes;
        _deepLink = deepLink;
    }
    //Deep link itself is the unique identifier for the model
    public String getId()
    {
        return _deepLink;
    }

    public String getActivityName()
    {
        return _activityName;
    }

    public String getPackageName()
    {
        return _packageName;
    }

    public String getActivityLabel()
    {
        return _activitylabel;
    }

    public String getDeepLink()
    {
        return _deepLink;
    }

    public int getIconRes()
    {
        return _iconRes;
    }
}
