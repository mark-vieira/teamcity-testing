package patches.projects

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the project with id = 'Intake'
accordingly, and delete the patch script.
*/
changeProject(RelativeId("Intake")) {
    check(archived == false) {
        "Unexpected archived: '$archived'"
    }
    archived = true
}