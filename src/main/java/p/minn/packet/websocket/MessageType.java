package p.minn.packet.websocket;

import java.util.EnumSet;

/**
 * @author minn
 * @QQ:394286006
 *
 */
public enum MessageType {
  Continuation((byte)0x00),Text((byte)0x01),Binary((byte)0x02),Connection((byte)0x08),Ping((byte)0x09),Pong((byte)0x0A),Undefined((byte)0xFF);

   public byte type;
   
   private static EnumSet<MessageType> currEnumSet = EnumSet.range(Continuation, Pong);

   private MessageType(byte type){
     this.type=type;
   }
   
   public static MessageType valueOf(int type){
     MessageType temp=MessageType.Undefined;
     for (MessageType messagetype : currEnumSet) {
          if(messagetype.type==(byte)type){
            temp=messagetype;
            break;
          }
     }
     return temp;
   }
   
}
