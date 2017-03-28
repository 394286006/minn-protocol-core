package p.minn.handshake;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import p.minn.handshake.BaseHandShake;
import p.minn.listener.ControllerEventListener;
import p.minn.packet.amf.AmfDecoder;
import p.minn.packet.amf.DataFrame;
import p.minn.packet.amf.RtmpDecoder;
import p.minn.packet.amf.RtmpMessageFactory;
import p.minn.packet.amf.RtmpPacket;
import p.minn.packet.amf.RtmpWrapper;
import p.minn.packet.amf.TypeValue;
import p.minn.utils.RtmpConstants;


/**
 * @author minn
 * @QQ:394286006
 *
 */
public class RtmpHandShake extends BaseHandShake<RtmpPacket,RtmpWrapper> {

  protected KeyAgreement keyAgreement;
  private byte handshakeType;
  public byte[] handshakeBytes;
  public  byte versionByte=0;
  public int validationScheme=-1;
  private Mac hmacSHA256;
  protected static final Random random = new Random();
  int digestOffset = -1;
  private byte[] c1digest;
  private byte[] c1key;
  int c1keyoffset=-1;
          
  public RtmpHandShake(ControllerEventListener<RtmpPacket, RtmpWrapper> evt) {
    super(evt);
    handshakeBytes=new byte[1536];
    try {
      hmacSHA256 = Mac.getInstance("HmacSHA256");
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void step() throws Exception {
    for (SocketChannel socket : sockets) {
      state = 0;
      isHsp=false;
      client = null;
   
      receivebuffer = ByteBuffer.allocate(1537);
      while (client == null) {
        if (!isHsp) {
          try{
          handshake(socket);
          }catch(Exception e){
            e.printStackTrace();
            removesockets.add(socket);
            System.exit(1);
            break;
          }
        } else  {
           client=evt.createClient(uuid,socket);
           evt.loginClien(client.clientId, client);
           client.setHs(true);
          
        }
      }
      removesockets.add(socket);
    }
  }

  public void handshake(SocketChannel socket) throws Exception {
    int bytes = socket.read(receivebuffer);
    ByteBuffer answer = null;
    System.out.println("state:"+state);
        switch (state) {
        case 0:
            if (bytes == -1)
                throw new IOException("Disconnected at handshake");
            //receivebuffer.rewind();
           // byte[] bodys=new byte[128];
            //System.arraycopy(receivebuffer.array(), 1, bodys, 0, 128);  
            //System.out.println("Player bodys : {}"+ new String(bodys));
          //  byte[] time=new byte[3];
           // System.arraycopy(receivebuffer.array(),1, time, 0, 3); 
           // byte[] bIn=receivebuffer.array();
          //  handshakeType=bIn[0];
            
          //  byte[] ver = new byte[4];
           // System.arraycopy(bIn, 3, ver, 0, 4);        
            //byte[] buf = new byte[128];
            //System.arraycopy(bIn, 0, buf, 0, 128);
            //sbyte versionByte = bIn[4];
           // time[0]=(byte)((time[0]&0xFF)<<16);
            //time[1]=(byte)((time[1]&0xFF)<<8);
         //   System.out.println("Player time : {}"+ (time[0]|time[1]|(time[2]&0xFF)));
           // System.out.println("Player version byte: {}"+ (new String(ver)));
            //System.out.println("Player version byte: {}"+ (versionByte & 0x0ff));
            //System.out.println("Hex: {}"+Hex.encodeHexString(buf));
//          System.out.println("flash Version string: "+Hex.encodeHexString(ver));
            //System.out.println("Detecting flash player version :"+(bIn[4] & 0x0ff)+"," +(bIn[5] & 0x0ff)+","+ (bIn[6] & 0x0ff)+","+ (bIn[7] & 0x0ff));
            receivebuffer.flip();
            handshakeType=receivebuffer.get();
            // System.out.println("handshakeType:"+handshakeType);
            // byte[] pBuffer = new byte[1536];
             
            // input.get(pBuffer, 1, 1536);       
             System.arraycopy(receivebuffer.array(), 1, handshakeBytes, 0, 1536);
            // System.arraycopy(pBuffer, 0, handshakeBytes, 0, 1536);
             
             if (validateScheme(handshakeBytes, 0)) {
               validationScheme = 0;
             }
           if (validateScheme(handshakeBytes, 1)) {
               validationScheme = 1;
           }
            if (validationScheme!=-1){
                getC1KeyData(digestOffset,handshakeBytes);
                state = 1;
                receivebuffer=ByteBuffer.allocate(3073);
            }else{
              this.removesockets.add(socket);
            }
            
            break;
        case 1:
          this.generateKeyAgreement();
          byte[] incomingPublicKey=getSharedSecret(c1key,this.keyAgreement);
          System.arraycopy(incomingPublicKey, 0, handshakeBytes, this.c1keyoffset, RtmpConstants.KEY_LENGTH);
          byte[] tempBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
          System.arraycopy(handshakeBytes, 0, tempBuffer, 0, digestOffset);
          System.arraycopy(handshakeBytes, digestOffset + RtmpConstants.DIGEST_LENGTH, tempBuffer, digestOffset, RtmpConstants.HANDSHAKE_SIZE - digestOffset - RtmpConstants.DIGEST_LENGTH);     
          byte[] s1digest = calculateHMAC_SHA256(tempBuffer, RtmpConstants.GENUINE_FMS_KEY, 36);
          System.arraycopy(s1digest, 0, handshakeBytes, this.digestOffset, RtmpConstants.DIGEST_LENGTH);
          byte[]  tempHash = calculateHMAC_SHA256(this.c1digest, RtmpConstants.GENUINE_FMS_KEY, 68);

          byte[] randBytes = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
          random.nextBytes(randBytes);
          byte[] lastHash = calculateHMAC_SHA256(randBytes, tempHash, RtmpConstants.DIGEST_LENGTH);
         
          answer = ByteBuffer.allocate(3073);
          answer.put( handshakeType);
          answer.put(handshakeBytes);
          answer.put(randBytes);
          answer.put(lastHash);
        // System.out.println("last pos:"+answer.position());
          answer.flip();
         // answer=protocolUtil.handshake(receivebuffer);
            state = 2;
            receivebuffer = ByteBuffer.allocate(1536);
            break;
        case 2:
          try{
            if (bytes == -1)
                throw new IOException("Disconnected at handshake");
            if(bytes==0){
              return;
            }
           
            RtmpPacket packet = RtmpDecoder.readData(receivebuffer);
            if(packet==null){
              return;
            } 
            
            RtmpWrapper wrapper=AmfDecoder.decodeInvoke(packet.body);
            Map<String,TypeValue> param= wrapper.getInfo().get(0).hashValue;
            uuid=param.get("uuid").stringValue;
             client=evt.getClient(uuid);
             if(client!=null){
               client.existsMessage(wrapper.getId());
             }else{
              
                Map<String,TypeValue> args =RtmpMessageFactory.infoMessage(-1,"status","NetConnection.Connect.Success",uuid,"用户登录");
                RtmpPacket back= RtmpMessageFactory.resultMessage(wrapper.getId(),0, 0x03,0x02, args);
                List<DataFrame> dfs=back.getFrames();
                for(DataFrame df:dfs){
                  socket.write(df.getAllData());
                }
             }
          }catch(Exception e){
            e.printStackTrace();
          }
            isHsp=true;
            this.removesockets.add(socket);
            lasttime-=RtmpConstants.INTERVAL;
            
            break;
        }
        if(answer!=null)
        socket.write(answer);

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
  
  protected void generateKeyAgreement() {
    DHParameterSpec keySpec = new DHParameterSpec(RtmpConstants.DH_MODULUS, RtmpConstants.DH_BASE);
    try {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        keyGen.initialize(keySpec);
        KeyPair keyPair = keyGen.generateKeyPair();
        keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(keyPair.getPrivate());
    } catch (Exception e) {
        System.out.println("Error generating keypair:"+e);
    }
  }
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
}
