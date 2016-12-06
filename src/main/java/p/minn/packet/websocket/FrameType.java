package p.minn.packet.websocket;

import java.util.EnumSet;

/**
 * @author minn
 * @QQ:394286006
 *
 */
public enum FrameType {
   FIRST_FRAME_UNMASKED((byte)0x01),CONTINUE_FRAME_UNMASKED((byte)0x00),END_FRAME_UNMASKED((byte)0x80),SINGLE_FRAME_UNMASKED((byte)0x81),Undefined((byte)0xFF);
    
    public byte type;
    
    private static EnumSet<FrameType> currEnumSet = EnumSet.range(FIRST_FRAME_UNMASKED, SINGLE_FRAME_UNMASKED);
    
    private FrameType(byte type){
      this.type=type;
    }
    
    public static FrameType valueOf(int type){
      FrameType temp=FrameType.Undefined;
      for (FrameType chunktype : currEnumSet) {
           if(chunktype.type==(byte)type){
             temp=chunktype;
             break;
           }
      }
      if(temp.type==FrameType.Undefined.type){
        System.out.println( "Invalid chunktype size: " +type );
      }
      return temp;
    }
}
