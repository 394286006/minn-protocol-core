package p.minn.packet.amf;



import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import p.minn.listener.ClientEventListener;
import p.minn.packet.Decoder;


public class RtmpDecoder extends Decoder<RtmpPacket,RtmpWrapper>
{
  
  public RtmpPacket [ ] packets;
  public int [ ] chunkSizes;
  public int [ ] remainings;

	public RtmpDecoder ( ClientEventListener<RtmpPacket,RtmpWrapper> evt,SocketChannel socket )
	{
		super(evt,socket);
	    packets = new RtmpPacket [64 ];
	    chunkSizes = new int[64 ];
	    remainings = new int[64 ];
	}
	

	
	protected void read(ByteBuffer buffer) throws Exception 
	{
	  byte[] data=buffer.array();
      int max = buffer.limit();
      int start = 0;      
      boolean hasPacket = true;
      do
      {
          //if(max-start>0){
              int header = data[start ];
              
              ChunkType chunkType =ChunkType.valueOf(header & 0xC0);
              int headerSize=chunkType.headerSize;
              int chunkStreamId = header & 0x3F;
              
              if ( packets[chunkStreamId ] == null ) 
              {
                  packets[chunkStreamId ] = new RtmpPacket( );
              }
              
              RtmpPacket packet = packets[chunkStreamId ];
              
              if ( max - start >= headerSize ) 
              { 
                  
                  if ( headerSize >  ChunkType.Type3.headerSize )
                  {
                      
                      packet.timeStamp = ( data[start + 1 ] & 0xFF ) << 16 | 
                                        ( data[start + 2 ] & 0xFF ) << 8 | 
                                        ( data[start + 3 ] & 0xFF );
                      
                      if ( headerSize >  ChunkType.Type2.headerSize )
                      {
                          
                          packet.bodySize = ( data[start + 4 ] & 0xFF ) << 16 | 
                                            ( data[start + 5 ] & 0xFF ) << 8 | 
                                            ( data[start + 6 ] & 0xFF );
                          
                          packet.bodyType =MessageType.valueOf(data[start + 7 ] & 0xFF);
                          
                          packet.body = new byte[packet.bodySize ];
                          
                          chunkSizes[chunkStreamId ] = packet.bodyType.type ==  MessageType.Audit.type ?  DataFrame.FRAME_LENGTH_65 :  DataFrame.FRAME_LENGTH_128;
                          remainings[chunkStreamId ] = packet.bodySize;
                  
                          if ( headerSize >  ChunkType.Type1.headerSize )
                          {
                              packet.messageStreamId = ( data[start + 8 ] & 0xFF ) << 24 | 
                                                  ( data[start + 9 ] & 0xFF ) << 16 | 
                                                  ( data[start + 10 ] & 0xFF ) << 8 | 
                                                  ( data[start + 11 ] & 0xFF );
                          }
                          
                      }
                      
                  }
                  
                  int remaining = remainings[chunkStreamId ];
                  int chunkSize = chunkSizes[chunkStreamId ];
                 

                                      
                  if ( chunkSize > remaining ) chunkSize = remaining;
                  
                  if ( ( max - start - headerSize ) >= chunkSize)
                  {
                      System.arraycopy( data , 
                                        start + headerSize , 
                                        packet.body , 
                                        packet.bodySize - remaining , 
                                        chunkSize );

                      remainings[chunkStreamId ] -= chunkSize;
                      if ( remainings[chunkStreamId ] == 0 ) 
                      {
                          RtmpPacket newPacket = RtmpPacket.clonePacket( packet );
                          switch(packet.bodyType){
                          case Audit:evt.onAudioEvent(newPacket,null);break;
                          case Video:evt.onFlvEvent(newPacket,null);break;
                          case Ping:  
                            evt.onControllerEvent(newPacket);
                            break;
                          case Command:      
                            RtmpWrapper   arguments=AmfDecoder.decodeInvoke(newPacket.body);
                               evt.onEvent(arguments,newPacket);
                          break;
                          default:
                              evt.onErrorPacketHandler(String.valueOf(newPacket.bodyType));
                          }
                          remainings[chunkStreamId ] = packet.bodySize;
                      }
                      
                      start += headerSize + chunkSize;
                      
                  } 
                  else hasPacket = false;
              }
              else hasPacket = false;
        //  }else 
          //  hasPacket=false;
          
      } 
      while ( hasPacket );
      
	}



	
	public static RtmpPacket readData (ByteBuffer buffer ) 
    {
       
        RtmpPacket packet=new RtmpPacket();
        int remaining=0  ;
        int chunkSize=0 ;
        buffer.flip();
        int max = buffer.limit();
        int start = 0;      
        byte[] data = buffer.array( );
        boolean hasPacket = true;
        
        do
        {
            
            if ( max - start > 0 )
            {
                
                int header = data[start ];
                ChunkType chunkType =ChunkType.valueOf(header & 0xC0);
                int headerSize =chunkType.headerSize;
                if ( max - start >= headerSize ) 
                { 
                    
                    if ( headerSize > ChunkType.Type3.headerSize  )
                    {
                        
                        packet.timeStamp = ( data[start + 1 ] & 0xFF ) << 16 | 
                                          ( data[start + 2 ] & 0xFF ) << 8 | 
                                          ( data[start + 3 ] & 0xFF );
                        
                        if ( headerSize >ChunkType.Type2.headerSize)
                        {
                            
                            packet.bodySize = ( data[start + 4 ] & 0xFF ) << 16 | 
                                              ( data[start + 5 ] & 0xFF ) << 8 | 
                                              ( data[start + 6 ] & 0xFF );
                            
                            packet.bodyType = MessageType.valueOf(data[start + 7 ] & 0xFF);
                            
                            packet.body = new byte[packet.bodySize ];
                            
                            chunkSize =DataFrame.FRAME_LENGTH_128;
                            remaining= packet.bodySize;
                    
                            if ( headerSize > ChunkType.Type1.headerSize )
                            {
                            
                                packet.messageStreamId = ( data[start + 8 ] & 0xFF ) << 24 | 
                                                    ( data[start + 9 ] & 0xFF ) << 16 | 
                                                    ( data[start + 10 ] & 0xFF ) << 8 | 
                                                    ( data[start + 11 ] & 0xFF );
                                
                            }
                            
                        }
                        
                    }
                                        
                    if ( chunkSize > remaining ) chunkSize = remaining;
                    
                    
                    if ( ( max - start - headerSize ) >= chunkSize &&chunkSize>0)
                    {
                        
                        System.arraycopy( data , 
                                          start + headerSize , 
                                          packet.body , 
                                          packet.bodySize - remaining , 
                                          chunkSize );

                        remaining-= chunkSize;
                        
                        if ( remaining == 0 ) 
                        {
                            
                            buffer.clear( );
                             return packet;
                            
                        }
                        start += headerSize + chunkSize;
                        
                    } 
                    else hasPacket = false;
                    
                }
                else hasPacket = false;
                
            }
            else hasPacket = false;
            
        } 
        while ( hasPacket );
        
        return null;
    }

}
