@groovy.transform.BaseScript(com.cellpointdigital.amgateway.dsl.AmadeusScript)

package first

import com.cellpointdigital.amgateway.sdk.dapi.AmadeusDapiClient
import com.cellpointdigital.amgateway.sdk.dapi.dto.AmadeusPaymentRecordsRequest
import com.cellpointdigital.dynamic.dsl.DI
import com.cellpointdigital.dynamic.dsl.dag.StepContext
import com.cellpointdigital.dynamic.dsl.dag.StepStatus
import groovy.json.JsonBuilder
import lib.AuthDApiService
import lib.ParsedBookingConfig

import static com.cellpointdigital.dynamic.dsl.dag.builder.PipelineBuilder.pipeline
/**
 * requests bean from the application (Spring)
 */
def dApiClient = DI.getAppBean(AmadeusDapiClient)

/**
 * requests bean from the Groovy Module
 */
def authService = DI.getBean(AuthDApiService)

http {

    configuration('hi') {
        port = 8085
    }

    on get, "/some/test/endpoint/{clientId}/order/{orderNo}", {
        when {
            parameters.clientId == "10101"
        }
        execute {

        }
    }

    on get, "/ticketing/{clientId}/order/{orderNo}", {
        when {
            parameters.clientId == "10101"
        }
        execute {
            final String orderNo = parameters.orderNo
            final String lastName = parameters.lastName

            def pipeline = pipeline {
                steps {
                    step "authenticate", {
                        exec {
                            final ticketCreds = new ParsedBookingConfig.DAPICredentials().tap {
                                clientId = "Pz1fmjUELnqpzQDjXD5yRvYGNOAMzGga"
                                clientSecret = "O2pyq6xE5VUE79kZ"
                                grantType = "client_credentials"
                                officeId = "BOGAV08LK"
                            }
                            final ticketingToken = authService.authenticateAndGetToken(ticketCreds)
                            return ticketingToken
                        }
                    }

                    step "DEFINES PAYMENT METHOD", {
                        exec {
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
                            def ticketingToken = findPrevResult("authenticate").execValue.toString()
                            try {
                                return dApiClient.setPaymentRecords(ticketingToken, orderNo, lastName, request)
                            } catch (Throwable e) {
                                this.log.error(e.toString())
                                result.status = StepStatus.ERROR
                            }
                        }
                    }

                    step "ISSUES TICKETS", {
                        exec {
                            def ticketingToken = findPrevResult("authenticate").execValue.toString();
                            Map documents = dApiClient.issueTravelDocuments(ticketingToken, orderNo, lastName, "")

                            if (documents.get('error') != null) {
                                result.status = StepStatus.ERROR
                            }
                            return documents
                        }
                    }
                }
            }
            def result = pipeline.execute(new StepContext(module))

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


