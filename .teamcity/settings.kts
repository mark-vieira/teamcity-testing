import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

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

    vcsRoot(TeamcityTestingMaster)
    vcsRoot(TeamcityTesting1x)
    subProjectsOrder = arrayListOf(RelativeId("Master"), RelativeId("1x"))
}

object TeamcityTesting1x : GitVcsRoot({
    name = "teamcity-testing-1.x"
    url = "https://github.com/mark-vieira/teamcity-testing.git"
    branch = "1.x"
    authMethod = password {
        userName = "mark-vieira"
        password = "credentialsJSON:0f60167b-3e37-4683-804e-fdbf52a8dd0a"
    }
})

object TeamcityTestingMaster : GitVcsRoot({
    name = "teamcity-testing-master"
    url = "https://github.com/mark-vieira/teamcity-testing.git"
    branch = "master"
    authMethod = password {
        userName = "mark-vieira"
        password = "credentialsJSON:0f60167b-3e37-4683-804e-fdbf52a8dd0a"
    }
})
