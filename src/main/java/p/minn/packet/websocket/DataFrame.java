package p.minn.packet.websocket;

import p.minn.packet.Frame;

/**
 * @author minn
 * @QQ:394286006
 * 
 */
public class DataFrame extends Frame {
  
  public static final int FRAME_LENGTH=125;
  
  public static final int FRAME_X00=0x00;
  
  public static final int FRAME_X01=0x01;
  
  public static final int FRAME_X80=0x80;
  
  public static final int FRAME_X81=0x81;
  
  public DataFrame(byte header, byte[] data) {
    super(header, data);
  }
  
}
