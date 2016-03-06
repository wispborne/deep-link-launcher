package com.manoj.dlt.models;

import android.net.Uri;
import org.json.JSONException;
import org.json.JSONObject;

public class DeepLinkInfo implements Comparable
{
    private String _activityName;
    private String _activitylabel;
    private String _packageName;
    private int _iconRes;
    private String _deepLink;
    private String _id;
    private long _updatedTime; //Milliseconds

    public DeepLinkInfo(String deepLink, String activityName, String activityLabel, String packageName, int iconRes, long updatedTime)
    {
        _activityName = activityName;
        _activitylabel = activityLabel;
        _packageName = packageName;
        _iconRes = iconRes;
        _deepLink = deepLink;
        _updatedTime = updatedTime;
        _id = generateId();
    }

    //Deep link without params itself is the unique identifier for the model
    public String getId()
    {
        return _id;
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

    public long getUpdatedTime()
    {
        return _updatedTime;
    }

    public static String toJson(DeepLinkInfo deepLinkInfo)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_KEYS.KEY_DEEP_LINK, deepLinkInfo.getDeepLink());
            jsonObject.put(JSON_KEYS.KEY_ACTIVITY_NAME, deepLinkInfo.getActivityName());
            jsonObject.put(JSON_KEYS.KEY_ACTIVITY_LABEL, deepLinkInfo.getActivityLabel());
            jsonObject.put(JSON_KEYS.KEY_PACKAGE_NAME, deepLinkInfo.getPackageName());
            jsonObject.put(JSON_KEYS.KEY_ICON_RESOURCE, deepLinkInfo.getIconRes());
            jsonObject.put(JSON_KEYS.KEY_UPDATED_TIME, deepLinkInfo.getUpdatedTime());
            return jsonObject.toString();
        } catch (JSONException jsonException)
        {
            return deepLinkInfo.getId();
        }
    }

    public static DeepLinkInfo fromJson(String deepLinkJson)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(deepLinkJson);
            String deepLink = jsonObject.getString(JSON_KEYS.KEY_DEEP_LINK);
            String activityName = jsonObject.getString(JSON_KEYS.KEY_ACTIVITY_NAME);
            String activityLable = jsonObject.getString(JSON_KEYS.KEY_ACTIVITY_LABEL);
            String packageName = jsonObject.getString(JSON_KEYS.KEY_PACKAGE_NAME);
            int iconResId = jsonObject.getInt(JSON_KEYS.KEY_ICON_RESOURCE);
            long updatedTime = jsonObject.getLong(JSON_KEYS.KEY_UPDATED_TIME);
            return new DeepLinkInfo(deepLink, activityName, activityLable, packageName, iconResId, updatedTime);
        } catch (JSONException jsonException)
        {
            return null;
        }
    }

    @Override
    public int compareTo(Object o)
    {
        if (o == null || !(o instanceof DeepLinkInfo))
        {
            return -1;
        }
        DeepLinkInfo that = (DeepLinkInfo) o;
        if (this.getUpdatedTime() < that.getUpdatedTime())
        {
            return -1;
        } else
        {
            return 1;
        }
    }

    private String generateId()
    {
        Uri uri = Uri.parse(_deepLink);
        String id = uri.toString();
        if (uri.getFragment() != null)
        {
            id = id.replace(uri.getFragment(), "").replace("#", "");
        }
        if (uri.getQuery() != null)
        {
            id = id.replace(uri.getQuery(), "").replace("?", "");
        }
        return id;
    }

    private static class JSON_KEYS
    {
        public static String KEY_DEEP_LINK = "deep_link";
        public static String KEY_ACTIVITY_NAME = "activity_name";
        public static String KEY_PACKAGE_NAME = "pacakage_name";
        public static String KEY_ACTIVITY_LABEL = "label";
        public static String KEY_ICON_RESOURCE = "icon_res";
        public static String KEY_UPDATED_TIME = "update_time";
    }
}
