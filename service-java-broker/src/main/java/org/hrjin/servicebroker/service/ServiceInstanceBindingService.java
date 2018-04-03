package org.hrjin.servicebroker.service;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.hrjin.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.hrjin.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.hrjin.servicebroker.model.ServiceInstanceBinding;

/**
 * Created by hrjin on 2018-04-03.
 */
public interface ServiceInstanceBindingService {
    /**
     * Create a new binding to a service instance.
     * @param createServiceInstanceBindingRequest containing parameters sent from Cloud Controller
     * @return The newly created ServiceInstanceBinding
     * @throws ServiceInstanceBindingExistsException if the same binding already exists
     * @throws ServiceBrokerException on internal failure
     */
    ServiceInstanceBinding createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException, OracleServiceException;

    /**
     * Delete the service instance binding. If a binding doesn't exist,
     * return null.
     * @param deleteServiceInstanceBindingRequest containing parameters sent from Cloud Controller
     * @return The deleted ServiceInstanceBinding or null if one does not exist
     * @throws ServiceBrokerException on internal failure
     */
    ServiceInstanceBinding deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest)
            throws ServiceBrokerException;
}
