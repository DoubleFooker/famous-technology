package com.ognice.mybatis.session;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/21
 */
@Data
@Accessors(chain = true)
public class DefaultDataSource implements DataSource {
    private String driver;
    private Driver driverInstance;
    private String url;
    private String username;
    private String password;

    private DefaultDataSource() {
    }

    public DefaultDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        initDriver();
        return DriverManager.getConnection(url, username, password);
    }

    private void initDriver() throws SQLException {
        if (this.driverInstance == null) {
            Class<?> driverType;
            try {
                driverType = Class.forName(driver);
                this.driverInstance = (Driver) driverType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new SQLException("Error setting driver on DataSource. Cause: " + e);
            }
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }


    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
