package au.com.respite.api.security

/**
 * Created by mfellows on 8/05/2014.
 */

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import reactivemongo.bson.BSONString
import spray.caching.Cache
import scala.concurrent.duration._

object SecurityUtil {


  def calculateHMAC(secret: String, applicationName: String, hostname: String): String = {
    val signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
    val mac = Mac.getInstance("HmacSHA1");
    mac.init(signingKey);
    val rawHmac = mac.doFinal((applicationName + "|" + hostname).getBytes());

    new String(Base64.encodeBase64(rawHmac));
  }

  def checkHMAC(secret: String, applicationName: String, hostname: String, hmac: String): Boolean = {
    return calculateHMAC(secret, applicationName, hostname) == hmac;
  }

  def main(args: Array[String]) {
    val hmac = SecurityUtil.calculateHMAC("The passphrase to calculate the secret with", "App 1", "localhost");
    println(hmac);
    println(SecurityUtil.checkHMAC("The passphrase to calculate the secret with", "App 1", "localhost", hmac));
  }
}
