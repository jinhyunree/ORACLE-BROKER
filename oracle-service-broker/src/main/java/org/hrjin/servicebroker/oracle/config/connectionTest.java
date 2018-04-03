package org.hrjin.servicebroker.oracle.config;

/**
 * Created by hrjin on 2018-04-02.
 */

import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by hrjin on 2018-04-02.
 */
public class connectionTest {
    public static void main(String[] args){

        String DB_URL = "jdbc:oracle:thin:@211.104.171.226:59161:xe";
        String DB_USER = "system";
        String DB_PASSWORD = "oracle";

        Connection conn = null;

        try {
            // 1. 드라이버 로딩
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("드라이버 로딩 성공");

            // forName의 인자로 전달된 주소에 드라이버가 없을 경우
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로딩 실패");
        }

        try {
            // 오라클DB에 연결
            conn = DriverManager.getConnection(
                    DB_URL, DB_USER , DB_PASSWORD);
            System.out.println("커넥션 성공");

            // 실제 사용 코드

            // 커넥션은 반드시 닫아주어야 한다.
            conn.close();
            System.out.println("커넥션 종료");
            // 오라클 DB에 연결이 실패하였을때
        } catch (SQLException e) {
            System.out.println("커넥션 실패");
        }
    }
}
