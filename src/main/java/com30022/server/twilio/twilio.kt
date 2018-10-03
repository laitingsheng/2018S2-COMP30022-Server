package com30022.server.twilio

import com.twilio.jwt.accesstoken.AccessToken

data class TokenResponse(
    val identity: String, val token: AccessToken
)
