package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;
public class TerminatedBroadcast implements Broadcast {
    String senderName;
    

    public TerminatedBroadcast(String senderName) {
       this.senderName = senderName;
    }
}
