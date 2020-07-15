// Created by: Stefan Nikolov
// User ID: 51768275
import java.net.Socket;
import java.util.ArrayList;

// This class is used for recording both groups and topics
// Both methods require the same structure so
// I've decided to keep 1 class instead of 2
public class UserGroup {
    String name;
    private ArrayList<Socket> members = new ArrayList<Socket>();

    public UserGroup(String name) {
        this.name = name;
    }

    public void addMember(Socket member) {
        if(!this.members.contains(member)) {
            this.members.add(member);
        } 
    }

    public void removeMember(Socket member) {
        if(this.members.contains(member)) {
            this.members.remove(member);
        }
    }

    public int groupSize() {
        return this.members.size();
    }
    // Given the index, return the corresponding user
    public Socket getMember(int index) {
        return this.members.get(index);
    }
    // Return the index in the array of a given user
    public int getMemberIndex(Socket member) {
        return this.members.indexOf(member);
    }
    // Returns true if a given user (specified by socket) exists in the array
    public boolean memberInGroup(Socket member) {
        if(this.members.contains(member)) {
            return true;
        } else {
            return false;
        }
    }
    // Remove all members from the array (used when "removing" a group or topic)
    public void clearMembers() {
        try {
            this.members.clear();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}