package org.hrjin.servicebroker.oracle.service.impl;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.hrjin.servicebroker.exception.ServiceInstanceExistsException;
import org.hrjin.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.hrjin.servicebroker.model.CreateServiceInstanceRequest;
import org.hrjin.servicebroker.model.DeleteServiceInstanceRequest;
import org.hrjin.servicebroker.model.ServiceInstance;
import org.hrjin.servicebroker.model.UpdateServiceInstanceRequest;
import org.hrjin.servicebroker.service.ServiceCatalogService;
import org.hrjin.servicebroker.service.ServiceInstanceProvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hrjin on 2018-03-27.
 *
 *  서비스 인스턴스 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스인 ServiceInstance 를 상속하여
 *  Oracle 서비스 인스턴스 서비스 관련 메소드를 구현한 클래스.
 *  서비스 인스턴스 생성/삭제/수정/조회 를 구현한다.
 */
@Service
public class OracleServiceInstanceProvService implements ServiceInstanceProvService{

    private static final Logger logger = LoggerFactory.getLogger(OracleServiceInstanceProvService.class);

    @Autowired
    private OracleAdminService oracleAdminService;

    @Autowired
    private ServiceCatalogService serviceCatalogService;

    @Autowired
    public OracleServiceInstanceProvService(OracleAdminService oracleAdminService){this.oracleAdminService = oracleAdminService;}

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest createServiceInstanceRequest) throws ServiceInstanceExistsException, ServiceBrokerException {
        // 서비스 인스턴스가 존재하는 지 확인
        ServiceInstance findInstance = oracleAdminService.findById(createServiceInstanceRequest.getServiceInstanceId());
        logger.info("서비스 인스턴스 존재하니?" + findInstance);

        // 요청 정보로부터 ServiceInstance 정보를 생성합니다.
        ServiceInstance instance = oracleAdminService.createServiceInstanceByRequest(createServiceInstanceRequest);

        if(findInstance != null){
            if(findInstance.getServiceInstanceId().equals(instance.getServiceInstanceId()) && findInstance.getPlanId().equals(instance.getPlanId()) &&
                    findInstance.getServiceDefinitionId().equals(instance.getServiceDefinitionId())){
                findInstance.setHttpStatusOK();
                return findInstance;
            }else{
                throw new ServiceInstanceExistsException(instance);
            }
        }

        // 해당 요청 정보로부터 생성된 Database 가 존재하는지 확인합니다.
        // 존재 할 경우 Database 를 삭제합니다.
        if(oracleAdminService.isExistsService(instance)){
            oracleAdminService.deleteDatabase(instance);
        }

        // Database 를 생성합니다.
        oracleAdminService.createDatabase(instance);

        // ServiceInstance 정보를 저장합니다.
        oracleAdminService.save(instance);

        return instance;
    }

    @Override
    public ServiceInstance getServiceInstance(String serviceInstanceId) {
        return null;
    }

    @Override
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest deleteServiceInstanceRequest) throws ServiceBrokerException {
        return null;
    }

    @Override
    public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest updateServiceInstanceRequest) throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
        return null;
    }
}
