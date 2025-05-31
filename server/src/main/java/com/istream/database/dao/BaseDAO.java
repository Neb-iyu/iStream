package com.istream.database.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface BaseDAO {
    Connection getConnection() throws SQLException;
    void closeConnection(Connection conn);
} 