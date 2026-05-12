package utils.crypto.ecdh

import botsetting.BotECDH
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.spec.ECPublicKeySpec
import org.bouncycastle.util.encoders.Hex
import java.security.*
import javax.crypto.KeyAgreement

fun generateEcdhV2(): BotECDH {
    if (Security.getProvider("BC") == null) {
        Security.addProvider(BouncyCastleProvider())
    }
    val curveName = "secp256r1"
    val svrPubKeyHex = "0440eaf325b9c66225143aa7f3961c953c3d5a8048c2b73293cdc7dcbab7f35c4c66aa8917a8fd511f9d969d02c8501bcaa3e3b11746f00567e3aea303ac5f2d25"
    val ecSpec = ECNamedCurveTable.getParameterSpec(curveName)
    val g = KeyPairGenerator.getInstance("EC", "BC")
    g.initialize(ecSpec)
    val pair = g.generateKeyPair()
    val localPrivKey = pair.private
    val localPubKey = pair.public
    val kf = KeyFactory.getInstance("EC", "BC")
    val pubKeySpec = ECPublicKeySpec(ecSpec.curve.decodePoint(Hex.decode(svrPubKeyHex)), ecSpec)
    val serverPubKey = kf.generatePublic(pubKeySpec)
    val agreement = KeyAgreement.getInstance("ECDH", "BC")
    agreement.init(localPrivKey)
    agreement.doPhase(serverPubKey, true)
    var rawSharedSecret = agreement.generateSecret()
    if (rawSharedSecret.size != 32) {
        val temp = ByteArray(32)
        if (rawSharedSecret.size < 32) {
            System.arraycopy(rawSharedSecret, 0, temp, 32 - rawSharedSecret.size, rawSharedSecret.size)
        } else {
            System.arraycopy(rawSharedSecret, rawSharedSecret.size - 32, temp, 0, 32)
        }
        rawSharedSecret = temp
    }
    val md5 = MessageDigest.getInstance("MD5")
    val finalShareKey = md5.digest(rawSharedSecret.copyOfRange(0, 16))
    val pubKeyBytes = (localPubKey as ECPublicKey).q.getEncoded(false)
    return BotECDH(
        publicKey = pubKeyBytes,
        shareKey = finalShareKey
    )
}