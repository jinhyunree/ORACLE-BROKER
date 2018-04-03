package org.hrjin.servicebroker.oracle.service.impl;

import org.hrjin.servicebroker.model.CreateServiceInstanceRequest;
import org.hrjin.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

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


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public OracleAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private static final RowMapper<ServiceInstance> mapper = new ServiceInstanceRowMapper();

    //private static final RowMapper<ServiceInstanceBinding> mapper2 = new ServiceInstanceBindingRowMapper();


    public ServiceInstance findById(String id){
        System.out.println("OracleAdminService.findById");
        ServiceInstance serviceInstance = null;;
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
        //List<String> databases = jdbcTemplate.queryForList("SELECT TABLESPACE_NAME, STATUS  FROM USER_TABLESPACES where TABLESPACE_NAME = " + instance.getServiceInstanceId(), String.class);
        List<String> databases = jdbcTemplate.queryForList(TABLESPACE_FIND_BY_INSTANCE_ID, String.class, instance.getServiceInstanceId());
        System.out.println("데이터 베이시스 크기?" + databases.size());
        return false;
    }

    public void deleteDatabase(ServiceInstance instance) {
        System.out.println("데이터 베이스 삭제 ");
        jdbcTemplate.execute("DROP TABLESPACE ? INCLUDING CONTENTS AND DATAFILES" + instance.getServiceInstanceId());

    }

    public void createDatabase(ServiceInstance instance) {
        System.out.println("데이터 베이스 생성");
        String createDatabase = "CREATE TABLESPACE " + "oracle_2018_04_02" + " datafile '/oracle/" + instance.getServiceInstanceId() + ".dat' size 10M autoextend on maxsize 10M extent management local uniform size 64K";
        System.out.println("데이터 베이스 생성" + createDatabase);
        jdbcTemplate.execute(createDatabase);

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
}
