package com.libert.app

import android.view.accessibility.AccessibilityNodeInfo

object AdClassifier {

    private val adKeywords = listOf(
        "patrocinado", "sponsored", "anúncio", "publicidade", "promovido", "promoted",
        "ad ", "ads ", "saiba mais", "instalar agora", "comprar agora", "shop now",
        "install now", "baixar agora", "download now", "publicidad"
    )

    private val betKeywords = listOf(
        "bet", "bets", "aposte", "apostas", "aposta", "cassino", "casino",
        "tiger", "tigrinho", "aviator", "mines", "fortune", "roleta", "spin",
        "bônus de depósito", "ganhe até", "rodadas grátis", "deposite", "jackpot",
        "bet365", "betano", "blaze", "vai de bet", "estrelabet", "kto", "pixbet",
        "parimatch", "1xbet", "novibet", "superbet", "777", "slot", "slots"
    )

    private val actionKeywords = listOf(
        "pular", "skip", "fechar", "close", "dismiss", "x", "cancelar", "cancel"
    )

    private val adViewIds = listOf(
        "ad_view", "banner_ad", "native_ad", "sponsor", "ads_container",
        "tt_ad", "anythink", "applovin", "mbridge", "close_btn", "btn_close",
        "closebutton", "dismiss_button", "ksad_kwai", "ironsource", "vungle",
        "ad_container", "ad_header", "ad_frame"
    )

    fun isAdElement(node: AccessibilityNodeInfo): Boolean {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""

        for (id in adViewIds) {
            if (viewId.contains(id)) return true
        }

        if (text.isNotBlank()) {
            for (keyword in adKeywords) {
                if (text == keyword || text.contains(keyword)) return true
            }
            for (bet in betKeywords) {
                if (text.contains(bet)) return true
            }
        }
        return false
    }

    fun isCloseOrSkipButton(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (!node.isClickable) return false

        for (id in adViewIds) {
            if (viewId.contains("close") || viewId.contains("skip") || viewId.contains("dismiss")) {
                return true
            }
        }

        for (keyword in actionKeywords) {
            if (text == keyword || text.contains(keyword) || viewId.contains(keyword)) {
                return true
            }
        }
        return false
    }
}
