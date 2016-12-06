package p.minn.packet.amf;

import java.util.List;
import java.util.Map;

public class TypeValue  {
 
  public AmfType type;
  public Object value;

  public int intValue=-1;
  public long longValue=-1;
  public double doubleValue=-1;
  public String stringValue;
  public boolean booleanValue;
  public List < TypeValue > listValue;
  public Map < String , TypeValue > hashValue;
  
  
  @Override
  public String toString() {
      

      String str="type:"+type;
      if(value!=null)str+=",object:"+value;
      if(stringValue!=null)str+=",stringValue:"+stringValue;
      if(listValue!=null)str+=",listValue:"+listValue;
      if(hashValue!=null)str+=",hashValue:"+hashValue;
      if(intValue!=-1)str+=",intValue:"+intValue;
      if(longValue!=-1)str+=",longValue:"+longValue;
      if(doubleValue!=-1)str+=",doubleValue:"+doubleValue;
      if(booleanValue)str+=",booleanValue:"+booleanValue;
      return str;
  }
  public TypeValue ( )
  {
  
      type =AmfType.Null;
      value = null;
      
  }   
  
      
  public TypeValue ( int value )
  {
      
      type = AmfType.Integer;
      this.value = value;
      intValue = value;
      
  }
  
  
  public TypeValue ( long value )
  {
      
      type = AmfType.Long;
      this.value = value;
      longValue = value;

  }
  
  
  public TypeValue ( double value )
  {
      
      type = AmfType.Number;
      this.value = value;
      doubleValue = value;

  }
  
  
  public TypeValue ( String value )
  {

      type = AmfType.String;
      this.value = value;
      stringValue = value;

  }
  
  
  public TypeValue ( boolean value )
  {
      
      type = AmfType.Boolean;
      this.value = value;
      booleanValue = value;

  }
  
  
  public TypeValue ( List < TypeValue > value )
  {
      type = AmfType.Array;
      this.value = value;
      listValue = value;

  }
  
  
  public TypeValue ( Map < String , TypeValue > value )
  {
      if(value==null){
        nullValue();
      }else{
        type = AmfType.Map;
        this.value = value;
        hashValue = value;
      }
  }
  
  private void  nullValue(){
    type = AmfType.Null;
    value = null;
  }

}
