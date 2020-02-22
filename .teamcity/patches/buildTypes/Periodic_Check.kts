package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Periodic_Check'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Periodic_Check")) {
    triggers {
        val trigger1 = find<ScheduleTrigger> {
            schedule {
                schedulingPolicy = cron {
                    hours = "0/8"
                }
                triggerBuild = always()
                withPendingChangesOnly = false
            }
        }
        trigger1.apply {
            param("revisionRule", "lastSuccessful")
            param("revisionRuleDependsOn", "TeamcityTesting_Master_Intake_Test")
        }
    }
}
