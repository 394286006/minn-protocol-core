package p.minn.packet.amf;

import java.util.EnumSet;

/**
 * @author minn
 * @QQ:394286006
 *
 */
public enum MessageType {
   PacketSize((byte)0x01),Read((byte)0x03),Ping((byte)0x04),ServerBandWidth((byte)0x05),ClientBandWidth((byte)0x06),Audit((byte)0x08),Video((byte)0x09),Invoke((byte)0x12),DataMessage((byte)0x13),Command((byte)0x14),Undefined((byte)0xFF);

   public byte type;
   
   private static EnumSet<MessageType> currEnumSet = EnumSet.range(PacketSize, Command);

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
