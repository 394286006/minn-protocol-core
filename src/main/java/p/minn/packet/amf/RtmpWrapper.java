package p.minn.packet.amf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RtmpWrapper  {
 
  private String name;
  
  private double id;
  
  private Map<String,TypeValue> args;
  
  private List<TypeValue> info;

  public RtmpWrapper() {
    info=new ArrayList<TypeValue>();
  }
  
  public RtmpWrapper(String name, double id) {
    this.name = name;
    this.id = id;
    info=new ArrayList<TypeValue>();
  }
  
  public RtmpWrapper(String name, double id, Map<String, TypeValue> args) {
    this.name = name;
    this.id = id;
    this.args = args;
    info=new ArrayList<TypeValue>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getId() {
    return id;
  }

  public void setId(double id) {
    this.id = id;
  }

  public Map<String, TypeValue> getArgs() {
    return args;
  }

  public void setArgs(Map<String, TypeValue> args) {
    this.args = args;
  }

  public List<TypeValue> getInfo() {
    return info;
  }

  public void setInfo(List<TypeValue> info) {
    this.info = info;
  }

 
}
