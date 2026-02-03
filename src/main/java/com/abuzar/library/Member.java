package com.abuzar.library;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

public class Member {
    private int id;
    private String name;
    private String email;
    private String phone;
    private Timestamp memberSince;  
    private String membershipStatus;  


    public Member() {}

     
    public Member(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.membershipStatus = "active";  
    }
 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Timestamp getMemberSince() { return memberSince; }
    public void setMemberSince(Timestamp memberSince) { this.memberSince = memberSince; }
 
    public LocalDate getMemberSinceDate() {
        return memberSince != null ? memberSince.toLocalDateTime().toLocalDate() : null;
    }

    public String getMembershipStatus() { return membershipStatus; }
    public void setMembershipStatus(String membershipStatus) { 
        this.membershipStatus = membershipStatus; 
    }

    // Helper: Is this member allowed to borrow?
    public boolean isActive() {
        return "active".equalsIgnoreCase(membershipStatus);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status='" + membershipStatus + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id == member.id && email.equals(member.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}