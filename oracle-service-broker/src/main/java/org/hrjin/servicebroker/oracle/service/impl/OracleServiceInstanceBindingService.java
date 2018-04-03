package org.hrjin.servicebroker.oracle.service.impl;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.hrjin.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.hrjin.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.hrjin.servicebroker.model.ServiceInstance;
import org.hrjin.servicebroker.model.ServiceInstanceBinding;
import org.hrjin.servicebroker.oracle.exception.OracleServiceException;
import org.hrjin.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hrjin on 2018-04-03.
 */
@PropertySource("classpath:application.yml")
@Service
public class OracleServiceInstanceBindingService implements ServiceInstanceBindingService{

    private static final Logger logger = LoggerFactory.getLogger(OracleServiceInstanceBindingService.class);

    @Autowired
    private Environment env;

    @Autowired
    private OracleAdminService oracleAdminService;

    @Autowired
    public OracleServiceInstanceBindingService(OracleAdminService oracleAdminService){
        this.oracleAdminService = oracleAdminService;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) throws ServiceInstanceBindingExistsException, ServiceBrokerException{
        logger.debug("OracleServiceInstanceBindingService CLASS createServiceInstanceBinding");

		/* 최초 ServiceInstanceBinding 생성 요청시에는 해당 ServiceInstanceBinding가 존재하지 않아 해당 메소드를 주석처리 하였습니다.*/
        //ServiceInstanceBinding binding = mysqlAdminService.findBindById(request.getBindingId());
        ServiceInstanceBinding findBinding = oracleAdminService.findBindById(request.getBindingId());

        // 요청 정보로부터 ServiceInstanceBinding 정보를 생성합니다.
        ServiceInstanceBinding binding = oracleAdminService.createServiceInstanceBindingByRequest(request);

		/* 요청 정보롭터 ServiceInstanceBinding 정보를 생성 할 경우 다음 처리부분이 불필요하여 주석처리 합니다.
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}
		*/
        if(findBinding != null){
            if(findBinding.getServiceInstanceId().equals(binding.getServiceInstanceId()) &&
                    findBinding.getId().equals(binding.getId())  &&
                    findBinding.getAppGuid().equals(binding.getAppGuid())){
                findBinding = getBindingInfo(request, findBinding);
                findBinding.setHttpStatusOK();
                return findBinding;
            }else{
                throw new ServiceInstanceBindingExistsException(binding);
            }

        }

        // 요청 정보로부터 ServiceInstance정보를 조회합니다.
        ServiceInstance instance = oracleAdminService.findById(request.getServiceInstanceId());

        // ServiceInstance정보가 엇을경우 예외처리
        if(instance == null) throw new ServiceBrokerException("Not Exists ServiceInstance");

        // Database명을 조회합니다.
        String database = oracleAdminService.getDatabase(instance.getServiceInstanceId());
        // 사용자 아이디를 생성합니다.
        String username = oracleAdminService.getUsername(request.getBindingId());
        // 사용자 비밀번호를 생성합니다.
        //String password = UUID.randomUUID().toString().replace("-", "");
        String password = oracleAdminService.getUsername(request.getServiceInstanceId());

		/* 새로운 사용자명이 존재하는지 검증합니다.*/
        if (oracleAdminService.isExistsUser(username)) {
            // ensure the instance is empty
            //mysqlAdminService.deleteUser(database, username);

            // 사용자 삭제시 특정 Databas의 사용자를 삭제하지 않아 아래와 같이 사용자 명으로 삭제 처리합니다.
            oracleAdminService.deleteUser(username);
        }

        if(oracleAdminService.checkUserConnections(instance.getPlanId(), instance.getServiceInstanceId())){
            throw new ServiceBrokerException("It may not exceed the specified plan.(Not assign Max User Connection)");
        }
        // 새로운 사용자를 생성합니다.
        oracleAdminService.createUser(database, username, password);

        // 반환될 credentials 정보를 생성합니다.
        Map<String,Object> credentials = new HashMap<String,Object>();
        credentials.put("name", database);
        credentials.put("hostname", env.getRequiredProperty("jdbc.host"));
        credentials.put("port", env.getRequiredProperty("jdbc.port"));
        credentials.put("username", username);
        credentials.put("password", password);
        credentials.put("uri", oracleAdminService.getConnectionString(database, username, password, env.getRequiredProperty("jdbc.host")));
        binding = new ServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());

        // Binding 정보를 저장합니다.
        oracleAdminService.saveBind(binding);

        // ServiceInstance의 Plan에 따라 사용자별 MAX_USER_CONNECTIONS 정보를 조정합니다.
        oracleAdminService.setUserConnections(instance.getPlanId(), instance.getServiceInstanceId());

        return binding;
    }

    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest) throws ServiceBrokerException {
        return null;
    }

    /**
     * Binding Info
     * @param request
     * @param instance
     * @return
     */
    public ServiceInstanceBinding getBindingInfo(CreateServiceInstanceBindingRequest request, ServiceInstanceBinding instance){
        // Database명을 조회합니다.
        String database = oracleAdminService.getDatabase(instance.getServiceInstanceId());
        // 사용자 아이디를 생성합니다.
        String username = oracleAdminService.getUsername(request.getBindingId());
        // 사용자 비밀번호를 생성합니다.
        //String password = UUID.randomUUID().toString().replace("-", "");
        String password = oracleAdminService.getUsername(request.getServiceInstanceId());

        // 반환될 credentials 정보를 생성합니다.
        Map<String,Object> credentials = new HashMap<String,Object>();
        credentials.put("name", database);
        credentials.put("hostname", env.getRequiredProperty("jdbc.host"));
        credentials.put("port", env.getRequiredProperty("jdbc.port"));
        credentials.put("username", username);
        credentials.put("password", password);
        credentials.put("uri", oracleAdminService.getConnectionString(database, username, password, env.getRequiredProperty("jdbc.host")));

        return new ServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());

    }
}
