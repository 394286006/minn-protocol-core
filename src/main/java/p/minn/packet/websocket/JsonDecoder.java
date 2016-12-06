package p.minn.packet.websocket;



import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import p.minn.listener.ClientEventListener;
import p.minn.packet.Decoder;
import p.minn.utils.WebSocketConstants;
import p.minn.utils.WebSocketProtocolUtil;


/**
 * @author minn
 * @QQ:394286006
 * 
 */
public class JsonDecoder extends Decoder<JsonPacket,JsonWrapper>
{
	

	public JsonDecoder (ClientEventListener<JsonPacket,JsonWrapper> evt,SocketChannel socket )
	{
		super(evt,socket);
	}
	
	protected void read(ByteBuffer buffer) throws Exception 
	{
	   byte[] data=buffer.array();
	    JsonPacket  packet =new JsonPacket();
		int headerFlag = data[0]& 0x0F;
		packet.bodyType=MessageType.valueOf(headerFlag);
        packet.bodySize=WebSocketProtocolUtil.getSizeOfPayload(data[1]);
        packet.setBody(WebSocketProtocolUtil.unMask(Arrays.copyOfRange(data, 2, WebSocketConstants.MASK_SIZE_EXT), Arrays.copyOfRange(data, WebSocketConstants.MASK_SIZE_EXT,WebSocketConstants.MASK_SIZE_EXT+ packet.bodySize)));
        if(headerFlag==1){
          evt.onEvent(new JsonWrapper(packet),packet);
        }else{
          logger.error(new String(packet.body));
        }
	}

	
}
