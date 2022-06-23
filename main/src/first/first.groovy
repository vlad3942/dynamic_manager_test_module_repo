@groovy.transform.BaseScript(com.cellpointdigital.amgateway.dsl.AmadeusScript)

package first

import com.cellpointdigital.amgateway.sdk.dapi.AmadeusDapiClient
import com.cellpointdigital.amgateway.sdk.dapi.dto.AmadeusPaymentRecordsRequest
import com.cellpointdigital.dynamic.dsl.DI
import com.cellpointdigital.dynamic.dsl.dag.ExecutingContext
import com.cellpointdigital.dynamic.dsl.dag.StepStatus
import groovy.json.JsonBuilder
import lib.AuthDApiService
import lib.ParsedBookingConfig

def log = getLogger()
/**
 * requests bean from the application (Spring)
 */
def dApiClient = DI.getAppBean(AmadeusDapiClient)

/**
 * requests bean from the Groovy Module
 */
def authService = DI.getBean(AuthDApiService)

def pipeline = pipeline {
    steps {
        step "authenticate", {
            final ticketCreds = new ParsedBookingConfig.DAPICredentials().tap {
                clientId = "Pz1fmjUELnqpzQDjXD5yRvYGNOAMzGga"
                clientSecret = "O2pyq6xE5VUE79kZ"
                grantType = "client_credentials"
                officeId = "BOGAV08LK"
            }
            ticketingToken = authService.authenticateAndGetToken(ticketCreds)
            ticketingToken
        }

        step "DEFINES PAYMENT METHOD", {
            final request = new AmadeusPaymentRecordsRequest().tap {
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
            try {
                return dApiClient.setPaymentRecords(ticketingToken, orderNo, lastName, request)
            } catch (Throwable e) {
                this.log.error(e.toString())
                result.status = StepStatus.ERROR
            }
        }

        step "ISSUES TICKETS", {
            Map documents = dApiClient.issueTravelDocuments(ticketingToken, orderNo, lastName, "")

            if (documents.get('error') != null) {
                result.status = StepStatus.ERROR
            }
            return documents
        }
    }
}

http {

    configuration('hi') {
        port = 8085
    }

    on get, "/some/test/endpoint/{clientId}/order/{orderNo}", {
        when {
            parameters.clientId == "10101"
        }
        execute {
            log.debug("test debug message inside execute")
        }
    }

    on get, "/ticketing/{clientId}/order/{orderNo}", {
        when {
            parameters.clientId == "10101"
        }
        execute {
            def context = new ExecutingContext(module)
            context.orderNo = parameters.orderNo
            context.lastName = parameters.lastName;

            def result = pipeline.execute(context)

            if (result.status == StepStatus.SUCCESS) {
                responseEntity = "OK"
            } else {
                responseEntity = "ERROR"
            }
        }

    }

    on get, "/authenticate/10101", {
        execute {
            responseEntity = new JsonBuilder().tap {
                item {
                    id '?????'
                }
            }
        }
    }

}


