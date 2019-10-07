package org.iot.dsa.dslink;

/**
 * This is a marker interface that indicates ActionResults are async.
 * <p>
 * Implementors are required to call ActionRequest.sendResults() for sending
 * the initial column definitions, as well as after returning false from next().
 * <p>
 * Implementors are also required to call ActionRequest.close().
 *
 * @author Aaron Hansen
 */
public interface AsyncActionResults extends ActionResults {

}
