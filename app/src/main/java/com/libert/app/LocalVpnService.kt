package com.libert.app

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class LocalVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    // Whitelist: Aplicativos bancários ignoram a VPN completamente (Segurança Máxima)
    private val bankPackages = listOf(
        "br.com.bb.android",
        "com.itau",
        "com.bradesco",
        "com.nu.production",
        "com.santander.app",
        "br.com.gft.cesta",
        "com.intermedium",
        "com.c6bank.app",
        "com.neondistribuidora",
        "com.picpay",
        "br.com.bradesco.next"
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = Builder()

        // Configuração de IP local fictício (processamento interno)
        builder.addAddress("10.1.1.1", 32)
        builder.addDnsServer("1.1.1.1")
        builder.setSession("LibertVpnFilter")

        // Exclui os apps de bancos do túnel VPN
        for (pkg in bankPackages) {
            try {
                builder.addDisallowedApplication(pkg)
            } catch (_: Exception) {}
        }

        try {
            vpnInterface = builder.establish()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}
