package p.minn.packet.amf;

import java.util.EnumSet;

/**
 * @author minn
 * @QQ:394286006
 *
 */
public enum ChunkType {
    Type0((byte)0x0C,(byte)0x00),Type1((byte)0x08,(byte)0x40),Type2((byte)0x04,(byte)0x80),Type3((byte)0x01,(byte)0xC0),Undefined((byte)0xFF,(byte)0xFF);
    
    public byte headerSize;
    public byte headerFlag;
    
    private static EnumSet<ChunkType> currEnumSet = EnumSet.range(Type0, Type3);
    
    private ChunkType(byte headerSize,byte headerFlag){
      this.headerSize=headerSize;
      this.headerFlag=headerFlag;
    }
    
    public static ChunkType valueOf(int headerFlag){
      ChunkType temp=ChunkType.Undefined;
      for (ChunkType chunktype : currEnumSet) {
           if(chunktype.headerFlag==(byte)headerFlag){
             temp=chunktype;
             break;
           }
      }
      if(temp.headerFlag==ChunkType.Undefined.headerFlag){
        System.out.println( "Invalid header size: " +headerFlag );
      }
      return temp;
    }
}
