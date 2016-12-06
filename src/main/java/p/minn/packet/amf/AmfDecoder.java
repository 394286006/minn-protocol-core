package p.minn.packet.amf;



import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;


public class AmfDecoder 
{
	
	
	public AmfDecoder (   )
	{
		
	}
	
	public static RtmpWrapper decodeInvoke ( byte [ ] bytesX ) throws Exception
	{
		
	    TypeValue item;
		ByteBuffer buffer = ByteBuffer.wrap( bytesX );
		List < TypeValue > infoList =new ArrayList < TypeValue > ( );
		RtmpWrapper rtmpWrapper=new RtmpWrapper();
		int numidx=0;
		while ( buffer.hasRemaining( ) )
		{
			try
			{
				item = decode( buffer );
				switch(numidx){
				  case 0:
				    rtmpWrapper.setName(item.stringValue);
				    break;
				  case 1:
				    rtmpWrapper.setId(item.doubleValue);
				    break;
				  case 2:
				    rtmpWrapper.setArgs(item.hashValue);
				  break;
				  default:
				    infoList.add(item);
				}
				
				++numidx;
			}
			catch ( Exception exception ) { throw( exception ); }			
								
			rtmpWrapper.setInfo(infoList);
			
		}
		
		return rtmpWrapper;
		
	}
	
	public static TypeValue decode ( ByteBuffer bufferX ) throws Exception
	{
		
	  AmfType type =AmfType.valueOf(bufferX.get( ));
		switch ( type )
		{
			case Number : return decodeDouble( bufferX );
			case Boolean : return decodeBoolean( bufferX ); 
			case String : return decodeString( bufferX ); 
			case Map : return decodeObject( bufferX ); 
			case Null : return new TypeValue( ); 
			case Undefined : return new TypeValue( ); 
			case MixedArray : return decodeMixedArray( bufferX );
			case Array : return decodeArray( bufferX );
			default   :	throw( new Exception( "Unknown AMF data type" ) );
					
		
		}
		
	}
	
	public static TypeValue decodeDouble ( ByteBuffer bufferX )
	{
		
		double result = bufferX.getDouble( );
		return new TypeValue( result );
		
	}
	
	
	public static TypeValue decodeBoolean ( ByteBuffer bufferX )
	{
		
		boolean result = ( bufferX.get() & 0xFF ) == 0 ? false : true;
		return new TypeValue( result );
		
	}
	
	
	public static TypeValue decodeString ( ByteBuffer bufferX )
	{
		int size = bufferX.get( ) << 8 | bufferX.get( );
		Charset cs=Charset.forName("utf-8");
		byte[] dst=new byte[size];
		bufferX.get(dst);
		String result= new String( cs.decode(ByteBuffer.wrap(dst)).array());
		return new TypeValue( result );
	}
	
	
	public static TypeValue decodeObject ( ByteBuffer bufferX )
	{
		
		int endInt;
		String key;
		TypeValue value;
		
		HashMap < String , TypeValue > result = new HashMap < String , TypeValue >( );
		
		while ( bufferX.hasRemaining( ) )
		{
			
			endInt = bufferX.get( ) << 16 | bufferX.get( ) << 8 | bufferX.get( );
			
			if ( endInt != 9 )
			{
				
				bufferX.position( bufferX.position( ) - 3 );
				
				try
				{
					
					key = decodeString( bufferX ).stringValue;
					value = decode( bufferX );
					
					result.put( key , value );

				}
				catch (Exception exception )
				{
					
					System.out.println( "AMFDecoder.decodeObject " + exception.getMessage() );
					bufferX.position( bufferX.capacity( ) );
					
				}
			
			} else break;
									
		}
		
		return new TypeValue( result );
		
	}

	
	public static TypeValue decodeArray ( ByteBuffer bufferX )
	{

		int count = 0;
		int size = bufferX.get( ) << 24 | bufferX.get() << 16 | bufferX.get( ) << 8 | bufferX.get( );
		ArrayList < TypeValue > result = new ArrayList < TypeValue > ( );
				
		while ( bufferX.hasRemaining( ) && count < size )
		{
			
			try
			{
				
			  TypeValue typeValue = decode( bufferX );
				result.add( typeValue );
				count++;
				
			}
			catch ( Exception exception )
			{

				System.out.println( "AMFDecoder.decodeObject " + exception.getMessage() );
				bufferX.position( bufferX.capacity( ) );
				
			}
									
		}
		
		return new TypeValue( result );
		
	}
	

	
	public static TypeValue decodeMixedArray ( ByteBuffer bufferX )
	{
		
	  TypeValue value;
		int endInt;
		ArrayList < TypeValue > result = new ArrayList < TypeValue > ( );
		while ( bufferX.hasRemaining( ) )
		{
			
			endInt = bufferX.get( ) << 16 | bufferX.get( ) << 8 | bufferX.get( );
			
			if ( endInt != 9 )
			{
				
				bufferX.position( bufferX.position( ) - 3 );

				try
				{
					value = decode( bufferX );
					result.add( value );

				}
				catch (Exception exception )
				{
					
					System.out.println( "AMFDecoder.decodeObject " + exception.getMessage() );
					bufferX.position( bufferX.capacity( ) );
					
				}
			
			} else break;
									
		}
		
		result.remove( 0 );
		return new TypeValue( result );
		
	}
	
}
