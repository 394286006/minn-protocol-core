package p.minn.packet.amf;

import java.util.EnumSet;

/**
 * @author minn
 * @QQ:394286006
 *
 */
public  enum AmfType {
   Number((byte)0x00),Integer((byte)0xFF),Long((byte)0x0C),Boolean((byte)0x01),String((byte)0x02),Map((byte)0x03),Null((byte)0x05),MixedArray((byte)0x08),Array((byte)0x0A),XML((byte)0x0F),Date((byte)0x0B),Undefined((byte)0x06);
  
  public byte type;
  
  private static EnumSet<AmfType> currEnumSet = EnumSet.range(Number, Array);

  private AmfType(byte type){
    this.type=type;
  }
  
  public static AmfType valueOf(byte type){
    AmfType temp=AmfType.Undefined;
    for (AmfType amfType : currEnumSet) {
         if(amfType.type==type){
           temp=amfType;
           break;
         }
    }
    return temp;
  }
  
  
}
