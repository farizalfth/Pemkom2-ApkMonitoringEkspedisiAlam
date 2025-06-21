package com.simeks.server;

import java.sql.SQLException;
import java.util.List;

public interface Repository<T, ID> {
    void save(T entity) throws SQLException;
    List<T> findAll() throws SQLException;
}