package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.comment.StockComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface StockCommentDao extends JpaRepository<StockComment,Long> {
    Page<StockComment> findAll(Pageable pageable);

    List<StockComment> findByIdIn(Set<Long> ids);
}
