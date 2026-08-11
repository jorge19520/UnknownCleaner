package com.example.unknowncleaner

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val callPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )

    private val smsPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.WRITE_SMS
    )

    private val callPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms.all { it.value }) {
            cleanCalls()
        } else {
            Toast.makeText(this, "Permisos denegados para llamadas", Toast.LENGTH_SHORT).show()
        }
    }

    private val smsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms.all { it.value }) {
            checkDefaultSmsAppAndClean()
        } else {
            Toast.makeText(this, "Permisos denegados para SMS", Toast.LENGTH_SHORT).show()
        }
    }

    private val defaultSmsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Telephony.Sms.getDefaultSmsPackage(this) == packageName) {
            cleanSms()
        } else {
            Toast.makeText(this, "Debes ser la app de SMS por defecto para borrar", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { smsPermissionLauncher.launch(smsPermissions) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier.padding(16.dp).fillMaxWidth(0.8f).height(60.dp)
                ) {
                    Text("SMS", color = Color.White)
                }

                Button(
                    onClick = { callPermissionLauncher.launch(callPermissions) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier.padding(16.dp).fillMaxWidth(0.8f).height(60.dp)
                ) {
                    Text("CALL", color = Color.White)
                }
            }
        }
    }

    private fun cleanCalls() {
        Thread {
            val deleted = CleanerUtils.cleanUnknownCalls(this)
            runOnUiThread {
                Toast.makeText(this, "Llamadas borradas: $deleted", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun checkDefaultSmsAppAndClean() {
        if (Telephony.Sms.getDefaultSmsPackage(this) == packageName) {
            cleanSms()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                        cleanSms()
                    } else {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                        defaultSmsLauncher.launch(intent)
                    }
                } else {
                    requestDefaultSmsOld()
                }
            } else {
                requestDefaultSmsOld()
            }
        }
    }

    private fun requestDefaultSmsOld() {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
        defaultSmsLauncher.launch(intent)
    }

    private fun cleanSms() {
        Thread {
            val deleted = CleanerUtils.cleanUnknownSms(this)
            runOnUiThread {
                Toast.makeText(this, "SMS borrados: $deleted\nNo olvides restaurar tu app de SMS original.", Toast.LENGTH_LONG).show()
            }
        }.start()
    }
}
