package p.minn.packet.amf;

import java.util.HashMap;
import java.util.Map;

import p.minn.packet.amf.TypeValue;

public class RtmpMessageFactory {

  public static RtmpPacket invokeMessage(String callId, double invokeChannel, int timeStamp,
      int chunkStreamId, int messageStreamId, MessageType bodyType, Map<String, TypeValue> args) {

    RtmpPacket packet = new RtmpPacket();
    AmfEncoder encoder = new AmfEncoder();
    RtmpWrapper wrapper=new RtmpWrapper(callId,invokeChannel);
    Map<String,TypeValue> m=new HashMap<String,TypeValue>();
    m.put("capabilities", new TypeValue(3.0));
    m.put("mode", new TypeValue(1.0));
    wrapper.setArgs(m);
    wrapper.getInfo().add(new TypeValue(args));

    packet.body = encoder.encodeInvoke(wrapper);
    packet.timeStamp = timeStamp;
    packet.chunkStreamId = chunkStreamId;
    packet.messageStreamId = messageStreamId;
    packet.bodyType = bodyType;
    packet.bodySize = packet.body.length;

    return packet;

  }
  
  public static RtmpPacket resultMessage(double invokeChannel, int timeStamp,
      int chunkStreamId, int messageStreamId, Map<String, TypeValue> args) {

    RtmpPacket packet = new RtmpPacket();
    AmfEncoder encoder = new AmfEncoder();
    RtmpWrapper wrapper=new RtmpWrapper("_result",invokeChannel);
    wrapper.getInfo().add(new TypeValue(args));

    packet.body = encoder.encodeInvoke(wrapper);
    packet.timeStamp = timeStamp;
    packet.chunkStreamId = chunkStreamId;
    packet.messageStreamId = messageStreamId;
    packet.bodyType =MessageType.Command;
    packet.bodySize = packet.body.length;

    return packet;

  }
  
  public static RtmpPacket statusMessage(double invokeChannel, int timeStamp,
      int chunkStreamId, int messageStreamId, Map<String, TypeValue> args) {

    RtmpPacket packet = new RtmpPacket();
    AmfEncoder encoder = new AmfEncoder();
    RtmpWrapper wrapper=new RtmpWrapper("onStatus",invokeChannel);
    wrapper.getInfo().add(new TypeValue(args));

    packet.body = encoder.encodeInvoke(wrapper);
    packet.timeStamp = timeStamp;
    packet.chunkStreamId = chunkStreamId;
    packet.messageStreamId = messageStreamId;
    packet.bodyType = MessageType.Command;
    packet.bodySize = packet.body.length;

    return packet;

  }
  
  public static RtmpPacket errorMessage(double invokeChannel, int timeStamp,
      int chunkStreamId, int messageStreamId, Map<String, TypeValue> args) {

    RtmpPacket packet = new RtmpPacket();
    AmfEncoder encoder = new AmfEncoder();
    RtmpWrapper wrapper=new RtmpWrapper("_error",invokeChannel);
    wrapper.getInfo().add(new TypeValue(args));

    packet.body = encoder.encodeInvoke(wrapper);
    packet.timeStamp = timeStamp;
    packet.chunkStreamId = chunkStreamId;
    packet.messageStreamId = messageStreamId;
    packet.bodyType = MessageType.Command;
    packet.bodySize = packet.body.length;

    return packet;

  }

  public static Map<String, TypeValue> infoMessage(long clientId, String level,
      String code, String uuid, String description) {

    HashMap<String, TypeValue> map = new HashMap<String, TypeValue>();
    map.put("code", new TypeValue(code));
    map.put("level", new TypeValue(level));
    map.put("clientId", new TypeValue(clientId));
    map.put("uuid", new TypeValue(uuid));
    map.put("description", new TypeValue(description));
    return map;

  }


  public static RtmpPacket pingMessage(int p1, int p2, int p3, int p4) {

    int length = 6;
    RtmpPacket packet;
    byte[] pingbytes;

    packet = new RtmpPacket();
    pingbytes = new byte[14];

    pingbytes[0] = (byte) (p1 >> 8);
    pingbytes[1] = (byte) (p1);
    pingbytes[2] = (byte) (p2 >> 24);
    pingbytes[3] = (byte) (p2 >> 16);
    pingbytes[4] = (byte) (p2 >> 8);
    pingbytes[5] = (byte) (p2);

    if (p3 != -1) {

      pingbytes[6] = (byte) (p3 >> 24);
      pingbytes[7] = (byte) (p3 >> 16);
      pingbytes[8] = (byte) (p3 >> 8);
      pingbytes[9] = (byte) (p3);

      length = 10;

    }

    if (p4 != -1) {

      pingbytes[10] = (byte) (p4 >> 24);
      pingbytes[11] = (byte) (p4 >> 16);
      pingbytes[12] = (byte) (p4 >> 8);
      pingbytes[13] = (byte) (p4);

      length = 14;

    }

    byte[] raw = new byte[length];
    System.arraycopy(pingbytes, 0, raw, 0, length);

    packet.body = raw;
    packet.bodyType = MessageType.Ping;
    packet.bodySize = packet.body.length;
    packet.chunkStreamId = 0x02;
    packet.messageStreamId = 0x00;
    return packet;

  }

  public RtmpPacket bandInMessage(int band) {

    RtmpPacket packet;

    byte[] pingbytes = new byte[4];
    pingbytes[0] = (byte) (band >> 24);
    pingbytes[1] = (byte) (band >> 16);
    pingbytes[2] = (byte) (band >> 8);
    pingbytes[3] = (byte) (band);

    packet = new RtmpPacket();
    packet.body = pingbytes;
    packet.bodyType = MessageType.ServerBandWidth;
    packet.bodySize = packet.body.length;
    packet.chunkStreamId = 0x02;
    return packet;

  }


  public RtmpPacket bandOutMessage(int band) {

    RtmpPacket packet;
    byte[] pingbytes;

    packet = new RtmpPacket();
    pingbytes = new byte[5];

    pingbytes[0] = (byte) (band >> 24);
    pingbytes[1] = (byte) (band >> 16);
    pingbytes[2] = (byte) (band >> 8);
    pingbytes[3] = (byte) (band);
    pingbytes[4] = (byte) (0x02);

    packet.body = pingbytes;
    packet.bodyType = MessageType.ClientBandWidth;
    packet.bodySize = packet.body.length;
    packet.chunkStreamId = 0x02;
    return packet;

  }

  public static RtmpPacket readMessage(int bytesX) {

    RtmpPacket packet;

    byte[] pingbytes = new byte[4];
    pingbytes[0] = (byte) (bytesX >> 24);
    pingbytes[1] = (byte) (bytesX >> 16);
    pingbytes[2] = (byte) (bytesX >> 8);
    pingbytes[3] = (byte) (bytesX);

    packet = new RtmpPacket();
    packet.body = pingbytes;
    packet.bodyType = MessageType.Read;
    packet.bodySize = packet.body.length;
    packet.chunkStreamId = 0x02;
    return packet;

  }

}
