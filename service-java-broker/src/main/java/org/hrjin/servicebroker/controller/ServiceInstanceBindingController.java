package org.hrjin.servicebroker.controller;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.hrjin.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.hrjin.servicebroker.model.*;
import org.hrjin.servicebroker.service.ServiceInstanceBindingService;
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
 * Created by hrjin on 2018-04-03.
 */
@Controller
public class ServiceInstanceBindingController extends BaseController{
    public static final String BASE_PATH = "/v2/service_instances/{instanceId}/service_bindings";

    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingController.class);

    @Autowired
    private ServiceInstanceBindingService serviceInstanceBindingService;
    @Autowired
    private ServiceInstanceProvService serviceInstanceService;

    @Autowired
    public ServiceInstanceBindingController(ServiceInstanceBindingService serviceInstanceBindingService,
                                            ServiceInstanceProvService  serviceInstanceService) {
        this.serviceInstanceBindingService = serviceInstanceBindingService;
        this.serviceInstanceService = serviceInstanceService;
    }

    @RequestMapping(value = BASE_PATH + "/{bindingId}", method = RequestMethod.PUT)
    public ResponseEntity<ServiceInstanceBindingResponse> bindServiceInstance(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("bindingId") String bindingId,
            @Valid @RequestBody CreateServiceInstanceBindingRequest request) throws
            ServiceInstanceDoesNotExistException, ServiceInstanceBindingExistsException,
            ServiceBrokerException {
        logger.debug( "PUT: " + BASE_PATH + "/{bindingId}"
                + ", bindServiceInstance(), serviceInstance.id = " + instanceId
                + ", bindingId = " + bindingId);
        ServiceInstance instance = serviceInstanceService.getServiceInstance(instanceId);
        if (instance == null) {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }
        ServiceInstanceBinding binding = serviceInstanceBindingService.createServiceInstanceBinding(
                request.withServiceInstanceId(instanceId).and().withBindingId(bindingId));
        logger.debug("ServiceInstanceBinding Created: " + binding.getId());
        return new ResponseEntity<ServiceInstanceBindingResponse>(
                new ServiceInstanceBindingResponse(binding),
                binding.getHttpStatus());
    }

    @RequestMapping(value = BASE_PATH + "/{bindingId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteServiceInstanceBinding(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("bindingId") String bindingId,
            @RequestParam("service_id") String serviceId,
            @RequestParam("plan_id") String planId) throws ServiceBrokerException, ServiceInstanceDoesNotExistException {
        logger.debug( "DELETE: " + BASE_PATH + "/{bindingId}"
                + ", deleteServiceInstanceBinding(),  serviceInstance.id = " + instanceId
                + ", bindingId = " + bindingId
                + ", serviceId = " + serviceId
                + ", planId = " + planId);
        ServiceInstance instance = serviceInstanceService.getServiceInstance(instanceId);
        if (instance == null) {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }
        ServiceInstanceBinding binding = serviceInstanceBindingService.deleteServiceInstanceBinding(
                new DeleteServiceInstanceBindingRequest( bindingId, instance, serviceId, planId));
        if (binding == null) {
            return new ResponseEntity<String>("{}", HttpStatus.GONE);
        }
        logger.debug("ServiceInstanceBinding Deleted: " + binding.getId());
        return new ResponseEntity<String>("{}", HttpStatus.OK);
    }

    @ExceptionHandler(ServiceInstanceDoesNotExistException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceDoesNotExistException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ServiceInstanceBindingExistsException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceBindingExistsException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }
}
