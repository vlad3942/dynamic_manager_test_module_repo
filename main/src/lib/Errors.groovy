package lib

import com.cellpointdigital.amgateway.dsl.errors.Error

/**
 * Constants indicating major server errors
 */
class Errors {
    public static final Error ILLEGAL_ARGUMENT = new Error("00400");
    public static final Error INTERNAL_ERROR = new Error("00500");
    public static final Error CANNOT_DAPI_AUTHENTICATE = new Error("AUTH-00001");
}
