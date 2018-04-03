package org.hrjin.servicebroker.controller;

import org.hrjin.servicebroker.exception.*;
import org.hrjin.servicebroker.model.*;
import org.hrjin.servicebroker.service.ServiceCatalogService;
import org.hrjin.servicebroker.service.ServiceInstanceProvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 *
 * 서비스 인스턴스 관련 Provision/Update Instance/Unprovision API 를 호출 받는 컨트롤러이다.
 *
 * Created by hrjin on 2018-03-26.
 */
@Controller
public class ServiceInstanceProvController extends BaseController{
    public static final String BASE_PATH = "/v2/service_instances";

    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceProvController.class);

    @Autowired
    private ServiceInstanceProvService service;
    @Autowired
    private ServiceCatalogService catalogService;

    @Autowired
    public ServiceInstanceProvController(ServiceInstanceProvService service, ServiceCatalogService catalogService) {
        this.service = service;
        this.catalogService = catalogService;
    }

    /*
    * 서비스 인스턴스 생성
    * */
    @RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.PUT)
    public ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(
            @PathVariable("instanceId") String serviceInstanceId,
            @Valid @RequestBody CreateServiceInstanceRequest request) throws
            ServiceDefinitionDoesNotExistException,
            ServiceInstanceExistsException,
            ServiceBrokerException {
        logger.debug("PUT: " + BASE_PATH + "/{instanceId}"
                + ", createServiceInstance(), serviceInstanceId = " + serviceInstanceId);
        ServiceDefinition svc = catalogService.getServiceDefinition(request.getServiceDefinitionId());
        logger.debug("svc..........................");
        if (svc == null) {
            throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
        }
        logger.debug("ServiceDefinitionDoesNotExistException");

        ServiceInstance instance = service.createServiceInstance(
                request.withServiceDefinition(svc).and().withServiceInstanceId(serviceInstanceId));

        logger.debug("ServiceInstance Created: " + instance.getServiceInstanceId());
        return new ResponseEntity<CreateServiceInstanceResponse>(
                new CreateServiceInstanceResponse(instance),
                instance.getHttpStatus());
    }

    @RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteServiceInstance(
            @PathVariable("instanceId") String instanceId,
            @RequestParam("service_id") String serviceId,
            @RequestParam("plan_id") String planId) throws ServiceBrokerException {
        logger.debug( "DELETE: " + BASE_PATH + "/{instanceId}"
                + ", deleteServiceInstanceBinding(), serviceInstanceId = " + instanceId
                + ", serviceId = " + serviceId
                + ", planId = " + planId);
        ServiceInstance instance = service.deleteServiceInstance(
                new DeleteServiceInstanceRequest(instanceId, serviceId, planId));
        if (instance == null) {
            return new ResponseEntity<String>("{}", HttpStatus.GONE);
        }
        logger.debug("ServiceInstance Deleted: " + instance.getServiceInstanceId());
        return new ResponseEntity<String>("{}", HttpStatus.OK);
    }

    @RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.PATCH)
    public ResponseEntity<String> updateServiceInstance(
            @PathVariable("instanceId") String instanceId,
            @Valid @RequestBody UpdateServiceInstanceRequest request) throws
            ServiceInstanceUpdateNotSupportedException,
            ServiceInstanceDoesNotExistException,
            ServiceBrokerException {
        logger.debug("UPDATE: " + BASE_PATH + "/{instanceId}"
                + ", updateServiceInstanceBinding(), serviceInstanceId = "
                + instanceId + ", instanceId = " + instanceId + ", planId = "
                + request.getPlanId());
        ServiceInstance instance = service.updateServiceInstance(request.withInstanceId(instanceId));
        logger.debug("ServiceInstance updated: " + instance.getServiceInstanceId());
        return new ResponseEntity<String>("{}", HttpStatus.OK);
    }


    @ExceptionHandler(ServiceDefinitionDoesNotExistException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceDefinitionDoesNotExistException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ServiceInstanceExistsException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(
            ServiceInstanceExistsException ex,
            HttpServletResponse response) {
        return new ResponseEntity<String>("{}", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceInstanceUpdateNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceUpdateNotSupportedException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
