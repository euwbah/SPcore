package com.spcore.spmobileapi.interceptors

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
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

private object Foos {
    fun addCookiesToRequest(request: Request, cookies: Set<String>?, deleteBody: Boolean = false) =
            addCookiesToRequest(request.newBuilder(), cookies, deleteBody)

    fun addCookiesToRequest(builder: Request.Builder, cookies: Set<String>?, deleteBody: Boolean = false): Request {
        builder.removeHeader("Cookie")
        cookies?.let {
            for(cookie in it) {
                Log.d("ME AutoCookies ADD", cookie)

                builder.addHeader("Cookie", cookie)
            }
        }

        return builder.build()
    }

    fun saveCookiesFromResponse(response: Response, cookieStore: CookieStore, host: String) {
        response.headers("Set-Cookie").let {
            Log.d("ME AutoCookies REC", it.toString())
            if (it.isNotEmpty())
                cookieStore.setCookies(host, HashSet(it))
        }
    }
}

internal class CookiesAddInterceptor(private val cookieStore: CookieStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().toString()
        val host = Uri.parse(url).host

        Log.d("ME Requesting", chain.request().toString())

        return chain.proceed(Foos.addCookiesToRequest(chain.request(), cookieStore.getCookies(host)))
    }
}

internal class CookiesRecInterceptor(private val cookieStore: CookieStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().toString()
        val host = Uri.parse(url).host
        val request = chain.request()
        val originalResponse = chain.proceed(request)

        Log.d("ME Responded", originalResponse.toString())

        Foos.saveCookiesFromResponse(originalResponse, cookieStore, host)

        // Recurse all redirects and handle cookies
        var currResp = originalResponse

        while(currResp.code() == 302) {

            val gg = currResp.header("Location")?.let {
                Log.d("ME AutoCookies REDIRECT", it)

                val redirectBuilder =
                        request.newBuilder()
                                .url(it)

                val cookies = cookieStore.getCookies(host)

                currResp = chain.proceed(
                        Foos.addCookiesToRequest(redirectBuilder, cookies))

                Foos.saveCookiesFromResponse(currResp,  cookieStore, host)

                false
            } ?: run {
                Log.w("ME AutoCookies REDIRECT", "ABNORMAL BEHAVIOR: 302 HTTP code, but no 'Location' header!")
                true
            }

            if (gg) break
        }

        return currResp
    }
}

internal class CookieStore(private val cookieSP: SharedPreferences) {
    private class Cookie(val rawStr: String) {
        val key: String
        val parts: List<String>

        init {
            parts = rawStr.split(";").map {it.trim()}
            key = parts[0].split("=")[0].trim()
        }

        override fun toString(): String {
            return this.rawStr
        }
    }

    fun clearCookies() {
        synchronized(this) {
            cookieSP.edit().clear().apply()
        }
    }

    fun setCookies(host: String, newCookiesStrs: Set<String>) {
        synchronized(this) {
            val oldCookiesStrs = getCookies(host)
            val oldCookies = oldCookiesStrs?.map { Cookie(it) }
            val newCookies = newCookiesStrs.map{ Cookie(it) }

            val remaining =
                    oldCookies?.filter { old ->
                        !newCookies.any { it.key == old.key }
                    }

            val aggregate =
                    remaining?.let {
                        it + newCookies
                    } ?: newCookies

            val stringSet = aggregate.map{it.rawStr}.toHashSet()

            cookieSP.edit().putStringSet(host, stringSet).apply()
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