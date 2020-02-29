package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class HistoryStockDaoImpl implements HistoryStockDao {
    @PersistenceContext
    EntityManager entityManager;



    @Transactional
    @Modifying
    public void insertList(List<HistoryDataT> historyDataList){
        Session session = entityManager.unwrap(Session.class);
        try{
            //Transaction transaction = session.getTransaction();
            int count = 0;
            for (HistoryDataT historyData : historyDataList){
                session.save(historyData);
                count++;
                if (count % 200 == 0){
                    session.flush();
                    session.clear();
                }
            }
            //transaction.commit();
        }finally {
            session.close();
        }

    }
}
