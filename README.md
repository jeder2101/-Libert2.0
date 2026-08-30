# Libert 2.0 🛡️

O **Libert 2.0** é um aplicativo Android de código aberto projetado para neutralizar anúncios abusivos, pop-ups invasivos e propagandas de casas de apostas (*bets*). Ele opera por meio de uma arquitetura híbrida de **dupla camada**, combinando filtragem de pacotes de rede via VPN local com ações contextuais de acessibilidade em tempo real.

---

## 🏗️ Arquitetura do Sistema

O sistema opera com duas camadas complementares que garantem máxima eficiência e baixo impacto na bateria:

### 1. Camada 1: Filtro de Rede (`LocalVpnService`)
* **Processamento Interno:** Cria uma interface VPN local no dispositivo para interceptar tráfego na porta 53 (DNS).
* **Descarte de Pacotes (Null-Routing):** Intercepta e ignora requisições direcionadas a redes de anúncios e plataformas de apostas antes que os dados sejam baixados.
* **Whitelist Nativa de Bancos:** Utiliza a API nativa do Android (`addDisallowedApplication`) para excluir completamente os aplicativos bancários e governamentais do túnel VPN, garantindo 100% de segurança e conformidade com checagens de integridade.

### 2. Camada 2: Automação Visual (`ShieldService`)
* **Monitoramento da Interface:** Atua como tolerância zero via `AccessibilityService` para capturar anúncios que passem pela rede (ex: vídeos pré-carregados em cache local).
* **Ações Automáticas:** Executa toques de fechamento em elementos de anúncios, cliques no canto superior direito (`performTopRightClick`) e deslizamentos inteligentes (`performSmartSwipe`).
* **Proteção de Privacidade:** O serviço é interrompido imediatamente ao detectar aplicativos bancários, carteiras digitais ou telas do sistema operacional.

---

## 📁 Estrutura do Repositório

```text
Libert2.0/
├── README.md
└── app/
    ├── build.gradle.kts
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/libert/app/
            │   ├── AdClassifier.kt       # Dicionário e motor de classificação
            │   ├── LocalVpnService.kt    # Camada de Rede (Filtro DNS)
            │   ├── ShieldService.kt      # Camada Visual (Acessibilidade)
            │   └── MainActivity.kt       # Interface de controle do usuário
            └── res/
                ├── layout/activity_main.xml
                └── xml/accessibility_service_config.xml
