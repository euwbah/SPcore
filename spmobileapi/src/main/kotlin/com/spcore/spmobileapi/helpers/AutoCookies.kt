package com.spcore.spmobileapi.helpers

/*
    Helpers in this file allows the Retrofit/HttpOk3 to automatically handle cookies
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
 */

