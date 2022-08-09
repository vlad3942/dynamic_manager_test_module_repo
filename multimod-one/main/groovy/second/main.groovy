@groovy.transform.BaseScript(com.cellpointdigital.amgateway.dsl.AmadeusScript)
package second


import groovy.json.JsonBuilder

http {

    configuration('') {
        port = 8099
    }

    on get, "/authenticate/{clientId}", {
        when {
            println("when 2")
            parameters.clientId == "10102"
        }
        execute {
            def builder = new JsonBuilder()
            builder.item {
                id parameters.clientId
            }
            responseEntity = builder
        }
    }

}
