package com.example.virtualapp.data

import android.content.Context

data class FakeIdentity(
    val packageName: String,
    val androidId: String,
    val imei: String,
    val wifiMac: String,
    val bluetoothMac: String,
    val wifiIp: String,
    val mobileIp: String,
    val serial: String,
    val model: String,
    val brand: String,
    val androidVersion: String,
    val fingerprint: String
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
                wifiMac = prefs.getString("${packageName}_wifiMac", "")!!,
                bluetoothMac = prefs.getString("${packageName}_btMac", "")!!,
                wifiIp = prefs.getString("${packageName}_wifiIp", "")!!,
                mobileIp = prefs.getString("${packageName}_mobileIp", "")!!,
                serial = prefs.getString("${packageName}_serial", "")!!,
                model = prefs.getString("${packageName}_model", "")!!,
                brand = prefs.getString("${packageName}_brand", "")!!,
                androidVersion = prefs.getString("${packageName}_androidVer", "")!!,
                fingerprint = prefs.getString("${packageName}_fingerprint", "")!!
            )
        }

        val newIdentity = FakeIdentity(
            packageName = packageName,
            androidId = randomHex(16),
            imei = randomDigits(15),
            wifiMac = randomMac(),
            bluetoothMac = randomMac(),
            wifiIp = randomIp(),
            mobileIp = randomIp(),
            serial = randomHex(12).uppercase(),
            model = "SM-${randomHex(4).uppercase()}",
            brand = "Samsung",
            androidVersion = "13",
            fingerprint = "samsung/${randomHex(6)}/${randomHex(6)}:13/TP1A.220624.014/${randomHex(8)}:user/release-keys"
        )

        prefs.edit()
            .putString("${packageName}_androidId", newIdentity.androidId)
            .putString("${packageName}_imei", newIdentity.imei)
            .putString("${packageName}_wifiMac", newIdentity.wifiMac)
            .putString("${packageName}_btMac", newIdentity.bluetoothMac)
            .putString("${packageName}_wifiIp", newIdentity.wifiIp)
            .putString("${packageName}_mobileIp", newIdentity.mobileIp)
            .putString("${packageName}_serial", newIdentity.serial)
            .putString("${packageName}_model", newIdentity.model)
            .putString("${packageName}_brand", newIdentity.brand)
            .putString("${packageName}_androidVer", newIdentity.androidVersion)
            .putString("${packageName}_fingerprint", newIdentity.fingerprint)
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

    fun delete(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("${packageName}_androidId")
            .remove("${packageName}_imei")
            .remove("${packageName}_wifiMac")
            .remove("${packageName}_btMac")
            .remove("${packageName}_wifiIp")
            .remove("${packageName}_mobileIp")
            .remove("${packageName}_serial")
            .remove("${packageName}_model")
            .remove("${packageName}_brand")
            .remove("${packageName}_androidVer")
            .remove("${packageName}_fingerprint")
            .apply()
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

    private fun randomIp(): String {
        return "${(1..223).random()}.${(0..255).random()}.${(0..255).random()}.${(1..254).random()}"
    }
}
