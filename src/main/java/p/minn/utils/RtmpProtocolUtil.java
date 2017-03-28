package p.minn.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.KeySpec;
import java.util.List;
import java.util.Random;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import p.minn.packet.amf.TypeValue;


public class RtmpProtocolUtil {
 
  private byte handshakeType;
  public byte[] handshakeBytes;
  public  byte versionByte=0;
  public int validationScheme=0;
  private Mac hmacSHA256;
  protected static final Random random = new Random();
  
  int digestOffset = -1;
  private byte[] c1digest;
  private byte[] c1key;
  int c1keyoffset=-1;
  // servers public key
  protected byte[] incomingPublicKey;

  // clients public key
  protected byte[] outgoingPublicKey;
      
  // swf verification bytes
  protected KeyAgreement keyAgreement;
  
  private static RtmpProtocolUtil protocolUtil;
  
  static { 
    //get security provider 
    Security.addProvider(new BouncyCastleProvider());   
   } 
  
  public RtmpProtocolUtil() {
    super();
    try {
      handshakeBytes=new byte[1536];
      hmacSHA256 = Mac.getInstance("HmacSHA256");
  } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
  }
  }
  
  public static RtmpProtocolUtil getInstance(){
    if(protocolUtil==null){
      protocolUtil=new RtmpProtocolUtil();
    }
    return protocolUtil;
  }
  
  public ByteBuffer handshake(ByteBuffer receivebuffer){
    ByteBuffer answer = null;
   // receivebuffer.mark();
    //protocolUtil.validate(receivebuffer);
    //receivebuffer.reset();
   // receivebuffer.mark();
   // System.arraycopy(receivebuffer.array(), 1, handshakeBytes, 0, 1536);
   // protocolUtil.prepareResponse(handshakeBytes,receivebuffer);
    //receivebuffer.reset();
    //receivebuffer.mark();
   // int serverDigestOffset = protocolUtil.getDigestOffset(handshakeBytes);
   // byte[] tempBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
    ///System.arraycopy(handshakeBytes, 0, tempBuffer, 0, serverDigestOffset);
    //System.arraycopy(handshakeBytes, serverDigestOffset + RtmpConstants.DIGEST_LENGTH, tempBuffer, serverDigestOffset, RtmpConstants.HANDSHAKE_SIZE - serverDigestOffset - RtmpConstants.DIGEST_LENGTH);            
    //calculate the hash
    //byte[] tempHash = protocolUtil.calculateHMAC_SHA256(tempBuffer, RtmpConstants.GENUINE_FMS_KEY, 36);
    //add the digest 
    //System.arraycopy(tempHash, 0, handshakeBytes, serverDigestOffset, RtmpConstants.DIGEST_LENGTH);
    //compute the challenge digest
   // byte[] inputBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
    //log.debug("Before get: {}", input.position());
    //receivebuffer.get(inputBuffer);
    //log.debug("After get: {}", input.position());
   // int keyChallengeIndex = protocolUtil.getDigestOffset(inputBuffer);
   // byte[] challengeKey = new byte[RtmpConstants.DIGEST_LENGTH];
  //  receivebuffer.position(keyChallengeIndex);
    //receivebuffer.get(challengeKey, 0, RtmpConstants.DIGEST_LENGTH);           
   // receivebuffer.reset();
    //compute key
    byte[] inputBuffer = new byte[receivebuffer.limit()];
    System.out.println("validationScheme:"+validationScheme);
    System.out.println("inputBuffer size:"+inputBuffer.length+":"+receivebuffer.position());
//  input.rewind();
    receivebuffer.get(inputBuffer);
    //get the clients dh offset
   // int clientDHOffset = getDHOffset(inputBuffer);

    //get the clients public key
   // outgoingPublicKey = new byte[RtmpConstants.KEY_LENGTH];
   // System.arraycopy(inputBuffer, clientDHOffset, outgoingPublicKey, 0, RtmpConstants.KEY_LENGTH);   
    //get the servers dh offset
   // int serverDHOffset = getDHOffset(handshakeBytes);
 //   System.out.println("Outgoing DH offset: {}"+ serverDHOffset);
    //create keypair
    KeyPair keys = generateKeyPair();
    //get public key
 //  incomingPublicKey = getPublicKey(keys);
   // System.out.println("c1key:"+outgoingPublicKey.length);
   // incomingPublicKey=getSharedSecret(c1digest,this.keyAgreement);
    incomingPublicKey=getSharedSecret(c1key,this.keyAgreement);
    //add to handshake bytes
    System.arraycopy(incomingPublicKey, 0, handshakeBytes, this.c1keyoffset, RtmpConstants.KEY_LENGTH);
    byte[] tempBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
    System.arraycopy(handshakeBytes, 0, tempBuffer, 0, digestOffset);
    System.arraycopy(handshakeBytes, digestOffset + RtmpConstants.DIGEST_LENGTH, tempBuffer, digestOffset, RtmpConstants.HANDSHAKE_SIZE - digestOffset - RtmpConstants.DIGEST_LENGTH);     
   byte[] s1digest = calculateHMAC_SHA256(tempBuffer, RtmpConstants.GENUINE_FMS_KEY, 36);
   System.arraycopy(s1digest, 0, handshakeBytes, this.digestOffset, RtmpConstants.DIGEST_LENGTH);
   
   
   
  byte[]  tempHash = protocolUtil.calculateHMAC_SHA256(this.c1digest, RtmpConstants.GENUINE_FMS_KEY, 68);
    //generate hash
    byte[] randBytes = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
    random.nextBytes(randBytes);
    byte[] lastHash = protocolUtil.calculateHMAC_SHA256(randBytes, tempHash, RtmpConstants.DIGEST_LENGTH);
    
   
    answer = ByteBuffer.allocate(3073);
    System.out.println("first pos:"+answer.position());
    answer.put( handshakeType);
    answer.put(handshakeBytes);
    answer.put(randBytes);
    
    answer.put(lastHash);
   System.out.println("last pos:"+answer.position());
    answer.flip();
    return answer;
  }
  
  
  public boolean validate(ByteBuffer input) {
    handshakeType=input.get();
   // System.out.println("handshakeType:"+handshakeType);
    byte[] pBuffer = new byte[1536];
    
   // input.get(pBuffer, 1, 1536);       
    System.arraycopy(input.array(), 1, pBuffer, 0, 1536);
    System.arraycopy(pBuffer, 0, handshakeBytes, 0, 1536);
   // byte versionByte = pBuffer[3];
    //System.out.println("versionByte:"+versionByte);
    
   // if(versionByte==0){
     // return false;
   // }
    if (validateScheme(pBuffer, 0)) {
        validationScheme = 0;
        
        return true;
    }
    if (validateScheme(pBuffer, 1)) {
        validationScheme = 1;
    
        return true;
    }
    
    return false;
}

public void prepareResponse(byte[] handshakeBytes,ByteBuffer input) {
    //put the clients input into a byte array
    byte[] inputBuffer = new byte[input.limit()];
    System.out.println("validationScheme:"+validationScheme);
    System.out.println("inputBuffer size:"+inputBuffer.length+":"+input.position());
//  input.rewind();
    input.get(inputBuffer);
    //get the clients dh offset
   // int clientDHOffset = getDHOffset(inputBuffer);

    //get the clients public key
   // outgoingPublicKey = new byte[RtmpConstants.KEY_LENGTH];
   // System.arraycopy(inputBuffer, clientDHOffset, outgoingPublicKey, 0, RtmpConstants.KEY_LENGTH);   
    //get the servers dh offset
   // int serverDHOffset = getDHOffset(handshakeBytes);
 //   System.out.println("Outgoing DH offset: {}"+ serverDHOffset);
    //create keypair
    KeyPair keys = generateKeyPair();
    //get public key
 //  incomingPublicKey = getPublicKey(keys);
   // System.out.println("c1key:"+outgoingPublicKey.length);
   // incomingPublicKey=getSharedSecret(c1digest,this.keyAgreement);
    incomingPublicKey=getSharedSecret(c1key,this.keyAgreement);
    //add to handshake bytes
    System.arraycopy(incomingPublicKey, 0, handshakeBytes, this.c1keyoffset, RtmpConstants.KEY_LENGTH);
    byte[] tempBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
    System.arraycopy(handshakeBytes, 0, tempBuffer, 0, digestOffset);
    System.arraycopy(handshakeBytes, digestOffset + RtmpConstants.DIGEST_LENGTH, tempBuffer, digestOffset, RtmpConstants.HANDSHAKE_SIZE - digestOffset - RtmpConstants.DIGEST_LENGTH);     
   byte[] s1digest = calculateHMAC_SHA256(tempBuffer, RtmpConstants.GENUINE_FMS_KEY, 36);
   System.arraycopy(s1digest, 0, handshakeBytes, this.digestOffset, RtmpConstants.DIGEST_LENGTH);
} 


protected int getDHOffset(byte[] bytes) {
    int dhOffset = -1;
    switch (validationScheme) {
        case 1:
            dhOffset = getDHOffset1(bytes);
            break;
        default:
            System.out.println("Scheme 0 will be used for DH offset");
        case 0:
            dhOffset = getDHOffset0(bytes);
    }  
    return dhOffset;
}
protected int getDHOffset0(byte[] bytes) {
    int offset = (bytes[1532] & 0x0ff) + (bytes[1533] & 0x0ff) + (bytes[1534] & 0x0ff) + (bytes[1535] & 0x0ff);
    offset = offset % 632;
    offset = offset + 772;
    if (offset + RtmpConstants.KEY_LENGTH >= 1536) {
        System.out.println("Invalid DH offset");
    }
    return offset;
}
protected int getDHOffset1(byte[] bytes) {
    int offset = (bytes[768] & 0x0ff) + (bytes[769] & 0x0ff) + (bytes[770] & 0x0ff) + (bytes[771] & 0x0ff);
    offset = offset % 632;
    offset = offset + 8;
    if (offset + RtmpConstants.KEY_LENGTH >= 1536) {
         System.out.println("Invalid DH offset");
    }
    return offset;
}   


protected KeyPair generateKeyPair() {
    KeyPair keyPair = null;
    DHParameterSpec keySpec = new DHParameterSpec(RtmpConstants.DH_MODULUS, RtmpConstants.DH_BASE);
    try {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        keyGen.initialize(keySpec);
        keyPair = keyGen.generateKeyPair();
        keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(keyPair.getPrivate());
    } catch (Exception e) {
        System.out.println("Error generating keypair:"+e);
    }
    return keyPair;
}

/**
 * Determines the validation scheme for given input. 
 *  
 * @param otherPublicKeyBytes 
 * @param agreement 
 * @return shared secret bytes if client used a supported validation scheme 
 */ 
protected static byte[] getSharedSecret(byte[] otherPublicKeyBytes, KeyAgreement agreement) { 
 BigInteger otherPublicKeyInt = new BigInteger(1, otherPublicKeyBytes); 
 try { 
  KeyFactory keyFactory = KeyFactory.getInstance("DH"); 
  KeySpec otherPublicKeySpec = new DHPublicKeySpec(otherPublicKeyInt, RtmpConstants.DH_MODULUS, RtmpConstants.DH_BASE); 
  PublicKey otherPublicKey = keyFactory.generatePublic(otherPublicKeySpec); 
  //agreement.init(otherPublicKey);
  //agreement = KeyAgreement.getInstance("DH");
  //agreement.init(otherPublicKey);
  agreement.doPhase(otherPublicKey, true); 
 } catch (Exception e) { 
  e.printStackTrace(); 
 } 
 byte[] sharedSecret = agreement.generateSecret();
 System.out.println("Shared secret [{}]: {}"+ sharedSecret.length+","+ Hex.encodeHexString(sharedSecret)); 
 return sharedSecret; 
}  

/**
 * Returns the public key for a given key pair.
 * 
 * @param keyPair
 * @return public key
 */
protected static byte[] getPublicKey(KeyPair keyPair) {
     DHPublicKey incomingPublicKey = (DHPublicKey) keyPair.getPublic();
     BigInteger dhY = incomingPublicKey.getY();
     System.out.println("Public key: {}"+ dhY);
     byte[] result = dhY.toByteArray();
     System.out.println("Public key as bytes - length [{}]: {}"+ result.length+","+ Hex.encodeHexString(result));
     byte[] temp = new byte[RtmpConstants.KEY_LENGTH];
     if (result.length < RtmpConstants.KEY_LENGTH) {
         System.arraycopy(result, 0, temp, RtmpConstants.KEY_LENGTH - result.length, result.length);
         result = temp;
          System.out.println("Padded public key length to 128");
     } else if(result.length > RtmpConstants.KEY_LENGTH){
         System.arraycopy(result, result.length - RtmpConstants.KEY_LENGTH, temp, 0, RtmpConstants.KEY_LENGTH);
         result = temp;
          System.out.println("Truncated public key length to 128");
     }
     return result;
}
public int getDigestOffset(byte[] pBuffer) {
    int serverDigestOffset = -1;
    switch (validationScheme) {
        case 1:
            serverDigestOffset = getDigestOffset1(pBuffer);
            break;
        default:
            
        case 0:
            serverDigestOffset = getDigestOffset0(pBuffer);
    }  
    return serverDigestOffset;
}
private boolean validateScheme(byte[] pBuffer, int scheme) {
    
    switch (scheme) {
        case 0:
            digestOffset = getDigestOffset0(pBuffer);
            break;
        case 1:
            digestOffset = getDigestOffset1(pBuffer);
            break;
        default:
            System.out.println("Unknown scheme: {}"+scheme);
    }   

    byte[] tempBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
    System.arraycopy(pBuffer, 0, tempBuffer, 0, digestOffset);
    System.arraycopy(pBuffer, digestOffset + RtmpConstants.DIGEST_LENGTH, tempBuffer, digestOffset, RtmpConstants.HANDSHAKE_SIZE - digestOffset - RtmpConstants.DIGEST_LENGTH);     

    c1digest = calculateHMAC_SHA256(tempBuffer, RtmpConstants.GENUINE_FP_KEY, 30);
    boolean result = true;
    for (int i = 0; i < RtmpConstants.DIGEST_LENGTH; i++) {
        if (pBuffer[digestOffset + i] != c1digest[i]) {
            result = false;
            break;
        }
    }
    getC1KeyData(digestOffset,pBuffer);
    return result;  
}
protected void getC1KeyData(int digestOffset,byte[] pBuffer){
  c1key=new byte[RtmpConstants.KEY_LENGTH];
  switch (validationScheme) {
    case 0:
      c1keyoffset=(pBuffer[1532] & 0x0FF) + (pBuffer[1533] & 0x0FF) + (pBuffer[1534] & 0x0FF) + (pBuffer[1535] & 0x0FF);
      c1keyoffset = c1keyoffset % 728;
      c1keyoffset = c1keyoffset + 12;
      break;
    case 1:
      c1keyoffset =  (pBuffer[768] & 0x0FF) + (pBuffer[769] & 0x0FF) +(pBuffer[770] & 0x0FF) + (pBuffer[771] & 0x0FF);
      c1keyoffset = c1keyoffset % 728;
      c1keyoffset = c1keyoffset + 776;
      break;
      default:
        System.out.println("invalid key");
  }
  System.arraycopy(pBuffer, c1keyoffset, c1key, 0, RtmpConstants.KEY_LENGTH);
}

protected int getDigestOffset0(byte[] pBuffer) {
    int offset = (pBuffer[8] & 0x0FF) + (pBuffer[9] & 0x0FF) + (pBuffer[10] & 0x0FF) + (pBuffer[11] & 0x0FF);
    offset = offset % 728;
    offset = offset + 12;
    if (offset + RtmpConstants.DIGEST_LENGTH >= 1536) {
        System.out.println("Invalid digest offset");
    }
    return offset;
}
protected int getDigestOffset1(byte[] pBuffer) {
    int offset =  (pBuffer[772] & 0x0FF) + (pBuffer[773] & 0x0FF) +(pBuffer[774] & 0x0FF) + (pBuffer[775] & 0x0FF);
    offset = offset % 728;
    offset = offset + 776;
    if (offset + RtmpConstants.DIGEST_LENGTH >= 1536) {
         System.out.println("Invalid digest offset");
    }
    return offset;
}

public byte[] calculateHMAC_SHA256(byte[] input, byte[] key) { 
  byte[] output = null; 
  try { 
   hmacSHA256.init(new SecretKeySpec(key, "HmacSHA256")); 
   output = hmacSHA256.doFinal(input); 
  } catch (InvalidKeyException e) { 
   e.printStackTrace();
  } 
  return output; 
 } 
public byte[] calculateHMAC_SHA256(byte[] input, byte[] key, int length) {
    byte[] output = null;
    try {
          hmacSHA256.init(new SecretKeySpec(key,0 , length, "HmacSHA256"));
        output = hmacSHA256.doFinal(input);
    } catch (InvalidKeyException e) {
        System.out.println("Invalid key:"+ e);
    }
    return output;
}

public static void traceWrappers(List<TypeValue> ws,String desc)
{
    System.out.println("**********"+desc+"**************");
    for(int i=0;i<ws.size();i++)
    {
    System.out.println("num"+i+","+ws.get(i).toString());
    }
    System.out.println("**********end***********");
}

public static void main(String[] args){
  byte[] bs=new byte[3];
  bs[0]=(byte)1;
  bs[1]=(byte)2;
  System.out.println((bs[0]&0xFF)+(bs[1]&0xFF));
  System.out.println(((bs[0]&0xff)<<8)|(bs[1]&0xFF));
}
}
