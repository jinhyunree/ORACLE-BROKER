package org.hrjin.servicebroker.oracle.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;

/**
 * Created by hrjin on 2018-04-03.
 */
public class OracleServiceException extends ServiceBrokerException {

    private static final long serialVersionUID = 8667141725171626000L;

    public OracleServiceException(String message) {
        super(message);
    }

}
