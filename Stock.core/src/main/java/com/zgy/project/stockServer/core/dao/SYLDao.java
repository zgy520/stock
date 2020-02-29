package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.detail.SYL.SYL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SYLDao extends JpaRepository<SYL,Long> {
    List<SYL> findByReportDateBetween(Date startDate,Date endDate);
}

