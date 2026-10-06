package com.example.virtualapp

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualapp.data.IdentityStore
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val apps = mutableListOf<AppInfo>()
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.appsRecyclerView)
        adapter = AppListAdapter(apps) { app -> onAppClicked(app) }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            loadApps()
            Toast.makeText(this, "ऐप लिस्ट लोड हो गई", Toast.LENGTH_SHORT).show()
        }

        loadApps()
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
        val list = pm.queryIntentActivities(intent, 0)
        val savedIdentities = IdentityStore.getAll(this)

        apps.clear()
        for (ri in list) {
            val ai = ri.activityInfo.applicationInfo
            if ((ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                ai.packageName != packageName) {

                val existing = savedIdentities.find { it.packageName == ai.packageName }

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
        }
        adapter.notifyDataSetChanged()
    }

    private fun onAppClicked(app: AppInfo) {
        val identity = IdentityStore.getOrCreate(this, app.packageName)

        app.isAdded = true
        app.fakeAndroidId = identity.androidId
        adapter.notifyDataSetChanged()

        MyApplication.copyApkToVirtual(this, app.packageName)

        Toast.makeText(
            this,
            "✅ ${app.name}\nAndroid ID: ${identity.androidId}\nIMEI: ${identity.imei}",
            Toast.LENGTH_LONG
        ).show()
    }
}
