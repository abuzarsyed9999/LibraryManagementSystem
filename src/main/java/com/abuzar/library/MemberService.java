package com.abuzar.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberService {

    public void saveMember(Member member) {
        String sql = "INSERT INTO members (name, email, phone, membership_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getMembershipStatus());
            
            if (stmt.executeUpdate() > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setId((int) generatedKeys.getLong(1));
                        member.setMemberSince(new Timestamp(generatedKeys.getTimestamp(2).getTime()));
                    }
                }
                System.out.println(" Member saved: " + member.getName());
            }
        } catch (SQLException e) {
            System.err.println("Save member failed: " + e.getMessage());
        }
    }

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Member member = new Member();
                member.setId(rs.getInt("id"));
                member.setName(rs.getString("name"));
                member.setEmail(rs.getString("email"));
                member.setPhone(rs.getString("phone"));
                member.setMemberSince(rs.getTimestamp("member_since"));
                member.setMembershipStatus(rs.getString("membership_status"));
                members.add(member);
            }
        } catch (SQLException e) {
            System.err.println(" Fetch members failed: " + e.getMessage());
        }
        return members;
    }

    public List<Member> searchMembers(String keyword) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getInt("id"));
                    member.setName(rs.getString("name"));
                    member.setEmail(rs.getString("email"));
                    member.setPhone(rs.getString("phone"));
                    member.setMemberSince(rs.getTimestamp("member_since"));
                    member.setMembershipStatus(rs.getString("membership_status"));
                    members.add(member);
                }
            }
        } catch (SQLException e) {
            System.err.println("Search members failed: " + e.getMessage());
        }
        return members;
    }

    public Member getMemberById(int id) {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getInt("id"));
                    member.setName(rs.getString("name"));
                    member.setEmail(rs.getString("email"));
                    member.setPhone(rs.getString("phone"));
                    member.setMemberSince(rs.getTimestamp("member_since"));
                    member.setMembershipStatus(rs.getString("membership_status"));
                    return member;
                }
            }
        } catch (SQLException e) {
            System.err.println("Fetch member failed: " + e.getMessage());
        }
        return null;
    }

    public void updateMember(Member member) {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ?, membership_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getMembershipStatus());
            stmt.setInt(5, member.getId());
            
            if (stmt.executeUpdate() > 0) {
                System.out.println("Member updated: " + member.getName());
            } else {
                System.out.println(" Member not found: ID=" + member.getId());
            }
        } catch (SQLException e) {
            System.err.println("Update member failed: " + e.getMessage());
        }
    }

    public boolean deleteMember(int id) {
        // Check for active borrowings first
        String checkSql = "SELECT COUNT(*) FROM borrowings WHERE member_id = ? AND return_date IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println(" Cannot delete member: They have active borrowings!");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Delete check failed: " + e.getMessage());
            return false;
        }

        // Safe to delete
        String deleteSql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            
            stmt.setInt(1, id);
            if (stmt.executeUpdate() > 0) {
                System.out.println(" Member deleted: ID=" + id);
                return true;
            } else {
                System.out.println(" Member not found: ID=" + id);
                return false;
            }
        } catch (SQLException e) {
            System.err.println(" Delete member failed: " + e.getMessage());
            return false;
        }
    }
}