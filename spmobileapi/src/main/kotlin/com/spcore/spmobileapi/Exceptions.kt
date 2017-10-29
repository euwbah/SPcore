package com.spcore.spmobileapi

import okhttp3.ResponseBody

class SPMobileAPIUninitializedException :
        UnsupportedOperationException("SPMobileAPI can't be used until initialize() is called!!!")

class NoInternetException : Exception()
class ErroneousResponseException(val error: ResponseBody?) : Exception()

/**
 * When exception is thrown, retry the operation
 */
class UnexpectedAPIException(val errorType: UnexpectedAPIError,
                             override val message: String = "") : Exception(message) {
    enum class UnexpectedAPIError {
        NO_RESPONSE_BODY
    }
}