import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

object PullRequestVcsRoot : GitVcsRoot({
    id("PullRequest")
    name = "teamcity-testing-pull-request"
    url = "https://github.com/mark-vieira/teamcity-testing.git"
    branch = "${DslContext.settingsRoot.paramRefs["branch"]}"
    branchSpec = "+:refs/heads/(${DslContext.settingsRoot.paramRefs["branch"]})"
    authMethod = password {
        userName = "mark-vieira"
        password = "credentialsJSON:0f60167b-3e37-4683-804e-fdbf52a8dd0a"
    }
})