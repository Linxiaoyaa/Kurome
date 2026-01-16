package com.kurome.app.utils.crypto.ecdh

import botsetting.BotECDH
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.util.encoders.Hex
import java.security.*
import javax.crypto.KeyAgreement

fun generateEcdhV2(): BotECDH {
    // 1. 注册 BouncyCastle 提供者
    if (Security.getProvider("BC") == null) {
        Security.addProvider(BouncyCastleProvider())
    }

    val curveName = "secp256r1" // 即 415 曲线
    val svrPubKeyHex = "0440eaf325b9c66225143aa7f3961c953c3d5a8048c2b73293cdc7dcbab7f35c4c66aa8917a8fd511f9d969d02c8501bcaa3e3b11746f00567e3aea303ac5f2d25"

    // 2. 生成本地密钥对
    val ecSpec = ECNamedCurveTable.getParameterSpec(curveName)
    val g = KeyPairGenerator.getInstance("EC", "BC")
    g.initialize(ecSpec)
    val pair = g.generateKeyPair()
    val localPrivKey = pair.private
    val localPubKey = pair.public

    // 3. 解析服务端公钥
    val kf = KeyFactory.getInstance("EC", "BC")
    val pubKeySpec = org.bouncycastle.jce.spec.ECPublicKeySpec(
        ecSpec.curve.decodePoint(Hex.decode(svrPubKeyHex)),
        ecSpec
    )
    val serverPubKey = kf.generatePublic(pubKeySpec)

    // 4. 计算共享密钥 (Shared Secret)
    val agreement = KeyAgreement.getInstance("ECDH", "BC")
    agreement.init(localPrivKey)
    agreement.doPhase(serverPubKey, true)
    val rawSharedSecret = agreement.generateSecret()

    // 5. 计算最终 ShareKey (取原始共享密钥前 16 字节的 MD5)
    val md5 = MessageDigest.getInstance("MD5")
    val finalShareKey = md5.digest(rawSharedSecret.copyOfRange(0, 16))

    // 6. 获取本地公钥的原始字节 (65字节)
    val pubKeyBytes = (localPubKey as org.bouncycastle.jce.interfaces.ECPublicKey).q.getEncoded(false)


    return BotECDH(
        publicKey = pubKeyBytes,
        shareKey = finalShareKey
    )
}