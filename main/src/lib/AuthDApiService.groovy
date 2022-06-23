package lib

import com.cellpointdigital.amgateway.sdk.dapi.AmadeusDapiClient
import com.cellpointdigital.dynamic.dsl.DI

class AuthDApiService {

    private def dApiClient = DI.getAppBean(AmadeusDapiClient)

    String authenticateAndGetToken(final ParsedBookingConfig.DAPICredentials credentials) {
        def result = dApiClient.authenticate(
                credentials.getClientId(),
                credentials.getClientSecret(),
                credentials.getGrantType(),
                credentials.getOfficeId()
        )

        return result.getToken_type() + " " + result.getAccess_token()
    }

}
