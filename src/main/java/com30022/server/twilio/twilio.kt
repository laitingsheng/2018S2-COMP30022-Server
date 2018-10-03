package com30022.server.twilio

private interface TokenGenerator {
    operator fun invoke(): String
}

private class ChatTokenGenerator : TokenGenerator {
    override operator fun invoke(): String {
        TODO("not implemented")
    }
}

private class VoiceTokenGenerator : TokenGenerator {
    override operator fun invoke(): String {
        TODO("not implemented")
    }
}

private class VideoTokenGenerator : TokenGenerator {
    override operator fun invoke(): String {
        TODO("not implemented")
    }
}

private val tokenGenerators = hashMapOf(
    "chat" to ChatTokenGenerator(),
    "voice" to VoiceTokenGenerator(),
    "video" to VideoTokenGenerator()
)

fun generateToken(type: String): String {
    return tokenGenerators[type]!!()
}
