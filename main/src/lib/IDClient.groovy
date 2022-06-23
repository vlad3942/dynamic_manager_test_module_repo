package lib
/**
 * Model describing the main fields for authorization
 */

class IDClient {

    int clientId
    String cpa

    IDClient(final int clientId, final String cpa) {
        this.clientId = clientId
        this.cpa = cpa
    }
}
