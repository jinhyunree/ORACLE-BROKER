package org.hrjin.servicebroker.oracle.service.impl;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.hrjin.servicebroker.model.CreateServiceInstanceRequest;
import org.hrjin.servicebroker.model.ServiceInstance;
import org.hrjin.servicebroker.model.ServiceInstanceBinding;
import org.hrjin.servicebroker.oracle.exception.OracleServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hrjin on 2018-03-27.
 */
@Service
public class OracleAdminService {
    public static final String SERVICE_INSTANCES_FILDS = "service_instance_id, service_id, plan_id, organization_guid, space_guid";

    public static final String SERVICE_INSTANCES_FIND_BY_INSTANCE_ID = "select " + SERVICE_INSTANCES_FILDS + " from service_instance where service_instance_id = ?";

    public static final String TABLESPACE_FIND_BY_INSTANCE_ID = "SELECT tablespace_name, status  FROM user_tablespaces where tablespace_name = ?";

    public static final String SERVICE_INSTANCES_ADD = "insert into service_instance("+SERVICE_INSTANCES_FILDS+") values(?,?,?,?,?) ";

    public static final String SERVICE_INSTANCES_UPDATE = "update service_instance set service_instance_id = ?,  service_id = ?, plan_id = ?, organization_guid = ?, space_guid = ?";

    public static final String SERVICE_BINDING_FILDS ="binding_id, instance_id, app_id, username, password";

    public static final String SERVICE_BINDING_FIND_BY_BINDING_ID = "select " + SERVICE_BINDING_FILDS + " from service_binding where binding_id = ?";

    public static final String SERVICE_BINDING_FIND_BY_INSTANCE_ID = "select " + SERVICE_BINDING_FILDS + " from service_binding where instance_id = ?";

    public static final String SERVICE_BINDING_FIND_USERNAME_BY_BINDING_ID = "select username from service_binding where binding_id = ?";

    public static final String SERVICE_BINDING_DELETE_BY_BINDING_ID = "delete from service_binding where binding_id = ?";

    static String DATABASE_PREFIX = "paasta_";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public OracleAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private static final RowMapper<ServiceInstance> mapper = new ServiceInstanceRowMapper();

    private static final RowMapper<ServiceInstanceBinding> mapper2 = new ServiceInstanceBindingRowMapper();


    public ServiceInstance findById(String id){
        System.out.println("OracleAdminService.findById");
        ServiceInstance serviceInstance = null;
        try {
            System.out.println("쿼리를 확인해 보자!" + SERVICE_INSTANCES_FIND_BY_INSTANCE_ID + id);
            serviceInstance = jdbcTemplate.queryForObject(SERVICE_INSTANCES_FIND_BY_INSTANCE_ID, mapper, id);
            serviceInstance.withDashboardUrl(getDashboardUrl(serviceInstance.getServiceInstanceId()));
        } catch (Exception e) {
        }
        return serviceInstance;
    }

    // DashboardUrl 생성
    public String getDashboardUrl(String instanceId){

        return "http://www.sample.com/"+instanceId;
    }


    private static class ServiceInstanceRowMapper implements RowMapper<ServiceInstance> {
        @Override
        public ServiceInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
            request.withServiceInstanceId(rs.getString(1));
            request.setServiceDefinitionId(rs.getString(2));
            request.setPlanId(rs.getString(3));
            request.setOrganizationGuid(rs.getString(4));
            request.setSpaceGuid(rs.getString(5));
            return new ServiceInstance(request);
        }
    }

    /*private static class ServiceInstanceBindingRowMapper implements RowMapper<ServiceInstanceBinding> {
        @Override
        public ServiceInstanceBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ServiceInstanceBinding(rs.getString(1),
                    rs.getString(2),
                    new HashMap<String, Object>(),
                    "",
                    rs.getString(3));
        }
    }*/


    public ServiceInstance createServiceInstanceByRequest(CreateServiceInstanceRequest request){
        System.out.println("OracleAdminService.createServiceInstanceByRequest");
        return new ServiceInstance(request).withDashboardUrl(getDashboardUrl(request.getServiceInstanceId()));
    }

    public boolean isExistsService(ServiceInstance instance) {
        System.out.println("서비스 인스턴스 아이디?" + instance.getServiceInstanceId());
        System.out.println("database~~~~~ " + getDatabase(instance.getServiceInstanceId()));
        //List<String> databases = jdbcTemplate.queryForList("SELECT TABLESPACE_NAME, STATUS  FROM USER_TABLESPACES where TABLESPACE_NAME = " + instance.getServiceInstanceId(), String.class);
        List<String> databases = jdbcTemplate.queryForList(TABLESPACE_FIND_BY_INSTANCE_ID, String.class, getDatabase(instance.getServiceInstanceId()));
        System.out.println("데이터 베이시스 크기?" + databases.size());
        return databases.size() > 0;
    }

    /**
     * 사용자 유무를 확인합니다.
     * @param userId
     * @return
     */
    public boolean isExistsUser(String userId){
        System.out.println("MysqlAdminService.isExistsUser");
        try {
            jdbcTemplate.execute("SHOW GRANTS FOR '"+userId+"'");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void deleteDatabase(ServiceInstance instance) {
        System.out.println("데이터 베이스 삭제 ");
        jdbcTemplate.execute("DROP TABLESPACE " + getDatabase(instance.getServiceInstanceId()) + " INCLUDING CONTENTS AND DATAFILES");
        System.out.println("템프 지우는 문자열좀 봅시다  : " + "DROP TABLESPACE " + getDatabase(instance.getServiceInstanceId()) + "_temp INCLUDING CONTENTS AND DATAFILES");
        jdbcTemplate.execute("DROP TABLESPACE " + getDatabase(instance.getServiceInstanceId()) + "_temp INCLUDING CONTENTS AND DATAFILES");
    }

    public void createDatabase(ServiceInstance instance) {
        System.out.println("데이터 베이스 생성");
        String createDatabase = "CREATE TABLESPACE " + getDatabase(instance.getServiceInstanceId()) + " datafile '/oracle/" + getDatabase(instance.getServiceInstanceId()) + ".dat' size 10M autoextend on maxsize 10M extent management local uniform size 64K";
        jdbcTemplate.execute(createDatabase);

        //템프
        String createDatabase2 = "CREATE TEMPORARY TABLESPACE " + getDatabase(instance.getServiceInstanceId()) + "_temp tempfile '/oracle/" + getDatabase(instance.getServiceInstanceId()) + "_temp.dat' size 10M autoextend on next 32m  maxsize 10M extent management local";
        System.out.println("템프 만드는 문자열  : " + createDatabase2);
        jdbcTemplate.execute(createDatabase2);
    }

    public void save(ServiceInstance instance) {
        System.out.println("오라클 어드민 서비스 세이브");
        if(findById(instance.getServiceInstanceId()) == null)  {//service_instance_id, service_id, plan_id, organization_guid, space_guid
            System.out.println(SERVICE_INSTANCES_ADD + instance.getServiceInstanceId() + instance.getServiceDefinitionId() + instance.getPlanId() + instance.getOrganizationGuid() + instance.getSpaceGuid());
            jdbcTemplate.update(SERVICE_INSTANCES_ADD, instance.getServiceInstanceId(), instance.getServiceDefinitionId(), instance.getPlanId(), instance.getOrganizationGuid(), instance.getSpaceGuid());
        } else {
            jdbcTemplate.update(SERVICE_INSTANCES_UPDATE, instance.getServiceInstanceId(), instance.getServiceDefinitionId(), instance.getPlanId(), instance.getOrganizationGuid(), instance.getSpaceGuid());
        }
    }

    public String getDatabase(String id){

        String database;
        String s = id.replaceAll("-", "_");
        database = DATABASE_PREFIX+s;

        return database;
    }

    // User명 생성
    public String getUsername(String id) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            // TODO: handle exception
        }
        digest.update(id.getBytes());
        String username = new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z0-9]+/", "").substring(0, 16);
        return username;

    }

    /**
     * ServiceInstanceBindingId로 ServiceInstanceBinding 정보를 조회합니다.
     * @param id
     * @return
     */
    public ServiceInstanceBinding findBindById(String id){
        System.out.println("OracleAdminService.findBindById");
        ServiceInstanceBinding serviceInstanceBinding = null;
        try {
            serviceInstanceBinding = jdbcTemplate.queryForObject(SERVICE_BINDING_FIND_BY_BINDING_ID, mapper2, id);
        } catch (Exception e) {
        }
        return serviceInstanceBinding;
    }

    /**
     * 요청 정보로부터 ServiceInstanceBinding 정보를 생성합니다.
     * @param request
     * @return
     */
    public ServiceInstanceBinding createServiceInstanceBindingByRequest(CreateServiceInstanceBindingRequest request){
        System.out.println("MysqlAdminService.createServiceInstanceBindingByRequest");
        return new ServiceInstanceBinding(request.getBindingId(),
                request.getServiceInstanceId(),
                new HashMap<String, Object>(),
                "syslogDrainUrl",
                request.getAppGuid());
    }

    /**
     * Database 접속정보를 생성합니다.
     * @param database
     * @param username
     * @param password
     * @param hostName
     * @return
     */
    public String getConnectionString(String database, String username, String password, String hostName) {
        //mysql://ns4VKg4Xtoy5mNCo:KyNRTVYPJyoqG1xo@10.30.40.163:3306/cf_dd2e0ffe_2bab_4308_b191_7d8814b16933
        StringBuilder builder = new StringBuilder();
        builder.append("mysql://"+username+":"+password+"@"+hostName+":3306/"+database);
        return builder.toString();
    }

    /**
     * 해당하는 User를 삭제합니다.
     */
    public void deleteUser(String userId) throws OracleServiceException {
        try{
            System.out.println("MysqlAdminService.deleteUser");
            jdbcTemplate.execute("DROP USER '"+userId+"'@'%'");
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private OracleServiceException handleException(Exception e) {
        return new OracleServiceException(e.getLocalizedMessage());
    }

    // User MAX_USER_CONNECTIONS 설정 조정
    public boolean checkUserConnections(String planId, String id) throws ServiceBrokerException {

		/* Plan 정보 설정 */
        int totalConnections = 0;
        int totalUsers;

        if(planA.equals(planId)) totalConnections = planAconnections;
        if(planB.equals(planId)) totalConnections = planBconnections;
        if(!planA.equals(planId) && !planB.equals(planId)) throw new ServiceBrokerException("");

        // ServiceInstanceBinding 정보를 조회한다.
        List<Map<String,Object>> list = findBindByInstanceId(id);
        // ServiceInstance의 총 Binding 건수 확인
        totalUsers = list.size();

        if(totalConnections <= totalUsers) return true;

        return false;
    }

    private static final class ServiceInstanceBindingRowMapper implements RowMapper<ServiceInstanceBinding> {
        @Override
        public ServiceInstanceBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ServiceInstanceBinding(rs.getString(1),
                    rs.getString(2),
                    new HashMap<String, Object>(),
                    "",
                    rs.getString(3));
        }
    }
}
