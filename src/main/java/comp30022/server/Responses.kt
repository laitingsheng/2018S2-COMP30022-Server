package comp30022.server

import java.io.Serializable

data class TokenResponse(
    val identity: String, val token: String
) : Serializable
