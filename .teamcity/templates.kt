import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.Template

object DefaultTemplate : Template({
    name = "Default"

    vcs {
        root(DslContext.settingsRoot)
    }
})