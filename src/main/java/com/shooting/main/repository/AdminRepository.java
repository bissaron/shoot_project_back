package com.shooting.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shooting.main.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{
	@Query("SELECT a FROM Admin a WHERE a.user.u_id = :userId")
    Admin findByUserId(@Param("userId") Long userId);
}
