package lib

import com.cellpointdigital.amgateway.dsl.errors.GeneralException
import com.cellpointdigital.amgateway.sdk.mretail.dao.MRetailDao
import com.cellpointdigital.amgateway.sdk.mretail.dao.NameValue
import com.cellpointdigital.dynamic.dsl.DI

class BookingConfigProvider {

    def mRetailDao = DI.getAppBean(MRetailDao)

    private MRetailBookingConfig requestIt(final IDClient k) {
        final def id = mRetailDao.getBookingAccountId(k.getClientId(), k.getCpa())
        final def info = createClientConfig(mRetailDao.getClientInfo(id))
        final def locations = createLocations(mRetailDao.getLocationsInfo(id))
        return new MRetailBookingConfig(info, locations)
    }

    private static List<MRetailBookingConfig.Details> createLocations(final Map<String, List<NameValue>> data) {
        final ArrayList<MRetailBookingConfig.Details> result = new ArrayList<>()
        data.forEach((k, v) -> {
            final MRetailBookingConfig.Details details = new MRetailBookingConfig.Details()
            details.setLocation(k)
            details.populate(v)
            result.add(details)
        })
        return result
    }

    private static MRetailBookingConfig.ClientConfig createClientConfig(final List<NameValue> data) {
        final MRetailBookingConfig.ClientConfig result = new MRetailBookingConfig.ClientConfig()
        result.populate(data)
        return result
    }

    ParsedBookingConfig getParsedBookingConfig(final ParsedBookingConfigRequest request) {

        IDClient key = new IDClient(request.getClientId(), request.getCpa())

        final MRetailBookingConfig config = requestIt(key)

        testClientConfig(config.getConfig())

        final MRetailBookingConfig.Details details =
                config.getLocations().stream()
                      .filter(l -> l.match(request))
                      .findFirst()
                      .orElseThrow(() -> new GeneralException(
                              "Booking configuration is not defined for {}.",
                              Errors.ILLEGAL_ARGUMENT, request
                      ))

        final var clientConfig = config.getConfig()

        final var tc = new ParsedBookingConfig.DAPICredentials()
                tc.clientId = clientConfig.getClientId()
                tc.clientSecret = clientConfig.getClientSecret()
                tc.grantType = clientConfig.getGrantType()
                tc.officeId = details.getTicketingOID()
        final var sc = new ParsedBookingConfig.DAPICredentials()
                sc.clientId = clientConfig.getClientId()
                sc.clientSecret = clientConfig.getClientSecret()
                sc.grantType = clientConfig.getGrantType()
                sc.officeId = details.getShoppingOID()
        final var vpc = new ParsedBookingConfig.TADMCredentials()
                vpc.aviancaBaseUrl = clientConfig.getAviancaBaseUrl()
                vpc.aviancaMsIdp = clientConfig.getAviancaMsIdp()
                vpc.aviancaOcpApimSubscriptionKey = clientConfig.getAviancaOcpApimSubscriptionKey()
                vpc.clientId = clientConfig.getAviancaClientId()
                vpc.clientSecret = clientConfig.getAviancaClientSecret()
                vpc.grantType = clientConfig.getAviancaGrantType()
                vpc.resource = clientConfig.getAviancaResource()
        return new ParsedBookingConfig(
                details.getIataCode(),
                sc,
                tc,
                vpc
        )
    }

    private static void testClientConfig(final MRetailBookingConfig.ClientConfig config) {
        if (
            config.getAviancaClientId()?.isBlank() ||
            config.getAviancaClientSecret()?.isBlank() ||
            config.getAviancaGrantType()?.isBlank() ||
            config.getAviancaResource()?.isBlank() ||
            config.getAviancaMsIdp()?.isBlank() ||
            config.getAviancaBaseUrl()?.isBlank() ||
            config.getAviancaOcpApimSubscriptionKey()?.isBlank()
        ) {
            throw new IllegalStateException(
                    "'Vianca TADM connection configuration is invalid, check the values in tags booking-config/client-config/avianca_*")
        }
    }


}
