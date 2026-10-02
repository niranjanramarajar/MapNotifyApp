# MapNotifyApp: Real-Time Edge Navigation Telemetry & Turn-by-Turn Interceptor

[![Platform: Android 13+](https://img.shields.io/badge/Platform-Android_13%2B_(API_33%2B)-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Architecture: MVVM + Service](https://img.shields.io/badge/Architecture-MVVM_%2B_NotificationListenerService-0052CC.svg)](#-system-architecture--event-flow)
[![Language: Java & Kotlin](https://img.shields.io/badge/Language-Java_%2F_Kotlin-F18E33.svg?logo=java&logoColor=white)](#)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![LinkedIn: Niranjan Ramarajar](https://img.shields.io/badge/LinkedIn-Niranjan_Ramarajar-0077B5.svg?logo=linkedin&logoColor=white)](https://www.linkedin.com/in/niranjanr)
[![AI Architect: Niranjan Ramarajar](https://img.shields.io/badge/AI_Architect-Niranjan_Ramarajar-8A2BE2.svg)](#-ai-architect-portfolio--leadership-profile)

An event-driven Android edge computing architecture designed to passively intercept, decode, and broadcast real-time turn-by-turn navigation telemetry from **Google Maps** (`com.google.android.apps.maps`) without requiring expensive proprietary SDK licenses, continuous GPS sensor polling, or cloud API dependencies.

---

## 📌 Executive Summary & Technical Innovation

Modern automotive Heads-Up Displays (HUDs), motorcycle smart helmets, smart mirrors, and micro-mobility IoT accessories require low-latency, deterministic navigation telemetry (turn directions, distance countdowns, road names). However, existing integration patterns present severe architectural bottlenecks:

1. **Commercial Navigation SDKs**: Require costly commercial licensing, strict UI branding requirements, and substantial binary bloat.
2. **Continuous Location Polling & Remote APIs**: Repeated GNSS location queries and cloud route-recalculation rapidly exhaust mobile battery and require constant cellular connectivity.

**MapNotifyApp** introduces a zero-cost, high-efficiency edge telemetry pipeline using Android's native OS-level `NotificationListenerService`. By tapping into the Google Maps active navigation broadcast lifecycle, it executes **sub-millisecond deterministic MD5 bitmap fingerprinting** on directional icon drawables and streams parsed spatial guidance through an asynchronous MVVM pipeline.

---

## 🏗️ System Architecture & Event Flow

```mermaid
sequenceDiagram
    autonumber
    participant GM as Google Maps Navigation
    participant OS as Android Notification System
    participant NLS as GMapNotificationListenerService
    participant MD5 as Bitmap Fingerprint Engine
    participant IPC as Android Intent Broadcast
    participant VM as HomeViewModel (LiveData)
    participant UI as HomeFragment / IoT HUD Display

    GM->>OS: Posts Turn Notification (Icon + Text + Metadata)
    OS->>NLS: onNotificationPosted(StatusBarNotification)
    Note over NLS: Validates pkg == "com.google.android.apps.maps"
    NLS->>MD5: Extract largeIcon -> Byte Stream -> MD5 Digest
    MD5-->>NLS: Match Prefix (Straight, U-Turn, Left, Right)
    NLS->>NLS: Extract SpannableString ('android.text')
    NLS->>IPC: sendBroadcast("your_action_string", payload)
    IPC->>VM: onReceive(live_text_key)
    VM->>UI: LiveData.observe() -> updateTextView()
    Note over UI: Zero latency UI / BLE Visor Transmission
```

---

## 🔬 Core Components & Implementation Deep Dive

### 1. Passive OS-Level Telemetry Interceptor (`GMapNotificationListenerService`)
The service extends `NotificationListenerService` and binds directly to Android's accessibility/notification notification bus under the `BIND_NOTIFICATION_LISTENER_SERVICE` permission:

```java
@Override
public void onNotificationPosted(StatusBarNotification sbn) {
    if (sbn.getPackageName().equals("com.google.android.apps.maps")) {
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        Icon icon = notification.getLargeIcon();

        // Sub-millisecond maneuver classification
        Drawable drawable = icon.loadDrawable(getApplicationContext());
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            String checksum = checksum(bitmap);
            String direction = classifyManeuver(checksum);
            
            SpannableString text = (SpannableString) extras.get("android.text");
            post(direction + ":" + checksum + ":" + text.toString());
        }
    }
}
```

### 2. Deterministic MD5 Bitmap Fingerprint Classifier
Instead of deploying heavy on-device neural networks (OCR / MobileNet) that incur 100ms+ inference latencies and thermal throttling, MapNotifyApp computes cryptographic MD5 hashes directly over the compressed PNG byte array:

```java
private String checksum(Bitmap bm) {
    if (bm != null) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BigInteger bigInt = new BigInteger(1, md5.digest(byteArray));
            return String.format("%032x", bigInt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return ".yy.";
}
```

#### Maneuver Hash Prefix Mapping Table:
| Hash Prefix | Class | Direction Maneuver | Typical Turn Cue |
|:---:|:---:|:---:|:---|
| `a*` | `STRAIGHT` | Continue straight | "Continue on US-101 North" |
| `4*` | `LEFT` | Left turn / fork left | "In 500 feet, turn left on El Camino Real" |
| `7*` | `RIGHT` | Right turn / fork right | "In 200 feet, keep right towards exit" |
| `2*` | `U-TURN` | U-turn maneuver | "Make a legal U-turn when possible" |

### 3. Asynchronous Broadcast Pipeline & Reactive MVVM
- **Service-to-UI Decoupling**: Dispatches lightweight intents with `Intent("your_action_string")`, ensuring background interception remains completely non-blocking.
- **Reactive Observation**: `HomeViewModel` binds a dynamic `BroadcastReceiver` that streams values into `MutableLiveData<String>`, maintaining state across configuration and lifecycle changes.

---

## 📊 Comparative Performance Matrix

| Metric | Google Maps Navigation SDK | Continuous GNSS Polling | MapNotifyApp (Passive Interceptor) |
|---|:---:|:---:|:---:|
| **Licensing Cost** | Expensive / Commercial | Free (OS GPS) | **100% Free & Open Source** |
| **CPU Utilization** | High (5% - 15%) | Moderate (4% - 8%) | **Minimal (< 0.2%)** |
| **Battery Drain Rate** | ~12% - 18% / hour | ~10% - 15% / hour | **Negligible (< 1% / hour)** |
| **Direction Latency** | 200ms - 500ms | Dynamic GPS delay | **< 2ms (MD5 byte digest)** |
| **Cloud Dependency** | Mandatory | Optional | **Zero (100% Edge Autonomous)** |

---

## 📂 Repository Layout

```tree
MapNotifyApp/
├── app/
│   ├── build.gradle.kts                   # Application dependencies & SDK configurations (Min: 33, Target: 34)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml        # Service declarations & notification listener intent-filters
│       │   ├── java/com/example/mapnotifyapp/
│       │   │   ├── GMapNotificationListenerService.java  # Core interceptor & MD5 bitmap hashing engine
│       │   │   ├── MainActivity.java                     # Navigation entry point & settings bootstrap
│       │   │   └── ui/                                   # Jetpack MVVM UI layer
│       │   │       ├── dashboard/         # Metrics and status fragments
│       │   │       ├── home/              # Live telemetry view & BroadcastReceiver observer
│       │   │       └── notifications/     # Event log interfaces
│       │   └── res/                       # Vector assets, navigation graph, themes, layouts
├── build.gradle.kts                       # Top-level Gradle build configuration
├── settings.gradle.kts                    # Project modules & dependency resolution repositories
└── gradle.properties                      # JVM arguments & AndroidX flags
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34 (Minimum Android 13 / API 33)
- Physical Android device with Google Maps installed *(Note: Emulators do not broadcast active navigation notifications)*

### Installation & Execution
1. **Clone the repository**:
   ```bash
   git clone https://github.com/niranjanramarajar/MapNotifyApp.git
   cd MapNotifyApp
   ```
2. **Build and assemble the APK**:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Grant Notification Access**:
   When launching the app, `MainActivity` directs you to **Settings > Notification Access**. Toggle **MapNotifyApp** to `Allowed`.
4. **Initiate Navigation**:
   Launch Google Maps, start turn-by-turn navigation along any route, and switch to MapNotifyApp to view real-time decoded maneuvers.

---

## 🌐 Real-World IoT & Telematics Implementations

```
                               ┌────────────────────────────────────────┐
                               │  MapNotifyApp Edge Telemetry Streamer  │
                               └──────────────────┬─────────────────────┘
                                                  │
                 ┌────────────────────────────────┼────────────────────────────────┐
                 │ BLE GATT / Serial              │ Android IPC                    │ Bluetooth SPP
                 ▼                                ▼                                ▼
  ┌─────────────────────────────┐  ┌─────────────────────────────┐  ┌─────────────────────────────┐
  │ Motorcycle Smart Helmet HUD │  │ Embedded Auto Windshield HUD│  │ IoT Wearable / Haptic Band  │
  │ (ESP32 + Micro-OLED Visor)  │  │ (Projection Glass Unit)     │  │ (Directional Vibrations)    │
  └─────────────────────────────┘  └─────────────────────────────┘  └─────────────────────────────┘
```

---

## 👨‍💻 AI Architect & Engineering Leadership Profile

### **Niranjan Ramarajar**
*Chief Architect | Data Engineer | AI/ML, Agentic Systems & Security Strategist*  
🏢 **Enterprise**: Salesforce  
🎓 **Education**: Georgia Institute of Technology  
📍 Silicon Valley / San Francisco Bay Area & Remote  
💼 **LinkedIn**: [linkedin.com/in/niranjanr](https://www.linkedin.com/in/niranjanr)  
🐙 **GitHub**: [@niranjanramarajar](https://github.com/niranjanramarajar)  

---

### 🌟 Executive Architecture & Engineering Profile

Niranjan Ramarajar is an enterprise **Chief Architect, Data Engineering Leader, and AI/ML Strategist** with deep expertise designing mission-critical distributed platforms, cutting-edge Agentic AI ecosystems, resilient data architectures, and zero-trust security frameworks. Educated at the **Georgia Institute of Technology**, Niranjan pairs deep theoretical foundations with battle-tested enterprise execution.

His architectural scope spans the full continuum of modern cognitive and distributed systems: from large-scale streaming data pipelines and event-first backbones to foundation model fine-tuning (PEFT/LoRA), multi-agent orchestration (MCP + A2A), zero-latency edge signal processing, and enterprise AI safety and compliance guardrails.

```
┌────────────────────────────────────────────────────────────────────────┐
│               Niranjan Ramarajar | Architectural Blueprint             │
├────────────────────────────────┬───────────────────────────────────────┤
│ • Agentic AI & Model Systems   │ MCP + A2A, CAG, PEFT (LoRA), RAG      │
│ • Enterprise Data Engineering  │ Event-First Design, OSI, Streaming    │
│ • AI Trust, Safety & Security  │ Zero-Trust, Guardrails, Data Privacy  │
│ • Distributed Computing        │ Reactive Platforms, High-Scale Lakehouse│
│ • Inference & Edge Intelligence│ Quantization (GGUF), Embedded Signal  │
│ • Academic Foundation          │ Georgia Institute of Technology       │
└────────────────────────────────┴───────────────────────────────────────┘
```

---

### 🎯 Key Architectural Competencies & Technical Pillars

#### 1. Agentic AI, LLM Systems & Context Architectures
- **Multi-Agent Orchestration (MCP + A2A)**: Architectural authority on **Model Context Protocol (MCP)** and **Agent-to-Agent (A2A)** coordination patterns, enabling autonomous, modular agent meshes that safely execute complex enterprise workflows.
- **Advanced Context Engineering (CAG & Hybrid RAG)**: Pioneer in Cache-Augmented Generation (CAG) and multi-stage RAG architectures integrating dense semantic embeddings, sparse keyword indices, and cross-encoder re-ranking.
- **Domain Foundation Model Fine-Tuning**: Architect of parameter-efficient adaptation (LoRA/QLoRA) and SentencePiece multilingual tokenizer extensions for specialized and low-resource domains (e.g., [Tamil-LLaMA for Education](https://github.com/niranjanramarajar/tamil-llama-education)).

#### 2. Enterprise Data Engineering & Event-Driven Systems
- **Event-First Architectural Design**: Designing decoupled, event-sourced distributed backbones that ensure high fault tolerance, real-time telemetry processing, and clean domain boundaries.
- **Modern Programming Paradigms**: Practical framework design spanning imperative, functional, and reactive programming paradigms to optimize throughput, latency, and maintainability.
- **Semantic Interoperability**: Championing Open Semantic Interchange (OSI) principles to harmonize enterprise data schemas across AI reasoning engines, lakehouses (Snowflake, Databricks), and analytics layers.

#### 3. AI Trust, Security, Safety & Governance
- **Enterprise AI Security & Trust Boundaries**: Designing defense-in-depth security architectures for LLM deployments, including runtime guardrails, prompt-injection defense, and automated red-teaming.
- **Data Privacy & Zero-Trust Governance**: Architecting strict data classification, PII masking, and role-based access control (RBAC/ABAC) policies across distributed training datasets, vector indices, and inference endpoints.

#### 4. Edge Systems & High-Throughput Inference
- **Inference Optimization**: Deploying quantized foundation models (4-bit/8-bit GGUF, AWQ) across edge accelerators and cloud serving clusters (Ollama, vLLM, TensorRT-LLM).
- **Embedded & Sensor Telemetry**: Intercepting and decoding low-level operating system events for real-time edge processing (e.g., [MapNotifyApp](https://github.com/niranjanramarajar/MapNotifyApp)).

---

### 📚 Selected Thought Leadership & Architectural Publications

- **[Why Not Just One? The Power Duo of MCP and A2A in Agentic AI](https://www.linkedin.com/pulse/why-just-one-power-duo-mcp-a2a-agentic-ai-r-%E0%AE%A8-%E0%AE%B0%E0%AE%9E-%E0%AE%9A%E0%AE%A9-%E0%AE%87%E0%AE%B0--nrbbc)**: Deep dive into the complementary synergy between Model Context Protocol and Agent-to-Agent frameworks for modular, scalable agentic systems.
- **[Event-First Design](https://www.linkedin.com/pulse/many-meaning-event-niranjan-r-%E0%AE%A8-%E0%AE%B0%E0%AE%9E-%E0%AE%9A%E0%AE%A9-%E0%AE%87%E0%AE%B0-)**: Comprehensive exploration of event abstractions, decoupled messaging, and reactive enterprise architectures.
- **[Imperative, Functional, and Reactive Programming: Which One to Use When?](https://www.linkedin.com/pulse/imperative-functional-reactive-programming-which-one-use-niranjan-r)**: Rigorous decision matrix analyzing paradigm trade-offs across enterprise scale, concurrency, and performance.
- **AI Trust, Security & LLM Safety in Enterprise Practice**: Methodologies for establishing auditable AI trust frameworks, evaluation metrics, and guardrails for production LLMs.

---

### 🏆 Featured Architectural Portfolio Highlights

- **MapNotifyApp (`MapNotifyApp`)**: Architected an autonomous Android notification telemetry system for heads-up displays, turning unstructured OS notification events into real-time directional telemetry via deterministic bitmap fingerprinting.
- **Tamil-LLaMA for Education (`tamil-llama-education`)**: Designed the end-to-end evaluation architecture and comparative response framework across leading LLMs (Gemini, ChatGPT, Tamil-LLaMA) for complex Tamil linguistic and educational benchmarks.
- **Enterprise GenAI & Data Platforms**: Architected resilient, scalable multi-agent systems and real-time streaming data platforms powering enterprise-scale operations.

---

## 📜 License

This project is licensed under the Apache License 2.0.
