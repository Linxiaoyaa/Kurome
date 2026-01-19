
<div align="center">

<img src="https://socialify.git.ci/Linxiaoyaa/Kurome/image?description=1&descriptionEditable=An%20Implementation%20of%20QQ%20Protocol%20in%20Pure%20Kotlin%2C%20Focusing%20on%20ECDH%20%26%20Wtlogin&font=Jost&forks=1&issues=1&language=1&name=1&owner=1&pattern=Diagonal%20Stripes&theme=Auto" alt="Kurome Banner" width="100%"/>

<p>
  <a href="https://github.com/Linxiaoyaa/Kurome/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/Linxiaoyaa/Kurome?style=flat-square" alt="License">
  </a>
  <a href="https://jitpack.io/#Linxiaoyaa/Kurome">
    <img src="https://img.shields.io/jitpack/v/github/Linxiaoyaa/Kurome?style=flat-square" alt="JitPack">
  </a>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin" alt="Language">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20JVM-green?style=flat-square" alt="Platform">
</p>

</div>

## Usage 

### Integrating into Gradle projects (Kotlin/Java)

Add the **JitPack** repository and the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Linxiaoyaa:Kurome:master-SNAPSHOT") // Or specific tag
}
```

For more library usage, please refer to the unit tests or source code in `utils.crypto`.

### Protocol Implementation Details

**Kurome** provides a low-level implementation of the core crypto and login protocols:

*   **ECDH V2**: Full implementation of NIST P-256 (secp256r1) key exchange with proper padding handling.
*   **Wtlogin**: Custom Base64 transcoding and ticket decoding logic compatible with Android clients.

### Providing service to bot applications

This project is designed as a core library. You can use it to build:

*   **Android QQ Clients**: Simulating mobile login flows.
*   **Chat Bots**: Handling the handshake and cryptographic challenges.

## Appendix

### Disclaimer

The **Kurome** project, including its developers, contributors, and affiliated individuals or entities, hereby explicitly disclaim any association with, support for, or endorsement of any form of illegal behavior. This disclaimer extends to any use or application of the Kurome project that may be contrary to local, national, or international laws, regulations, or ethical guidelines.

Kurome is an open-source software project designed to facilitate lawful and ethical applications in its intended use cases (Protocol Research & Education). It is the responsibility of each user to ensure that their usage of Kurome complies with all applicable laws and regulations in their jurisdiction.

The developers and contributors of Kurome assume no liability whatsoever for any actions taken by users that violate the law or engage in any form of illicit activity. Users are solely responsible for their own actions and any consequences that may arise from the use of Kurome.

By using or accessing Kurome, the user acknowledges and agrees to release the developers, contributors, and affiliated individuals or entities from any and all liability arising from the use or misuse of the project.

**Please use Kurome responsibly and in accordance with the law.**

### Feedback

*   [Submit an Issue](https://github.com/Linxiaoyaa/Kurome/issues)

### Related Projects

<table>
  <tbody>
    <tr>
      <td><a href="https://github.com/Linxiaoyaa/Kurome">Kurome</a></td>
      <td>Android QQ Protocol Implementation (Kotlin)（👈Here）</td>
    </tr>
    <tr>
      <td><a href="https://github.com/LagrangeDev/Lagrange.Core">Lagrange.Core</a></td>
      <td>NTQQ Protocol Implementation (C#)</td>
    </tr>
    <tr>
      <td><a href="https://github.com/mamoe/mirai">Mirai</a></td>
      <td>High-performance QQ Robot Framework (Kotlin)</td>
    </tr>
  </tbody>
</table>
