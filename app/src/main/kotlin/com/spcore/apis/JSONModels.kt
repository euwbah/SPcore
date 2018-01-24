package com.spcore.apis

// class LoginResponse(val token: String, val isInitial: Boolean)
class LoginResponse(val token: String, val username: String?, val displayName: String?)

class StringResponse(val response: String   )