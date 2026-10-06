package com.studentmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.FileAsset;

@Repository 
public interface FileAssetRepository extends JpaRepository<FileAsset,Long> {

    
} 
