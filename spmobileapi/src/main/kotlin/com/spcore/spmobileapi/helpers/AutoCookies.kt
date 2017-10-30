package com.spcore.spmobileapi.helpers

import android.content.SharedPreferences
import android.net.Uri
import okhttp3.Interceptor
import okhttp3.Response

/*
    Helpers in this file allows the Retrofit/HttpOk3 to automatically tryRetrieveElse cookies
    by saving cookies (in KVP map form) taken from Set-Cookie response headers and
    requesting with the same set of cookies.

    When a response is received, the CookiesRecInterceptor will scan the Set-Cookie header
    and update the CookieStore with all KVPs present in that response

    When a request is about to be made, the CookiesAddInterceptor will add the
    Cookie header to the request, with all the cookies present in the CookieStore

    The CookieStore utilizes SharedPreferences to store cookies like how a
    browser would store them on a computer.

    As a precaution to take for this extremely haxxx, unrobust, SP-building architecture like
    solution, it is CRUCIAL to provide an option in the Settings Activity
    to clear the App Cache, in the event that some cookies are corrupted / something
    wrong with this whole system.

    Kudos to @twiceyuan for the original Java version of this
    https://gist.github.com/twiceyuan/909331b966a63cf9538985e697e2c148
 */

class CookiesAddInterceptor(private val cookieStore: CookieStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().toString()
        val host = Uri.parse(url).host
        val builder = chain.request().newBuilder()
        val preferences = cookieStore.getCookies(host)

        preferences?.let {
            for (cookie in it)
                builder.addHeader("Cookie", cookie)
        }

        return chain.proceed(builder.build())
    }
}

class CookiesRecInterceptor(private val cookieStore: CookieStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().toString()
        val host = Uri.parse(url).host
        val originalResponse = chain.proceed(chain.request())

        originalResponse.headers("Set-Cookie").let {
            if (it.isNotEmpty())
                cookieStore.setCookies(host, HashSet(it))
        }

        return originalResponse
    }
}

class CookieStore(private val cookieSP: SharedPreferences) {
    fun setCookies(host: String, cookies: Set<String>) {
        synchronized(this) {
            cookieSP.edit().putStringSet(host, cookies).apply()
        }
    }

    fun getCookiesString(host: String): String {
        synchronized(this) {
            return cookieSP.getStringSet(host, null)
                    ?.reduce { acc, x -> "$acc; $x"} ?: ""
        }
    }

    fun getCookies(host: String): Set<String>? {
        synchronized(this) {
            return cookieSP.getStringSet(host, null)
        }
    }
}