package com.thunderclouddev.deeplink.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable


data class DeepLinkInfo(val deepLink: Uri,
                        val activityLabel: String,
                        val packageName: String,
                        val name: String? = null,
                        val updatedTime: Long) : Comparable<DeepLinkInfo>, Parcelable {
    //Deep link without params itself is the unique identifier for the model
    val id: String

    init {
        // TODO: include id in here. DO NOT generate id every time
        id = generateId()
    }

    override fun compareTo(other: DeepLinkInfo) = if (this.updatedTime < other.updatedTime) 1 else -1

    // unique id for each deep link entry. similar deep links, varying in query or fragments are combined
    private fun generateId(): String {
        var id = deepLink.toString()
        if (deepLink.fragment != null) {
            id = id.replace(deepLink.fragment, "").replace("#", "")
        }
        if (deepLink.query != null) {
            id = id.replace(deepLink.query, "").replace("?", "")
        }
        id = id.replace("/", "")
        //replace '.' since firebase does not support them in paths
        id = id.replace(".", "-dot-")
        return id
    }

    // Generated [Parcelable] implementation below
    constructor(input: Parcel) : this(
            deepLink = input.readValue(Uri::class.java.classLoader) as Uri,
            activityLabel = input.readString(),
            packageName = input.readString(),
            name = input.readString(),
            updatedTime = input.readLong()) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(deepLink)
        dest.writeString(activityLabel)
        dest.writeString(packageName)
        dest.writeString(name)
        dest.writeLong(updatedTime)
    }

    val CREATOR: Parcelable.Creator<DeepLinkInfo> = object : Parcelable.Creator<DeepLinkInfo> {
        override fun createFromParcel(`in`: Parcel): DeepLinkInfo {
            return DeepLinkInfo(`in`)
        }

        override fun newArray(size: Int): Array<DeepLinkInfo?> {
            return arrayOfNulls(size)
        }
    }
}
