import static groovy.json.JsonOutput.*

class ConfigProcessor implements Serializable {

    protected dslFactory
    private final String importDirectory = "./"

    public ConfigProcessor() {
    }

    public ConfigProcessor(aFactory) {
        this.dslFactory = aFactory
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
    static def clone(def thisConfig) {
        def aCopy = [:]
        thisConfig.each { ck, cv ->
            if (cv instanceof Map) {
                aCopy."${ck}" = [:]
                aCopy."${ck}" << cv
            } else {
                aCopy."${ck}" = cv
            }
        }
        return aCopy
    }    
    private def validate(def config) {
        config.strictFields?.fields?.each { field ->
            def fieldParent = field.tokenize('.')[0]
            def fieldKey = field.tokenize('.')[1]
            if (!config."${fieldParent}"."${fieldKey}") {
                throw new IllegalStateException("Null strict field: ${field}")
            }
        }
        return config
    }
    void prettyPrint(def jc) {
        def folderedBaseName
        folderedBaseName = [
                jc.'folder.project'?: "",
                jc.'folder.jobType'?: "",
                jc.'job.baseName'].findAll { it != null && it.toString().length() != 0 }.join("/")
                
        def jobName = folderedBaseName

        def header = "======>"
        def footer = '=' * ((jobName.size() + header.size() + 1))
        def content = ["Job Class": jc.'jobClass.baseClassName']

        switch (jc.'folder.jobType') {
            case "Build": content.put("Source", "${jc.'github.org'}/${jc.'github.repo'}"); break;
            case "Deploy":
            case "Maintenance":
                content.putAll(["Environments": jc.'environment.regex'])
                if ("SRADeploy".equals(jc.'jobClass.baseClassName')) {
                    content.putAll(["Projects": groovy.json.JsonOutput.toJson(jc.'artifacts.items')])
                } else {
                    content.putAll(["Application GID": jc.'artifacts.deployGroupId'])

                }
                content.putAll(["Workflow Script": "${jc.'job.workflowDirectory'}/${jc.'job.workflowFile'}"])
                break;
            case "Promotion":
                if ("ImagePromote".equals(jc.'jobClass.baseClassName')) {
                    content.put("Image regex", jc.'openshift.imageRegex')
                } else {
                    content.put("Artifacts", jc.'artifacts.artifactGroups')
                }
                break;
            case "Pipeline": content.putAll(["Environment": jc.'job.fqaEnv' ?: jc.'job.ciEnv']); break;
        }


        def outContent = ""
        content.each { k, v -> outContent += "${k}: ${v}\n" }

        dslFactory.out.println("${header} ${jobName}")
        dslFactory.out.println(outContent)
        dslFactory.out.println(footer)

    }    
}
    