package lib

import com.cellpointdigital.amgateway.sdk.mretail.dao.NameValue

class MRetailBookingConfig {

    ClientConfig config
    List<Details> locations

    MRetailBookingConfig(final ClientConfig config, final List<Details> locations) {
        this.config = config
        this.locations = locations
    }

    static class ClientConfig {
        String clientId
        String clientSecret
        String grantType
        String aviancaClientId
        String aviancaClientSecret
        String aviancaGrantType
        String aviancaResource
        String aviancaMsIdp
        String aviancaBaseUrl
        String aviancaOcpApimSubscriptionKey

        void populate(List<NameValue> list) {
            list.forEach(kv -> {
                switch (kv.getName()) {
                    case "client_id": clientId = kv.getValue(); break
                    case "client_secret": clientSecret = kv.getValue(); break
                    case "grant_type": grantType = kv.getValue(); break
                    case "avianca_client_id": aviancaClientId = kv.getValue(); break
                    case "avianca_client_secret": aviancaClientSecret = kv.getValue(); break
                    case "avianca_grant_type": aviancaGrantType = kv.getValue(); break
                    case "avianca_resource": aviancaResource = kv.getValue(); break
                    case "avianca_ms_idp": aviancaMsIdp = kv.getValue(); break
                    case "avianca_base_url": aviancaBaseUrl = kv.getValue(); break
                    case "avianca_ocp_apim_subscription_key": aviancaOcpApimSubscriptionKey = kv.getValue(); break
                }
            })
        }
    }

    static class Details {
        String location
        String currency
        String iataCode
        String mop
        int posCountryID
        String shoppingOID
        String ticketingOID

        boolean match(final ParsedBookingConfigRequest r) {
            return posCountryID == r.getPosCountryId() && currency == r.getCurrency()
                    && mop == r.getMop() && shoppingOID == r.getShoppingOId()
        }

        void populate(final List<NameValue> list) {
            list.forEach(kv -> {
                switch (kv.getName()) {
                    case "Currency": currency = kv.getValue(); break
                    case "IATACode": iataCode = kv.getValue(); break
                    case "MOP": mop = kv.getValue(); break
                    case "PosCountryID": posCountryID = Integer.parseInt(kv.getValue()); break
                    case "Shopping_OID": shoppingOID = kv.getValue(); break
                    case "Ticketing_OID": ticketingOID = kv.getValue(); break
                }
            })
        }

    }

}

class ParsedBookingConfig {

    ParsedBookingConfig(String iataCode, DAPICredentials ticketingCredentials, DAPICredentials shoppingCredentials, TADMCredentials viancaPayCredentials) {
        this.iataCode = iataCode
        this.ticketingCredentials = ticketingCredentials
        this.shoppingCredentials = shoppingCredentials
        this.viancaPayCredentials = viancaPayCredentials
    }

    String iataCode
    DAPICredentials ticketingCredentials
    DAPICredentials shoppingCredentials
    TADMCredentials viancaPayCredentials

    static class DAPICredentials {
        String clientId
        String clientSecret
        String grantType
        String officeId
    }

    static class TADMCredentials {
        String clientId
        String clientSecret
        String grantType
        String resource
        String aviancaMsIdp
        String aviancaBaseUrl
        String aviancaOcpApimSubscriptionKey
    }
}

class ParsedBookingConfigRequest {
    String cpa
    int clientId
    String currency
    String shoppingOId
    String mop
    int posCountryId
}
