package com30022.server.exception

class InvalidTokenType(type: String) : RuntimeException("There is no token of type '$type'")
