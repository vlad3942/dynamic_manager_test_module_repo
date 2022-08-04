import com.cellpointdigital.dynamic.logging.DynamicLoggingStrategy
import com.cellpointdigital.dynamic.runner.DynamicModuleHolder

def module = DynamicModuleHolder.get()


import groovy.util.logging.Slf4j

@Slf4j(loggingStrategy = DynamicLoggingStrategy)
class Sss {

}

println("hello world: " + module.getModuleId())
