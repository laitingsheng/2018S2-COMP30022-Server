package com30022.server.twilio

interface TokenGenerator {
    operator fun invoke(): String
}

class ChatTokenGenerator : TokenGenerator {
    override operator fun invoke(): String {
        TODO("not implemented")
    }
}
