package com.thunderclouddev.deeplink.events

import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest

// TODO: Instead of using events, we should call the business logic directly to create the item
data class DeepLinkLaunchedEvent(val createDeepLinkRequest: CreateDeepLinkRequest)