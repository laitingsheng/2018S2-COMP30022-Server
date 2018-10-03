package com30022.server.twilio

import com.twilio.jwt.accesstoken.*

private const val TWILIO_APP_NAME = "GUGUGU"
private const val TWILIO_FIREBASE_PUSH_CREDENTIAL = "CR32258191da074808a5975001f0bdc79b"
private const val TWILIO_ACCOUNT_SID = "ACc564c5adcb834b833daff618d73d8a2e"
private const val TWILIO_AUTH_TOKEN = "e5353cd242105dc49524a537330b5a85"
private const val TWILIO_API_KEY = "SKaebd656fa23853424d9edf7ac792e8ef"
private const val TWILIO_API_SECRET = "RFlJSI0ou4lboaiPyYo7RdL3qxZ6axem"
private const val TWILIO_CHAT_SERVICE_SID = "ISf316b89375a04ec8ab22b3f7e6881625"

private abstract class TokenGenerator protected constructor() {

    abstract operator fun invoke(uid: String, device: String): AccessToken

    fun buildToken(grant: Grant, uid: String): AccessToken {
        return AccessToken.Builder(TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET).identity(uid).grant(grant)
            .build()
    }
}

private class ChatTokenGenerator : TokenGenerator() {
    override operator fun invoke(uid: String, device: String): AccessToken {
        val grant = ChatGrant()
        grant.serviceSid = TWILIO_CHAT_SERVICE_SID
        grant.pushCredentialSid = TWILIO_FIREBASE_PUSH_CREDENTIAL
        grant.endpointId = "$TWILIO_APP_NAME:$uid:$device"

        return buildToken(grant, uid)
    }
}

private class VoiceTokenGenerator : TokenGenerator() {
    override operator fun invoke(uid: String, device: String): AccessToken {
        val grant = VoiceGrant()
        // grant.setOutgoingApplication()
        TODO("finalise implementation")

        return buildToken(grant, uid)
    }
}

private class VideoTokenGenerator : TokenGenerator() {
    override operator fun invoke(uid: String, device: String): AccessToken {
        val grant = VideoGrant()
        // grant.setOutgoingApplication()
        TODO("finalise implementation")

        return buildToken(grant, uid)
    }
}

private val tokenGenerators = hashMapOf(
    "chat" to ChatTokenGenerator(), "voice" to VoiceTokenGenerator(), "video" to VideoTokenGenerator()
)

fun generateToken(type: String, uid: String, device: String): AccessToken {
    return tokenGenerators[type]!!(uid, device)
}
