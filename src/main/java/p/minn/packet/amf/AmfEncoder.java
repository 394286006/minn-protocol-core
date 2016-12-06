package p.minn.packet.amf;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;


public class AmfEncoder 
{
	
	
	public byte [ ] encodeInvoke ( RtmpWrapper wrapper )
	{
		
		byte [ ] part;
		byte [ ] result;
		byte [ ] newResult;
		
		result = new byte[0];
		
		part = encode(new TypeValue( wrapper.getName()) );           
        newResult = new byte [ result.length + part.length ];
        
        System.arraycopy( result , 0 , newResult , 0 , result.length );
        System.arraycopy( part , 0 , newResult , result.length , part.length );
        
        result = newResult;
		
        part = encode(new TypeValue( wrapper.getId()) );           
        newResult = new byte [ result.length + part.length ];
        
        System.arraycopy( result , 0 , newResult , 0 , result.length );
        System.arraycopy( part , 0 , newResult , result.length , part.length );
        
        result = newResult;
        
        part = encode(new TypeValue(wrapper.getArgs()) );           
        newResult = new byte [ result.length + part.length ];
        
        System.arraycopy( result , 0 , newResult , 0 , result.length );
        System.arraycopy( part , 0 , newResult , result.length , part.length );
        
        result = newResult;
		
		List<TypeValue> list=wrapper.getInfo();
		
		for ( int i = 0 ; i < list.size() ; i++ )
		{
			
			part = encode( list.get( i ) );			
			newResult = new byte [ result.length + part.length ];
			
			System.arraycopy( result , 0 , newResult , 0 , result.length );
			System.arraycopy( part , 0 , newResult , result.length , part.length );
			
			result = newResult;
			
		}
		
		return result;
		
	}
	
	
	public static byte [ ] encode ( TypeValue typeValue )
	{
		
		byte [ ] result = new byte [ ] { 0x05 };
		
		if ( typeValue.type.equals(AmfType.Integer ) ) return encodeDouble( typeValue );
		else
		if ( typeValue.type.equals(AmfType.Long ) ) return encodeDouble( typeValue );
		else
		if ( typeValue.type.equals( AmfType.Number ) ) return encodeDouble( typeValue );
		else
		if ( typeValue.type.equals(AmfType.String ) ) return encodeString( typeValue );
		else
		if ( typeValue.type.equals(AmfType.Boolean) ) return encodeBoolean( typeValue );
		else
		if ( typeValue.type.equals( AmfType.Map ) ) return encodeHashMap( typeValue );
		else
		if ( typeValue.type.equals( AmfType.Array ) ) return encodeArrayList( typeValue );
		else
		if ( typeValue.type.equals( AmfType.Null ) ) return result;
		else 
		return result;
		
	}	
	
	
	public static byte [ ] encodeDouble ( TypeValue typeValue )
	{
		
		Double number;
		ByteBuffer buffer;
		
		if ( typeValue.type == AmfType.Integer ) typeValue.doubleValue = ( double ) typeValue.intValue;
		if ( typeValue.type == AmfType.Long) typeValue.doubleValue = ( double ) typeValue.longValue;
		
		number = typeValue.doubleValue;
		
		buffer = ByteBuffer.allocate( 9 );
		buffer.put( ( byte ) AmfType.Number.type );
		buffer.putDouble( number );
		
		return buffer.array( );
		
	}
	
	
	public static byte [ ] encodeString ( TypeValue typeValue )
	{

		String string;
		ByteBuffer buffer;
		
		string = typeValue.stringValue;
		Charset cs=Charset.forName("utf-8");
		ByteBuffer bb=cs.encode(string);
		
		buffer = ByteBuffer.allocate( bb.limit() + 3 );
		buffer.put( ( byte ) AmfType.String.type );
		buffer.put( ( byte ) ( bb.limit() >> 8 ) );
		buffer.put( ( byte ) bb.limit() );
		
		buffer.put(bb);
		return buffer.array( );
		
		
	}
	
	public static byte [ ] encodeBoolean ( TypeValue typeValue )
	{

		Boolean booleen;
		ByteBuffer buffer;
		
		booleen = typeValue.booleanValue;
		buffer = ByteBuffer.allocate( 2 );
		buffer.put( ( byte ) AmfType.Boolean.type );
		
		if ( booleen ) buffer.put( ( byte ) 0x01 );
		else buffer.put( ( byte ) 0x00 );
		
		return buffer.array( );
		
	}
	
	public static byte [ ] encodeHashMap ( TypeValue hashMap )
	{
		
		byte [ ] result;
		byte [ ] ending;
		
		byte [ ] rawKey;
		byte [ ] rawValue;
		byte [ ] newResult;
		
		TypeValue value;
		Map < String , TypeValue > map = hashMap.hashValue;
		
		result = new byte[1 ];
		result[0 ] = ( byte ) AmfType.Map.type;
		
		for ( String key : map.keySet( ) )
		{
			
			value = map.get( key );

			rawKey = encode( new TypeValue( key ) );			
			rawValue = encode( value );
			newResult = new byte[result.length + rawKey.length + rawValue.length - 1 ];
			
			System.arraycopy( result , 0 , newResult , 0 , result. length );
			System.arraycopy( rawKey , 1 , newResult , result.length , rawKey.length - 1 );
			System.arraycopy( rawValue , 0 , newResult , result.length + rawKey.length - 1 , rawValue.length );
			
			result = newResult;
			
		}
		
		ending = new byte[3 ];
		ending[2] = 0x09;
		
		newResult = new byte[result.length + 3 ];
		System.arraycopy( result , 0 , newResult , 0 , result. length );
		System.arraycopy( ending , 0 , newResult , result.length , 3 );
		
		return newResult;
		
	}
	
	
	public static byte [ ] encodeArrayList ( TypeValue typeValue )
	{
		
		byte [ ] result;
		byte [ ] rawValue;
		byte [ ] newResult;
		List < TypeValue > list = typeValue.listValue;
		
		result = new byte[5 ];
		result[0 ] = ( byte ) AmfType.Array.type;
		result[1 ] = ( byte ) (list.size() << 24);
		result[2 ] = ( byte ) (list.size() << 16);
		result[3 ] = ( byte ) (list.size() << 8);
		result[4 ] = ( byte ) (list.size());
		
		for ( TypeValue tv : list )
		{
			
			rawValue = encode( tv );
			newResult = new byte[result.length + rawValue.length ];

			System.arraycopy( result , 0 , newResult , 0 , result. length );
			System.arraycopy( rawValue , 0 , newResult , result.length , rawValue.length );
			
			result = newResult;

		}
		
		return result;
		
	}
	

    public static byte [ ] encode ( List<TypeValue> list )
    {

        ByteArrayOutputStream result = new ByteArrayOutputStream( );
        
        try
        {

            for ( TypeValue typeValue : list )
            {
                
                byte [ ] byteValue = AmfEncoder.encode( typeValue );
                result.write( byteValue );
                
            }

        }
        catch ( IOException exception ) 
        { 
            
            System.out.println("encode exception:"+ exception.getMessage()); 
            exception.printStackTrace( ); 
            
        }
        
        return result.toByteArray( );
        
    }
    
    
    public static byte [ ] concatenate ( byte [ ] ... arraysX )
    {
    

        ByteArrayOutputStream result = new ByteArrayOutputStream( );
        
        try
        {

            for ( byte [ ] array : arraysX ) result.write( array );
            
        }
        catch ( IOException exception ) { exception.printStackTrace( ); }
        
        return result.toByteArray( );
        
    }
    
    public static int bytesToInt ( byte [ ] bytesX )
    {
        
        int result = 0;
        int actual = 0;
        int offset = bytesX.length * 8;
        
        do
        {
        
            offset -= 8;
            result += ( bytesX[ actual ] & 0xFF ) << offset;
            actual ++;
            
        }
        while ( offset > 0 );
        
        return result;
        
    }
    
    public static byte [ ] intToBytes ( long valueX , int lengthX )
    {
        
        byte [ ] result = new byte [ lengthX ];
        int offset = lengthX * 8;
        int actual = 0;
        
        do
        {
            
            offset -= 8;
            result[ actual ] = ( byte ) ( valueX >> offset );
            actual ++ ;
            
        }
        while ( offset > 0 );
        
        return result;
        
    }
    
    public static String getHexa ( byte [ ] bytesX )
    {
        

        int counter = 1;
        String result = " ";
        
        for ( byte actual : bytesX )
        {
            
            result += getHexaBits( ( actual & 0xF0 ) >> 4 ) + getHexaBits( actual & 0x0F ) + " ";
            if ( counter % 8 == 0 ) result += "  ";
            if ( counter % 16 == 0 ) result += "\n";
            ++counter;
            
        }
        
        return result;

    }
    
    public static String getHexaBits ( int valueX )
    {

        switch ( valueX )
        {
        
            case 15 : return "F";
            case 14 : return "E";
            case 13 : return "D";
            case 12 : return "C";
            case 11 : return "B";
            case 10 : return "A";
            default : return "" + valueX;
        
        }
        
    }


}
