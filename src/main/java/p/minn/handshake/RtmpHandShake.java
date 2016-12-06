package p.minn.handshake;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import p.minn.utils.RtmpProtocolUtil;


/**
 * @author minn
 * @QQ:394286006
 *
 */
public class RtmpHandShake extends BaseHandShake<RtmpPacket,RtmpWrapper> {

  public byte handshakeType;
  private byte[] handshakeBytes;
  protected static final Random random = new Random();
  private RtmpProtocolUtil protocolUtil;
          
  public RtmpHandShake(ControllerEventListener<RtmpPacket, RtmpWrapper> evt) {
    super(evt);
    handshakeBytes=new byte[1536];
    protocolUtil=RtmpProtocolUtil.getInstance();
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
        switch (state) {
        case 0:
            if (bytes == -1)
                throw new IOException("Disconnected at handshake");
            receivebuffer.flip();
            byte[] bIn=receivebuffer.array();
            handshakeType=bIn[0];
            byte[] ver = new byte[4];
            System.arraycopy(bIn, 3, ver, 0, 4);        
            byte[] buf = new byte[128];
            System.arraycopy(bIn, 0, buf, 0, 128);
            byte versionByte = bIn[4];
           // System.out.println("Player handshakeType : {}"+ (handshakeType));
            //System.out.println("Player version byte: {}"+ (versionByte));
            //System.out.println("Player version byte: {}"+ (versionByte & 0x0ff));
            //System.out.println("Hex: {}"+Hex.encodeHexString(buf));
//          System.out.println("flash Version string: "+Hex.encodeHexString(ver));
            //System.out.println("Detecting flash player version :"+(bIn[4] & 0x0ff)+"," +(bIn[5] & 0x0ff)+","+ (bIn[6] & 0x0ff)+","+ (bIn[7] & 0x0ff));
          
            if (versionByte!=0){
                state = 1;
                receivebuffer=ByteBuffer.allocate(RtmpConstants.HANDSHAKE_SIZE_SERVER);
            }else{
              this.removesockets.add(socket);
            }
            break;
        case 1:
            //make sure this is a client we can communicate with
            receivebuffer.mark();
            protocolUtil.validate(receivebuffer);
            receivebuffer.reset();
            receivebuffer.mark();
          
            protocolUtil.prepareResponse(handshakeBytes,receivebuffer);
            receivebuffer.reset();
            receivebuffer.mark();
            int serverDigestOffset = protocolUtil.getDigestOffset(handshakeBytes);
            byte[] tempBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
            System.arraycopy(handshakeBytes, 0, tempBuffer, 0, serverDigestOffset);
            System.arraycopy(handshakeBytes, serverDigestOffset + RtmpConstants.DIGEST_LENGTH, tempBuffer, serverDigestOffset, RtmpConstants.HANDSHAKE_SIZE - serverDigestOffset - RtmpConstants.DIGEST_LENGTH);            
            //calculate the hash
            byte[] tempHash = protocolUtil.calculateHMAC_SHA256(tempBuffer, RtmpConstants.GENUINE_FMS_KEY, 36);
            //add the digest 
            System.arraycopy(tempHash, 0, handshakeBytes, serverDigestOffset, RtmpConstants.DIGEST_LENGTH);
            //compute the challenge digest
            byte[] inputBuffer = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
            //log.debug("Before get: {}", input.position());
            receivebuffer.get(inputBuffer);
            //log.debug("After get: {}", input.position());
            int keyChallengeIndex = protocolUtil.getDigestOffset(inputBuffer);
            byte[] challengeKey = new byte[RtmpConstants.DIGEST_LENGTH];
            receivebuffer.position(keyChallengeIndex);
            receivebuffer.get(challengeKey, 0, RtmpConstants.DIGEST_LENGTH);           
            receivebuffer.reset();
            //compute key
            tempHash = protocolUtil.calculateHMAC_SHA256(challengeKey, RtmpConstants.GENUINE_FMS_KEY, 68);
            //generate hash
            byte[] randBytes = new byte[RtmpConstants.HANDSHAKE_SIZE - RtmpConstants.DIGEST_LENGTH];
            random.nextBytes(randBytes);
            byte[] lastHash = protocolUtil.calculateHMAC_SHA256(randBytes, tempHash, RtmpConstants.DIGEST_LENGTH);
            
           
            answer = ByteBuffer.allocate(3073);
            
            answer.put( handshakeType);
            answer.put(handshakeBytes);
            answer.put(randBytes);
            
            answer.put(lastHash);
            
            answer.flip();
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

}
