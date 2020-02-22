import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

version = "2019.2"

project {
    // Disable editing settings via the UI, any modifications should be made here
    params.param("teamcity.ui.settings.readOnly", "true")

    // Register VCS root so it can be used by build types
    vcsRoot(PullRequestVcsRoot)

    // Register default build template
    template(DefaultTemplate)
    defaultTemplate = DefaultTemplate

    buildType {
        id("StagePassedIntake")
        name = "[Stage] Passed Intake"
        type = BuildTypeSettings.Type.COMPOSITE

        triggers {
            vcs {
                branchFilter = "+:<default>"
                perCheckinTriggering = true
                groupCheckinsByCommitter = true
                enableQueueOptimization = false
                triggerRules = "-:.teamcity/**"
            }
        }

        dependencies {
            snapshot(Intake_Test) {
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
        }
    }

    buildType {
        id("StagePullRequest")
        name = "[Stage] PR Ready to Merge"
        type = BuildTypeSettings.Type.COMPOSITE

        triggerOnPullRequest()
        publishStatusToGitHub()

        dependencies {
            snapshot(Intake_Test) {
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
        }
    }

    subProjectsOrder = listOf(Intake, ExhaustiveTesting)
}

object Intake : Project({
    name = "Intake Checks"

    buildTypesOrder = listOf(Intake_SanityCheck, Intake_Test)
})

object Intake_SanityCheck : BuildType({
    name = "Sanity Check"

    publishStatusToGitHub()

    steps {
        gradle {
            tasks = "classes testClasses"
        }
    }
})

object Intake_Test : BuildType({
    name = "Run Tests"

    publishStatusToGitHub()

    steps {
        gradle {
            tasks = "check"
        }
    }

    dependencies {
        snapshot(Intake_SanityCheck) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
    }
})


object ExhaustiveTesting : Project({
    name = "Exhaustive Testing"

    buildType(Periodic_Check)
})

object Periodic_Check : BuildType({
    name = "Periodic"

    steps {
        gradle {
            tasks = "check"
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
