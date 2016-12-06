package p.minn.packet.amf;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import p.minn.packet.Encoder;


public class RtmpEncoder  extends Encoder<RtmpPacket>
{
  
	public RtmpEncoder ( SocketChannel socket)
	{
      super(socket);
	}
	

	
  public void write(RtmpPacket packet) throws IOException {
    // TODO Auto-generated method stub
    List<DataFrame> frames=packet.getFrames();
    for(DataFrame df:frames){
      this.socket.write(df.getAllData());
    }
  }
  

    
}
