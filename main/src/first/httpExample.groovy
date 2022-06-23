@groovy.transform.BaseScript(com.cellpointdigital.amgateway.dsl.AmadeusScript)

package first

import com.cellpointdigital.amgateway.sdk.dapi.dto.AmadeusAuthTokenResponse
import com.cellpointdigital.amgateway.sdk.dapi.dto.AmadeusPaymentRecordsRequest
import com.cellpointdigital.dynamic.dsl.dag.ExecutingContext
import com.cellpointdigital.dynamic.dsl.dag.StepStatus
import com.cellpointdigital.dynamic.dsl.dag.builder.PipelineBuilder
import com.cellpointdigital.dynamic.dsl.httprequest.HttpRequestBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import lib.ParsedBookingConfig

import java.net.http.HttpClient
import java.net.http.HttpResponse

final log = getLogger()
final service = "https://test.airlines.api.amadeus.com"
final httpClient = HttpClient.newHttpClient()
final ObjectMapper mapper = new ObjectMapper()

def pipeline = PipelineBuilder.pipeline {
    steps {
        step "authenticate", {
            final ticketCreds = new ParsedBookingConfig.DAPICredentials().tap {
                clientId = "Pz1fmjUELnqpzQDjXD5yRvYGNOAMzGga"
                clientSecret = "O2pyq6xE5VUE79kZ"
                grantType = "client_credentials"
                officeId = "BOGAV08LK"
            }

            def request = HttpRequestBuilder.builder("${service}/v1/security/oauth2/token", {
                postWithFormParams(
                        [client_id    : ticketCreds.clientId,
                         client_secret: ticketCreds.clientSecret,
                         grant_type   : ticketCreds.grantType,
                         office_id    : ticketCreds.officeId])
            }).build()

            ticketingToken = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .handle({ response, t ->
                        if (t == null && response.statusCode() == 200) {
                            def authResponse = mapper.readValue(response.body(), AmadeusAuthTokenResponse.class)
                            return "${authResponse.token_type} ${authResponse.access_token}"
                        } else {
                            result.status = StepStatus.ERROR
                            log.error((t == null) ? t.toString() : "HTTP Code: {}. Body: {}", response.statusCode(), response.body())
                        }
                        return ""
                    })
            ticketingToken
        }
        step "DEFINES PAYMENT METHOD", {
            def request = new HttpRequestBuilder("${service}/v2/purchase/orders/${orderNo}/payment-records").tap {
                pathParam "lastName", lastName
                authorization ticketingToken.get()
                postWithJson new AmadeusPaymentRecordsRequest().tap {
                    paymentRequests += new AmadeusPaymentRecordsRequest.Payment().tap {
                        paymentMethod = new AmadeusPaymentRecordsRequest.PaymentMethod().tap {
                            paymentType = "PaymentCard"
                            cardNumber = "4111111111111111"
                            expiryDate = "1223"
                            holderName = "dad"
                            vendorCode = "VI"
                        }
                        authorization = new AmadeusPaymentRecordsRequest.Authorization("463785")
                    }
                }
            }.build()

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .handle({response, t ->
                        if (t != null || response.statusCode() != 200) {
                            result.status = StepStatus.ERROR
                            log.error((t == null) ? t.toString() : "HTTP Code: {}. Body: {}", response.statusCode(), response.body())
                        }
                    })
        }
        step "ISSUES TICKETS", {
            def request = HttpRequestBuilder.builder("${service}/v2/purchase/orders/${orderNo}}/travel-documents", {
                pathParam "lastName", lastName
                authorization ticketingToken.get()
                POST()
            }).build()

            def result = result
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply { response ->
                        def documents = mapper.readValue(response.body(), Map.class)
                        if (documents.get("error") != null) {
                            result.status = StepStatus.ERROR
                        }
                    }
        }
    }
}

http {

    configuration('hi') {
        port = 8085
    }

    on get, "/ticketing1/{clientId}/order/{orderNo}", {

        when {
            parameters.clientId == "10101"
        }

        execute {
            def context = new ExecutingContext(module)
            context.httpClient = httpClient
            context.orderNo = parameters.orderNo
            context.lastName = parameters.lastName
            pipeline.execute(context)
        }

    }
}
