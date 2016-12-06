package p.minn.packet.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import p.minn.common.utils.UtilCommon;
import p.minn.packet.Packet;

/**
 * @author minn
 * @QQ:394286006
 * 
 */
public final class JsonPacket extends Packet<MessageType> {

  
  public JsonPacket() {
    super();
  }
  
  public JsonPacket(int clientId) {
    super(clientId);
  }
  
  public String toString(){
    String message = new String(this.getBody());
    return message;
  }

  public void setBody(JsonWrapper wrapper){
      String str =UtilCommon.gson2Str(wrapper);
      byte[] msg=str.getBytes();
      setBody(msg);
  }
  public void setBody(Map<String,Object> wrapper){
    String str =UtilCommon.gson2Str(wrapper);
    byte[] msg=str.getBytes();
    setBody(msg);
}

  public List<DataFrame> getFrames() {
    // TODO Auto-generated method stub
    List<DataFrame> frames=new ArrayList<DataFrame>();
    DataFrame df=null;
    byte[] dst;
    if(this.bodySize <=DataFrame.FRAME_LENGTH){
      df=new DataFrame(FrameType.SINGLE_FRAME_UNMASKED.type ,this.body);
      frames.add(df);
    }else{
      int mod=this.bodySize%DataFrame.FRAME_LENGTH;
      int num=this.bodySize/DataFrame.FRAME_LENGTH;
      
      for(int i=0;i<num;i++){
          dst=new byte[DataFrame.FRAME_LENGTH];
          System.arraycopy(this.body, i*DataFrame.FRAME_LENGTH, dst, 0, DataFrame.FRAME_LENGTH);
          if(i==0){
            df=new DataFrame(FrameType.FIRST_FRAME_UNMASKED.type,dst);
          }else{
            df=new DataFrame(FrameType.CONTINUE_FRAME_UNMASKED.type,dst);
          }
          frames.add(df);
      }
      if(mod!=0){
        dst=new byte[this.bodySize-num*DataFrame.FRAME_LENGTH];
        System.arraycopy(this.body, num*DataFrame.FRAME_LENGTH, dst, 0, dst.length);
        df=new DataFrame(FrameType.END_FRAME_UNMASKED.type,dst);
        frames.add(df);
      }
     
    }

    return frames;
  }

  
}
