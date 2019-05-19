package org.gudy.azureus2.core3.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class SHA1Hasher {
  private final MessageDigest sha1;

  public SHA1Hasher() {
    try {
      sha1 = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  
  /**
   * Calculate the SHA-1 hash for the given bytes.
   * @param bytes data to hash
   * @return 20-byte hash
   */
  public byte[] calculateHash( byte[] bytes ) {
    sha1.reset();
    sha1.update(bytes);
    return sha1.digest();
  }

  public byte[] calculateHash( ByteBuffer bb ) {
    sha1.reset();
    sha1.update(bb);
    return sha1.digest();
  }
  
  
  /**
   * Start or continue a hash calculation with the given data.
   * @param data input
   */
  public void update( byte[] data ) {
  	sha1.update(data);
  }
  
  
  /**
   * Start or continue a hash calculation with the given data,
   * starting at the given position, for the given length.
   * @param data input
   * @param pos start position
   * @param len length
   */
  public void update( byte[] data, int pos, int len ) {
  	sha1.update(data, pos, len);
  }
  

  /**
   * Finish the hash calculation.
   * @return 20-byte hash
   */
  public byte[] getDigest() {
  	return sha1.digest();
  }
  
  public HashWrapper getHash() {
  	return new HashWrapper(sha1.digest());
  }  
  
  /**
   * Resets the hash calculation.
   */
  public void reset() {
    sha1.reset();
  }
  
}
