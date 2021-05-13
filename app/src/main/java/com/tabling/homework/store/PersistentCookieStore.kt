package com.tabling.homework.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tabling.homework.api.SerializableHttpCookie
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class PersistentCookieStore(context: Context) : CookieStore {
    private val sharedPreferences: SharedPreferences
    // In memory
    private var allCookies: MutableMap<URI, Set<HttpCookie>>? = null

    private fun loadAllFromPersistence() {
        allCookies = HashMap()
        val allPairs = sharedPreferences.all
        for ((key, value) in allPairs) {
            val uriAndName = key.split(SP_KEY_DELIMITER_REGEX.toRegex(), 2).toTypedArray()
            try {
                val uri = URI(uriAndName[0])
                val encodedCookie = value as String
                var cookie: HttpCookie
                cookie = SerializableHttpCookie().decode(encodedCookie)!!
                var targetCookies: MutableSet<HttpCookie>? =
                    (allCookies as HashMap<URI, Set<HttpCookie>>)[uri] as MutableSet<HttpCookie>?
                if (targetCookies == null) {
                    targetCookies = HashSet()
                    (allCookies as HashMap<URI, Set<HttpCookie>>)[uri] = targetCookies
                }
                // Repeated cookies cannot exist in persistence
                // targetCookies.remove(cookie)
                targetCookies.add(cookie)
            } catch (e: URISyntaxException) {
                Log.w(TAG, e)
            } catch (e: KotlinNullPointerException) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    override fun add(uri: URI, cookie: HttpCookie) {
        var uri = uri
        uri = cookieUri(uri, cookie)
        var targetCookies: MutableSet<HttpCookie>? = allCookies!![uri] as MutableSet<HttpCookie>?
        if (targetCookies == null) {
            targetCookies = HashSet()
            allCookies!![uri] = targetCookies
        }
        targetCookies.remove(cookie)
        targetCookies.add(cookie)
        saveToPersistence(uri, cookie)
    }

    private fun saveToPersistence(uri: URI, cookie: HttpCookie) {
        val editor = sharedPreferences.edit()
        editor.putString(
            uri.toString() + SP_KEY_DELIMITER + cookie.name,
            SerializableHttpCookie().encode(cookie)
        )
        editor.apply()
    }

    @Synchronized
    override fun get(uri: URI): List<HttpCookie> {
        return getValidCookies(uri)
    }

    @Synchronized
    override fun getCookies(): List<HttpCookie> {
        val allValidCookies: MutableList<HttpCookie> =
            ArrayList()
        for (storedUri in allCookies!!.keys) {
            allValidCookies.addAll(getValidCookies(storedUri))
        }
        return allValidCookies
    }

    private fun getValidCookies(uri: URI): List<HttpCookie> {
        val targetCookies: MutableList<HttpCookie> = ArrayList()
        // If the stored URI does not have a path then it must match any URI in the same domain
        for (storedUri in allCookies!!.keys) { // Check ith the domains match according to RFC 6265
            if (checkDomainsMatch(
                    storedUri.host,
                    uri.host
                )
            ) { // Check if the paths match according to RFC 6265
                if (checkPathsMatch(storedUri.path, uri.path)) {
                    allCookies!![storedUri]?.let { targetCookies.addAll(it) }
                }
            }
        }
        // Check it there are expired cookies and remove them
        if (targetCookies.isNotEmpty()) {
            val cookiesToRemoveFromPersistence: MutableList<HttpCookie> =
                ArrayList()
            val it =
                targetCookies.iterator()
            while (it.hasNext()) {
                val currentCookie = it.next()
                if (currentCookie.hasExpired()) {
                    cookiesToRemoveFromPersistence.add(currentCookie)
                    it.remove()
                }
            }
            if (cookiesToRemoveFromPersistence.isNotEmpty()) {
                removeFromPersistence(uri, cookiesToRemoveFromPersistence)
            }
        }
        return targetCookies
    }

    /* http://tools.ietf.org/html/rfc6265#section-5.1.3
    A string domain-matches a given domain string if at least one of the
    following conditions hold:
    o  The domain string and the string are identical.  (Note that both
    the domain string and the string will have been canonicalized to
    lower case at this point.)
    o  All of the following conditions hold:
        *  The domain string is a suffix of the string.
        *  The last character of the string that is not included in the
           domain string is a %x2E (".") character.
        *  The string is a host name (i.e., not an IP address). */
    private fun checkDomainsMatch(
        cookieHost: String,
        requestHost: String
    ): Boolean {
        return requestHost == cookieHost || requestHost.endsWith(".$cookieHost")
    }

    /*  http://tools.ietf.org/html/rfc6265#section-5.1.4
        A request-path path-matches a given cookie-path if at least one of
        the following conditions holds:
        o  The cookie-path and the request-path are identical.
        o  The cookie-path is a prefix of the request-path, and the last
        character of the cookie-path is %x2F ("/").
        o  The cookie-path is a prefix of the request-path, and the first
        character of the request-path that is not included in the cookie-
        path is a %x2F ("/") character. */
    private fun checkPathsMatch(
        cookiePath: String,
        requestPath: String
    ): Boolean {
        return requestPath == cookiePath ||
                requestPath.startsWith(cookiePath) && cookiePath[cookiePath.length - 1] == '/' ||
                requestPath.startsWith(cookiePath) && requestPath.substring(cookiePath.length)[0] == '/'
    }

    private fun removeFromPersistence(
        uri: URI,
        cookiesToRemove: List<HttpCookie>
    ) {
        val editor = sharedPreferences.edit()
        for (cookieToRemove in cookiesToRemove) {
            editor.remove(
                uri.toString() + SP_KEY_DELIMITER
                        + cookieToRemove.name
            )
        }
        editor.apply()
    }

    @Synchronized
    override fun getURIs(): List<URI> {
        return ArrayList(allCookies!!.keys)
    }

    @Synchronized
    override fun remove(uri: URI, cookie: HttpCookie): Boolean {
        val targetCookies: MutableSet<HttpCookie>? = allCookies!![uri] as MutableSet<HttpCookie>?
        val cookieRemoved = targetCookies != null && targetCookies.remove(cookie)
        if (cookieRemoved) {
            removeFromPersistence(uri, cookie)
        }
        return cookieRemoved
    }

    private fun removeFromPersistence(uri: URI, cookieToRemove: HttpCookie) {
        val editor = sharedPreferences.edit()
        editor.remove(uri.toString() + SP_KEY_DELIMITER + cookieToRemove.name)
        editor.apply()
    }

    @Synchronized
    override fun removeAll(): Boolean {
        allCookies!!.clear()
        removeAllFromPersistence()
        return true
    }

    private fun removeAllFromPersistence() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private val TAG = PersistentCookieStore::class.java.simpleName
        // Persistence
        private const val SP_COOKIE_STORE = "cookieStore"
        private const val SP_KEY_DELIMITER = "|" // Unusual char in URL
        private const val SP_KEY_DELIMITER_REGEX = ("\\" + SP_KEY_DELIMITER)

        /**
         * Get the real URI from the cookie "domain" and "path" attributes, if they
         * are not set then uses the URI provided (coming from the response)
         */
        private fun cookieUri(uri: URI, cookie: HttpCookie): URI {
            var cookieUri = uri
            if (cookie.domain != null) { // Remove the starting dot character of the domain, if exists (e.g: .domain.com -> domain.com)
                var domain = cookie.domain
                if (domain[0] == '.') {
                    domain = domain.substring(1)
                }
                try {
                    cookieUri = URI(
                        if (uri.scheme == null) "http" else uri.scheme, domain,
                        if (cookie.path == null) "/" else cookie.path, null)
                } catch (e: URISyntaxException) {
                    Log.w(TAG, e)
                }
            }
            return cookieUri
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences(SP_COOKIE_STORE, Context.MODE_PRIVATE)
        loadAllFromPersistence()
    }
}