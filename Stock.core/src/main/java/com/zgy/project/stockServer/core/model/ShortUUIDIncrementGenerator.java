package com.zgy.project.stockServer.core.model;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class ShortUUIDIncrementGenerator implements IdentifierGenerator, Configurable {
    private static volatile int Guid = 100;
    @Override
    public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry) throws MappingException {

    }


    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        synchronized (this){
            return Long.valueOf(getGuid());
        }
    }


    private String getGuid(){
        Guid+=1;

        long now = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyy");
        String time = dateFormat.format(now);
        String info = now+"";
        int rand = 0;
        if (Guid>999){
            Guid =100;
        }
        rand = Guid;
        return time+info.substring(2,info.length()) + rand;
    }
}
