import static groovy.json.JsonOutput.*

class ConfigProcessor implements Serializable {

    protected dslFactory
    private final String importDirectory = "./"

    public ConfigProcessor() {
    }

    public ConfigProcessor(aFactory) {
        this.dslFactory = aFactory
        this.isProductionSeed = isProductionSeed
        this.moduleName = aModuleName
        this.projectVersion = aProjectVersion
    }    
    public def processConfig(path) {
        def cfText = this.dslFactory.readFileFromWorkspace(path.toString())
        def config = new ConfigSlurper().parse(cfText)

        def commonChildFields =  config.findAll { it.key.startsWith("__") }
        commonChildFields.each {
            field ->
                config.remove(field.key)
        }

        def baseConfig = applyImportList([:], config)
        if (config.keySet().contains("common")) {
            baseConfig = applyConfigObject(baseConfig, config.common)
        }
        baseConfig['commonChildFields'] = commonChildFields

        def result = []
        config.findAll { key, _ -> !(key in ["imports", "common"]) }.each { k, v ->
            def jc = applyConfigObject(clone(baseConfig), v)
            def jc_path = path.toString()
            jc.github.url = "https://${jc.github.host}"
            jc.job.baseName = k
            jc.job.isProductionSeed = isProductionSeed
            jc.job.dslSha = projectVersion.tokenize('.')[3].split("-SNAPSHOT").first()
            jc.devops.projectVersion = projectVersion
            jc.devops.artifactId = moduleName
            
            jc.remove('commonChildFields')

            def flat = (new ConfigObject(validate(jc) as Map).flatten() as Map)
            result << flat
        }



        return result
    }
    private def applyImportList(def baseConfig, def importSourceConfig) {
        importSourceConfig.imports?.each { importObject ->
            def overrideConfig
            if (importObject.startsWith("__")) {
                overrideConfig = baseConfig.commonChildFields.find {
                    commonChild ->
                        commonChild.key == importObject
                }
                if (overrideConfig == null) {
                    throw new NoSuchFieldException("No such import field: ${importObject}")
                }
                baseConfig = applyConfigObject(baseConfig, overrideConfig?.value)
            } else {
                overrideConfig = this.dslFactory.readFileFromWorkspace("${importDirectory}/${importObject}")
                baseConfig = applyRawConfig(baseConfig, overrideConfig)
            }
        }
        return baseConfig
    }    
    private def applyRawConfig(def configInstance, def configFileText) {
        if (configFileText) {
            def newConfig = new ConfigSlurper().parse(configFileText)
            applyConfigObject(configInstance, newConfig)
        }
        return configInstance
    }

    private def applyConfigObject(def baseConfig, def overrideConfig) {
        if (overrideConfig != null) {
            baseConfig = applyImportList(baseConfig, overrideConfig)

            overrideConfig.findAll { k, _ -> !(k in ["imports"]) }.each { key, valueForKey ->
                if (baseConfig."${key}" == null) {
                    baseConfig."${key}" = [:]
                }
                if (key == "strictFields") {
                    if (!baseConfig.strictFields.containsKey("fields")) {
                        baseConfig.strictFields.fields = []
                    }
                    baseConfig.strictFields.fields += valueForKey.fields
                } else {
                    baseConfig."${key}" << valueForKey
                }
            }
        }
        return baseConfig
    }
}
    