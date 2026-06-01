package org.example.project

import java.net.URLEncoder

class ScoreBuilder(
    private val baseUrl: String
) {

    fun build(data: Map<String, String>): String {

        val referrer = data["referrer"].orEmpty()
        val gadid = data["gadid"].orEmpty()
        val device = data["device"].orEmpty()
        val probe = data["probe"].orEmpty()
        val firstInst = data["package"].orEmpty()
        val firebaseId = data["firebase_id"].orEmpty()

        val url = buildString {
            append(baseUrl)
            append("fgz5eokkl")
            append("?tnpj80m=").append(referrer.encode())
            append("&ej4jrukoze=").append(gadid.encode())
            append("&u7x0xxzp0=").append(device.encode())
            append("&i2cjy=").append(probe.encode())
            append("&enmcbw=").append(firstInst.encode())
            append("&pn9lpv=").append(firebaseId.encode())
        }

//        log("LinkBuilder: final url = $url")

        return url
    }

    private fun String.encode(): String {
        return URLEncoder.encode(this, "UTF-8")
    }
}