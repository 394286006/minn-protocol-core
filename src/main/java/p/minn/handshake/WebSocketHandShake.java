package p.minn.handshake;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import p.minn.common.utils.UtilCommon;
import p.minn.handshake.BaseHandShake;
import p.minn.listener.ControllerEventListener;
import p.minn.packet.websocket.DataFrame;
import p.minn.packet.websocket.JsonPacket;
import p.minn.packet.websocket.JsonWrapper;
import p.minn.utils.WebSocketConstants;
import p.minn.utils.WebSocketProtocolUtil;


/**
 * @author minn
 * @QQ:394286006
 *
 */
public class WebSocketHandShake extends BaseHandShake<JsonPacket, JsonWrapper> {

  public WebSocketHandShake(ControllerEventListener<JsonPacket, JsonWrapper> evt) {
    super(evt);
  }

  public void step() throws Exception {
    for (SocketChannel socket : sockets) {
      state = 0;
      isHsp=false;
      client = null;
      receivebuffer = ByteBuffer.allocate(1537);
      while (client == null) {
        client=evt.getClient(uuid);
        if(client!=null){
          removesockets.add(socket);
          client.existsMessage(0);
         break;
        }
        if (!isHsp) {
          try{
            handshake(socket);
          }catch(Exception e){
            e.printStackTrace();
            removesockets.add(socket);
            break;
          }
        } else  {
            client = evt.createClient(uuid,socket);
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
        if (bytes == -1){
          throw new IOException("Disconnected at handshake");
        }
        byte[] res = WebSocketProtocolUtil.getHandShakeResponse(receivebuffer);
        answer = ByteBuffer.allocate(res.length);
        answer.put(res);
        state = 1;
        receivebuffer = ByteBuffer.allocate(2);
        break;
      case 1:
        byte[] buf = receivebuffer.array();
        int opcode = buf[0] & 0x0F;
        if (opcode == 1) {
          final int payloadSize = WebSocketProtocolUtil.getSizeOfPayload(buf[1]);
          state = 2;
          receivebuffer = ByteBuffer.allocate(WebSocketConstants.MASK_SIZE + payloadSize);
        } else {
          throw new IOException("Disconnected at handshake");
        }
        break;
      case 2:
        buf = receivebuffer.array();
        buf =
            WebSocketProtocolUtil.unMask(Arrays.copyOfRange(buf, 0, WebSocketConstants.MASK_SIZE),
                Arrays.copyOfRange(buf, WebSocketConstants.MASK_SIZE, buf.length));
        JsonWrapper wrapper=new JsonWrapper(buf);
        uuid=wrapper.getUuid();
        Map<String,Object> data=new HashMap<String,Object>();
        Map<String,Object> connect=new HashMap<String,Object>();
        connect.put("msg", "登录成功");
        data.put("data", connect);
        data.put("uuid", uuid);
        data.put("status", WebSocketConstants.CONNECTED);
        buf=UtilCommon.gson2Str(data).getBytes();
        BufferedOutputStream out = new BufferedOutputStream(socket.socket().getOutputStream());
        out.write(DataFrame.FRAME_X81);
        out.write(buf.length);
        out.write(buf);
        out.flush();
        this.isHsp = true;
        this.lasttime-=WebSocketConstants.INTERVAL;
        break;
    }
    if (answer != null) {
      BufferedOutputStream out = new BufferedOutputStream(socket.socket().getOutputStream());
      out.write(answer.array());
      out.flush();
    }
  }


}
