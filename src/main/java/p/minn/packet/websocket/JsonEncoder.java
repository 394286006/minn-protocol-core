package p.minn.packet.websocket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import p.minn.packet.Encoder;

/**
 * @author minn
 * @QQ:394286006
 * 
 */
public class JsonEncoder  extends Encoder<JsonPacket>
{
	public JsonEncoder ( SocketChannel socket)
	{
      super(socket);
	}

	public void write(JsonPacket packet) throws IOException
	{
	   for(DataFrame df:packet.getFrames()){
  	     out.write(df.getHeader());
  	     out.write(df.getLength());
         out.write(df.getData());
	   }
       out.flush();
		    
	}
    
}
