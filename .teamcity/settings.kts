import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {
    buildType {
        id("StagePassedIntake")
        name = "[Stage] Passed Intake"
        type = BuildTypeSettings.Type.COMPOSITE

        dependencies {
            snapshot(Intake_Test) {
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
        }
    }

    buildType {
        id("StagePullRequest")
        name = "[Stage] Pull Request"
        type = BuildTypeSettings.Type.COMPOSITE

        features {
            pullRequests {
                provider = github {
                    authType = vcsRoot()
                    filterTargetBranch = "refs/heads/${DslContext.settingsRoot.paramRefs["branch"]}"
                    filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
                }
            }
            commitStatusPublisher {
                publisher = github {
                    githubUrl = "https://api.github.com"
                    authType = personalToken {
                        token = "credentialsJSON:0f60167b-3e37-4683-804e-fdbf52a8dd0a"
                    }
                }
            }
        }

        dependencies {
            snapshot(Intake_Test) {
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
        }
    }

    subProject(Periodic)
    subProject(Intake)
}


object Intake : Project({
    name = "Intake Checks"

    buildType(Intake_Test)
    buildType(Intake_SanityCheck)
})

object Intake_SanityCheck : BuildType({
    name = "Sanity Check"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "classes testClasses"
            buildFile = ""
        }
    }
})

object Intake_Test : BuildType({
    name = "Run Tests"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "check"
            buildFile = ""
        }
    }

    triggers {
        vcs {
            branchFilter = "-:refs/pull/*"
            perCheckinTriggering = true
            groupCheckinsByCommitter = true
            enableQueueOptimization = false
            triggerRules = "-:.teamcity/**"
        }
    }

    dependencies {
        snapshot(Intake_SanityCheck) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
    }
})


object Periodic : Project({
    name = "Periodic Checks"

    buildType(Periodic_Check)
})

object Periodic_Check : BuildType({
    name = "Hourly Periodic"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "check"
            buildFile = ""
        }
    }

    triggers {
        schedule {
            schedulingPolicy = cron {
                minutes = "0/10"
            }
            triggerBuild = onWatchedBuildChange {
                buildType = "TeamcityTesting_Master_Intake_Test"
                watchedBuildRule = ScheduleTrigger.WatchedBuildRule.LAST_SUCCESSFUL
                watchedBuildTag = ""
                watchedBuildBranchFilter = "+:<default>"
                promoteWatchedBuild = true
            }
            withPendingChangesOnly = false
            branchFilter = "+:<default>"
        }
    }

    dependencies {
        snapshot(Intake_Test) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }
})
