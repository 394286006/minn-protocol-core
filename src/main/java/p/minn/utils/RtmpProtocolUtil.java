package p.minn.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import p.minn.packet.amf.TypeValue;


public class RtmpProtocolUtil {
 
  public  byte versionByte=0;
  public int validationScheme=0;
  private Mac hmacSHA256;
     
  // servers public key
  protected byte[] incomingPublicKey;

  // clients public key
  protected byte[] outgoingPublicKey;
      
  // swf verification bytes
  protected KeyAgreement keyAgreement;
  
  private static RtmpProtocolUtil protocolUtil;
  
  public RtmpProtocolUtil() {
    super();
    try {
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
  
  public boolean validate(ByteBuffer input) {
    byte[] pBuffer = new byte[input.remaining()];
    //put all the input bytes into our buffer
    input.get(pBuffer, 0, input.remaining());       
    
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
   // System.out.println("inputBuffer size:"+inputBuffer.length+":"+input.position());
//  input.rewind();
    input.get(inputBuffer);
    //get the clients dh offset
    int clientDHOffset = getDHOffset(inputBuffer);

    //get the clients public key
    outgoingPublicKey = new byte[RtmpConstants.KEY_LENGTH];
    System.arraycopy(inputBuffer, clientDHOffset, outgoingPublicKey, 0, RtmpConstants.KEY_LENGTH);      
    //get the servers dh offset
    int serverDHOffset = getDHOffset(handshakeBytes);
    //System.out.println("Outgoing DH offset: {}"+ serverDHOffset);
    //create keypair
    KeyPair keys = generateKeyPair();
    //get public key
    incomingPublicKey = getPublicKey(keys);
    //add to handshake bytes
    System.arraycopy(incomingPublicKey, 0, handshakeBytes, serverDHOffset, RtmpConstants.KEY_LENGTH);
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
 * Returns the public key for a given key pair.
 * 
 * @param keyPair
 * @return public key
 */
protected static byte[] getPublicKey(KeyPair keyPair) {
     DHPublicKey incomingPublicKey = (DHPublicKey) keyPair.getPublic();
     BigInteger dhY = incomingPublicKey.getY();
    // System.out.println("Public key: {}"+ dhY);
     byte[] result = dhY.toByteArray();
    // System.out.println("Public key as bytes - length [{}]: {}"+ result.length+","+ Hex.encodeHexString(result));
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
    int digestOffset = -1;
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

    byte[] tempHash = calculateHMAC_SHA256(tempBuffer, RtmpConstants.GENUINE_FP_KEY, 30);

    boolean result = true;
    for (int i = 0; i < RtmpConstants.DIGEST_LENGTH; i++) {
        //log.trace("Digest: {} Temp: {}", (pBuffer[digestOffset + i] & 0x0ff), (tempHash[i] & 0x0ff));
        if (pBuffer[digestOffset + i] != tempHash[i]) {
            result = false;
            break;
        }
    }

    return result;  
}

protected int getDigestOffset0(byte[] pBuffer) {
    
    int offset = (pBuffer[8] & 0x0ff) + (pBuffer[9] & 0x0ff) + (pBuffer[10] & 0x0ff) + (pBuffer[11] & 0x0ff);
    offset = offset % 728;
    offset = offset + 12;
    if (offset + RtmpConstants.DIGEST_LENGTH >= 1536) {
        System.out.println("Invalid digest offset");
    }
    return offset;
}
protected int getDigestOffset1(byte[] pBuffer) {
    
    int offset = (pBuffer[772] & 0x0ff) + (pBuffer[773] & 0x0ff) + (pBuffer[774] & 0x0ff) + (pBuffer[775] & 0x0ff);
    offset = offset % 728;
    offset = offset + 776;
    if (offset + RtmpConstants.DIGEST_LENGTH >= 1536) {
         System.out.println("Invalid digest offset");
    }
    return offset;
}

public byte[] calculateHMAC_SHA256(byte[] input, byte[] key, int length) {
    byte[] output = null;
    try {
        hmacSHA256.init(new SecretKeySpec(key, 0, length, "HmacSHA256"));
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
  System.out.println((3&0xC0));
}
}
