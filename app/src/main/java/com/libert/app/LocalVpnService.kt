package com.libert.app

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class LocalVpnService : VpnService(), Runnable {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null
    @Volatile private var isRunning = false

    // Whitelist de Apps Bancários (Excluídos da VPN via API Nativa do Android)
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

    // Lista de domínios conhecidos de anúncios, rastreadores e apostas para descarte imediato
    private val blockedDomains = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adnxs.com",
        "taboola.com",
        "outbrain.com",
        "applovin.com",
        "ironsrc.com",
        "vungle.com",
        "unity3d.com",
        "bet365.com",
        "betano.com",
        "blaze.com",
        "pixbet.com",
        "estrelabet.com",
        "kto.com",
        "1xbet.com",
        "777.com"
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_STICKY

        val builder = Builder()
        builder.addAddress("10.1.1.1", 32)
        builder.addRoute("0.0.0.0", 0)
        builder.addDnsServer("1.1.1.1") // Resolvedor DNS Cloudflare primário
        builder.setSession("LibertVpnFilter")

        // Exclui apps bancários do tráfego do túnel
        for (pkg in bankPackages) {
            try {
                builder.addDisallowedApplication(pkg)
            } catch (_: Exception) {}
        }

        try {
            vpnInterface = builder.establish()
            isRunning = true
            vpnThread = Thread(this, "LibertVpnThread").apply { start() }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun run() {
        val descriptor = vpnInterface?.fileDescriptor ?: return
        val inputStream = FileInputStream(descriptor)
        val outputStream = FileOutputStream(descriptor)
        val buffer = ByteBuffer.allocate(32767)

        while (isRunning) {
            try {
                val readBytes = inputStream.read(buffer.array())
                if (readBytes > 0) {
                    buffer.limit(readBytes)
                    
                    // Inspeciona se o pacote é uma consulta DNS na Porta 53
                    if (isDnsQuery(buffer)) {
                        val domain = extractDomainFromDns(buffer)
                        if (domain != null && isBlockedDomain(domain)) {
                            // Pacote de anúncio/bet detectado: descarte o pacote (não retransmite)
                            buffer.clear()
                            continue
                        }
                    }

                    // Se não for bloqueado, mantém o fluxo normal de rede
                    outputStream.write(buffer.array(), 0, readBytes)
                    buffer.clear()
                } else {
                    Thread.sleep(10)
                }
            } catch (e: Exception) {
                if (!isRunning) break
            }
        }
    }

    private fun isDnsQuery(buffer: ByteBuffer): Boolean {
        if (buffer.limit() < 28) return false
        val ipHeaderLength = (buffer.get(0).toInt() and 0x0F) * 4
        val protocol = buffer.get(9).toInt() and 0xFF
        
        // Protocolo UDP = 17
        if (protocol != 17) return false
        
        val destinationPort = ((buffer.get(ipHeaderLength + 2).toInt() and 0xFF) shl 8) or 
                              (buffer.get(ipHeaderLength + 3).toInt() and 0xFF)
                              
        return destinationPort == 53
    }

    private fun extractDomainFromDns(buffer: ByteBuffer): String? {
        try {
            val ipHeaderLength = (buffer.get(0).toInt() and 0x0F) * 4
            val dnsOffset = ipHeaderLength + 8 + 12 // IP + UDP Header + DNS Header
            
            if (dnsOffset >= buffer.limit()) return null

            val domainBuilder = StringBuilder()
            var position = dnsOffset

            while (position < buffer.limit()) {
                val length = buffer.get(position).toInt() and 0xFF
                if (length == 0) break
                
                if (domainBuilder.isNotEmpty()) domainBuilder.append(".")
                
                for (i in 0 until length) {
                    position++
                    if (position >= buffer.limit()) return null
                    domainBuilder.append(buffer.get(position).toInt().toChar())
                }
                position++
            }

            return domainBuilder.toString().lowercase()
        } catch (e: Exception) {
            return null
        }
    }

    private fun isBlockedDomain(domain: String): Boolean {
        return blockedDomains.any { domain.endsWith(it) || domain == it }
    }

    override fun onDestroy() {
        isRunning = false
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}
