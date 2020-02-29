package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.detail.ZFCode.ZFCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZFCodeDao extends JpaRepository<ZFCode,Long> {
}
