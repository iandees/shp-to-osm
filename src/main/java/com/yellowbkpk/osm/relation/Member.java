package com.yellowbkpk.osm.relation;


import com.yellowbkpk.osm.primitive.Primitive;

/**
 * @author Ian Dees
 *
 */
public class Member {
    
    private Primitive member;
    private String role;
    
    public Member(Primitive member, String role) {
        this.member = member;
        this.role = role;
    }

    public Primitive getMember() {
        return member;
    }

    public String getRole() {
        return role;
    }

}
