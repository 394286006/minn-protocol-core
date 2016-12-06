package p.minn.packet.amf;

import java.nio.ByteBuffer;

import p.minn.packet.Frame;

/**
 * @author minn
 * @QQ:394286006
 * 
 */
public class DataFrame extends Frame {
  
  public static final int FRAME_LENGTH_128=128;
  
  public static final int FRAME_LENGTH_65=65;
  
  public static final String _RESULT="_result";
  
  private ByteBuffer buffer =null;
  
  public DataFrame(byte[] header, byte[] data) {
    super(header, data);
     buffer=ByteBuffer.allocate(header.length+data.length);
     buffer.put(header);
     buffer.put(data);
  }
  
  public ByteBuffer getAllData(){
    buffer.flip();
    return buffer;
  }
  
}
