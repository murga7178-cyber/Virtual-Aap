package com.example.virtualapp

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualapp.data.FakeIdentity
import com.example.virtualapp.data.IdentityStore
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val apps = mutableListOf<AppInfo>()
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.appsRecyclerView)
        adapter = AppListAdapter(
            apps,
            onClick = { app -> onAppClicked(app) },
            onLongClick = { app -> onAppLongClicked(app) }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            loadApps()
            Toast.makeText(this, "लोड हो रहा है... कुल: ${apps.size}", Toast.LENGTH_SHORT).show()
        }

        loadApps()
    }

    private fun loadApps() {
        apps.clear()
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val list = pm.queryIntentActivities(intent, 0)

        for (ri in list) {
            val ai = ri.activityInfo.applicationInfo
            if (ai.packageName != packageName) {
                addApp(pm, ai)
            }
        }

        if (apps.isEmpty()) {
            val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (ai in allApps) {
                if (ai.packageName != packageName &&
                    pm.getLaunchIntentForPackage(ai.packageName) != null) {
                    addApp(pm, ai)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun addApp(pm: PackageManager, ai: ApplicationInfo) {
        val existing = IdentityStore.getAll(this)
            .find { it.packageName == ai.packageName }

        apps.add(
            AppInfo(
                name = ai.loadLabel(pm).toString(),
                packageName = ai.packageName,
                icon = ai.loadIcon(pm),
                isAdded = existing != null,
                fakeAndroidId = existing?.androidId
            )
        )
    }

    // Tap → पूरी identity दिखाओ
    private fun onAppClicked(app: AppInfo) {
        val identity = IdentityStore.getOrCreate(this, app.packageName)

        app.isAdded = true
        app.fakeAndroidId = identity.androidId
        adapter.notifyDataSetChanged()

        MyApplication.copyApkToVirtual(this, app.packageName)

        showIdentityDialog(app, identity)
    }

    // Tap dialog: सारी fake info दिखाओ + Copy बटन
    private fun showIdentityDialog(app: AppInfo, id: FakeIdentity) {
        val message = """
            📱 ${app.name}

            🆔 Android ID: ${id.androidId}

            📞 IMEI: ${id.imei}

            📶 WiFi MAC: ${id.wifiMac}

            🔵 Bluetooth MAC: ${id.bluetoothMac}

            🌐 WiFi IP: ${id.wifiIp}

            📡 Mobile IP: ${id.mobileIp}

            🔢 Serial: ${id.serial}

            📦 Model: ${id.model}

            🏢 Brand: ${id.brand}

            🤖 Android Version: ${id.androidVersion}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Fake Identity")
            .setMessage(message)
            .setPositiveButton("Copy") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("FakeIdentity", message))
                Toast.makeText(this, "Copy हो गया", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("बंद करें", null)
            .show()
    }

    // Long-press → delete
    private fun onAppLongClicked(app: AppInfo) {
        if (!app.isAdded) {
            Toast.makeText(this, "इस ऐप की ID अभी बनी नहीं है", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("ID हटाएँ?")
            .setMessage("${app.name} की fake ID हटा दी जाएगी।\nअगली बार नई ID बनेगी।")
            .setPositiveButton("हटाएँ") { _, _ ->
                IdentityStore.delete(this, app.packageName)
                app.isAdded = false
                app.fakeAndroidId = null
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "ID हटा दी गई", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("रहने दें", null)
            .show()
    }
}
