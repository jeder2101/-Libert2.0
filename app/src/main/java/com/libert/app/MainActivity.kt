package com.libert.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val VPN_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartVpn = findViewById<Button>(R.id.btnStartVpn)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)

        btnStartVpn.setOnClickListener {
            startVpnProtection()
        }

        btnAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Ative o serviço Libert 2.0 na lista", Toast.LENGTH_LONG).show()
        }
    }

    private fun startVpnProtection() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            val startVpnIntent = Intent(this, LocalVpnService::class.java)
            startService(startVpnIntent)
            Toast.makeText(this, "Filtro de Rede (VPN) Ativado!", Toast.LENGTH_SHORT).show()
        }
    }
}
