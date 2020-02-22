import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/**
 * Enables [status publishing](https://www.jetbrains.com/help/teamcity/?Commit+Status+Publisher) to GitHub.
 */
fun BuildType.publishStatusToGitHub() {
    features {
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:0f60167b-3e37-4683-804e-fdbf52a8dd0a"
                }
            }
        }
    }
}

/**
 * Triggers this build when a pull request is created or updated.
 */
fun BuildType.triggerOnPullRequest() {
    vcs {
        root(PullRequestVcsRoot)
    }

    triggers {
        vcs {
            branchFilter = "+:pull/*"
            enableQueueOptimization = true
            triggerRules = "-:.teamcity/**"
        }
    }

    features {
        pullRequests {
            vcsRootExtId = "${PullRequestVcsRoot.id}"
            provider = github {
                authType = vcsRoot()
                filterTargetBranch = "refs/heads/${PullRequestVcsRoot.paramRefs["branch"]}"
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
            }
        }
    }
}