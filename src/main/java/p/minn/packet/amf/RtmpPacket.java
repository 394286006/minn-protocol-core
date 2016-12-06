package p.minn.packet.amf;

import java.util.ArrayList;
import java.util.List;

import p.minn.packet.Packet;

/**
 * @author minn
 * @QQ:394286006
 * 
 */
public final class RtmpPacket extends Packet<MessageType> {
  
  public int chunkStreamId = 0x03;
  
  public int timeStamp = 0;
  
  public int messageStreamId = 0;

  public RtmpPacket() {
    super();
  }
  
  public RtmpPacket(int clientId) {
    super(clientId);
  }
  
  public String toString(){
    String message = new String(this.getBody());
    return message;
  }

  public static RtmpPacket clonePacket ( RtmpPacket packetX )
  {
      
      RtmpPacket packet = new RtmpPacket( );

      packet.chunkStreamId = packetX.chunkStreamId;
      packet.body = packetX.body.clone( );
      
      packet.bodyType = packetX.bodyType;
      packet.bodySize = packetX.bodySize;
      
      packet.timeStamp = packetX.timeStamp;
      packet.messageStreamId = packetX.messageStreamId;
      
      return packet;
      
  }
  
  public List<DataFrame> getFrames() {
    // TODO Auto-generated method stub
    List<DataFrame> frames=new ArrayList<DataFrame>();
    DataFrame df=null;
    byte[] header=null;
    int headerFlag=0;
    byte[] dst;
    if(this.bodySize <=DataFrame.FRAME_LENGTH_128){
      header=new byte[ChunkType.Type0.headerSize];
      headerFlag=ChunkType.Type0.headerFlag;
      header[0 ] = ( byte )( headerFlag |chunkStreamId & 0x3F );
      header[1 ] = ( byte )( timeStamp >> 16 );
      header[2 ] = ( byte )( timeStamp >> 8 );
      header[3 ] = ( byte )( timeStamp );
      header[4 ] = ( byte )( bodySize >> 16 );
      header[5 ] = ( byte )( bodySize >> 8 );
      header[6 ] = ( byte )( bodySize );
      header[7 ] =  bodyType.type ;
      header[8 ] = ( byte )( messageStreamId >> 24 );
      header[9 ] = ( byte )( messageStreamId >> 16 );
      header[10 ]= ( byte )( messageStreamId >> 8 );
      header[11 ]= ( byte )( messageStreamId );
      df=new DataFrame(header,this.body);
      frames.add(df);
    }else{
      int mod=this.bodySize%DataFrame.FRAME_LENGTH_128;
      int num=this.bodySize/DataFrame.FRAME_LENGTH_128;
      
      for(int i=0;i<num;i++){
          dst=new byte[DataFrame.FRAME_LENGTH_128];
          System.arraycopy(this.body, i*DataFrame.FRAME_LENGTH_128, dst, 0, DataFrame.FRAME_LENGTH_128);
          if(i==0){
            header=new byte[ChunkType.Type0.headerSize];
            headerFlag=ChunkType.Type0.headerFlag;
            header[0 ] = ( byte )( headerFlag |chunkStreamId & 0x3F );
            header[1 ] = ( byte )( timeStamp >> 16 );
            header[2 ] = ( byte )( timeStamp >> 8 );
            header[3 ] = ( byte )( timeStamp );
            header[4 ] = ( byte )( bodySize >> 16 );
            header[5 ] = ( byte )( bodySize >> 8 );
            header[6 ] = ( byte )( bodySize );
            header[7 ] =  bodyType.type;
            header[8 ] = ( byte )( messageStreamId >> 24 );
            header[9 ] = ( byte )( messageStreamId >> 16 );
            header[10 ]= ( byte )( messageStreamId >> 8 );
            header[11 ]= ( byte )( messageStreamId );
            df=new DataFrame(header,dst);
          }else{
            header=new byte[ChunkType.Type3.headerSize];
            headerFlag=ChunkType.Type3.headerFlag;
            header[0] = ( byte )( headerFlag |chunkStreamId & 0x3F );
            df=new DataFrame(header,dst);
          }
          frames.add(df);
      }
      if(mod!=0){
        dst=new byte[this.bodySize-num*DataFrame.FRAME_LENGTH_128];
        System.arraycopy(this.body, num*DataFrame.FRAME_LENGTH_128, dst, 0, dst.length);
        header=new byte[ChunkType.Type3.headerSize];
        headerFlag=ChunkType.Type3.headerFlag;
        header[0] = ( byte )( headerFlag |chunkStreamId & 0x3F );
        df=new DataFrame(header,dst);
        frames.add(df);
      }
     
    }

    return frames;
  }

  
}
