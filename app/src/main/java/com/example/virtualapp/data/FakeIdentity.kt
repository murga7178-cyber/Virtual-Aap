package com.example.virtualapp.data

import android.content.Context

data class FakeIdentity(
    val packageName: String,
    val androidId: String,
    val imei: String,
    val macAddress: String,
    val serial: String,
    val model: String
)

object IdentityStore {

    private const val PREF = "fake_identity_store"

    fun getOrCreate(context: Context, packageName: String): FakeIdentity {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val existingId = prefs.getString("${packageName}_androidId", null)

        if (existingId != null) {
            return FakeIdentity(
                packageName = packageName,
                androidId = existingId,
                imei = prefs.getString("${packageName}_imei", "")!!,
                macAddress = prefs.getString("${packageName}_mac", "")!!,
                serial = prefs.getString("${packageName}_serial", "")!!,
                model = prefs.getString("${packageName}_model", "")!!
            )
        }

        val newIdentity = FakeIdentity(
            packageName = packageName,
            androidId = randomHex(16),
            imei = randomDigits(15),
            macAddress = randomMac(),
            serial = randomHex(12).uppercase(),
            model = "Virtual-${randomHex(4).uppercase()}"
        )

        prefs.edit()
            .putString("${packageName}_androidId", newIdentity.androidId)
            .putString("${packageName}_imei", newIdentity.imei)
            .putString("${packageName}_mac", newIdentity.macAddress)
            .putString("${packageName}_serial", newIdentity.serial)
            .putString("${packageName}_model", newIdentity.model)
            .apply()

        return newIdentity
    }

    fun getAll(context: Context): List<FakeIdentity> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val result = mutableListOf<FakeIdentity>()
        for (key in prefs.all.keys) {
            if (key.endsWith("_androidId")) {
                val pkg = key.removeSuffix("_androidId")
                result.add(getOrCreate(context, pkg))
            }
        }
        return result
    }

    private fun randomHex(len: Int): String {
        val chars = "0123456789abcdef"
        return (1..len).map { chars.random() }.joinToString("")
    }

    private fun randomDigits(len: Int): String {
        return (1..len).map { (0..9).random().toString() }.joinToString("")
    }

    private fun randomMac(): String {
        val chars = "0123456789ABCDEF"
        return (1..6).joinToString(":") {
            "${chars.random()}${chars.random()}"
        }
    }
}
