package ru.hse.spb.sharkova.datainfr

import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.appendHTML
import org.kohsuke.github.*
import java.io.FileWriter
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

// obvious flaws: gets response code 202 frequently and does not handle it in any manner
fun main() {
    val startTime = Instant.now()

    try {
        val github = GitHubBuilder.fromEnvironment().withOAuthToken("2f7c048d66aa4f95fbe3b73704c9a20d7dca1ef7", "sharkovadarya").build()
        val user = github.getUser("JetBrains-Research")
        val repositories = user.repositories
        val i = OffsetDateTime.now(ZoneOffset.UTC)
        val lastWeek = i.minusDays(i.dayOfWeek.value.toLong()).truncatedTo(ChronoUnit.DAYS)
        var commitsOverLastWeek = 0
        var reposWithCommitsOverLastWeek = 0
        var lastCommit: GHCommit? = null
        var issuesClosedLastWeek = 0
        var lastClosedIssue: GHIssue? = null

        repositories.forEach { (_, repo) ->
            val commitActivity = repo.statistics.commitActivity
            val lastWeekCommits = commitActivity.filter { it.week == lastWeek.toEpochSecond() }
            if (lastWeekCommits.isNotEmpty()) {
                reposWithCommitsOverLastWeek++
                lastWeekCommits.forEach {
                    commitsOverLastWeek += it.total
                }
            }

            val repoCommits = repo.listCommits()
            val repoLastCommit = repoCommits.first()
            if (lastCommit == null || repoLastCommit.authoredDate > lastCommit!!.authoredDate) {
                lastCommit = repoLastCommit
            }
            val closedIssues = repo.listIssues(GHIssueState.CLOSED)
            val repoIssuesClosedLastWeek = closedIssues.filter {
                val closedDate = LocalDate.from(it.closedAt.toInstant().atZone(ZoneId.of("UTC")))
                closedDate.minusDays(closedDate.dayOfWeek.value.toLong()) == lastWeek.toLocalDate()
            }
            issuesClosedLastWeek += repoIssuesClosedLastWeek.size
            if (closedIssues.toList().isNotEmpty() && (lastClosedIssue == null || closedIssues.last().closedAt > lastClosedIssue?.closedAt)) {
                lastClosedIssue = closedIssues.last()
            }
        }

        FileWriter("index.html").appendHTML().html {
            head {  }
            body {
                p { +"${LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId())}" }
                p { +"$commitsOverLastWeek commits over the last week in $reposWithCommitsOverLastWeek repositories." }
                if (lastCommit != null) {
                    p {
                        +("Last commit authored by ${lastCommit!!.author.login} to repository ${lastCommit!!.owner.name}" +
                                " on ${lastCommit!!.authoredDate}.")
                    }
                }
                p { +"$issuesClosedLastWeek issues closed over the last week in all repositories." }
                if (lastClosedIssue != null) {
                    val issue = lastClosedIssue!!
                    p {
                        +("Last issue closed: ${issue.title}, " +
                                "assigned to ${issue.assignees.joinToString(", ") { it.login }}, " +
                                if (issue.closedBy != null) "closed by ${issue.closedBy.login} " else "" +
                                        "in repository ${issue.repository.name} on ${issue.closedAt}.")
                    }
                }
            }
        }.flush()

        println(TimeUnit.SECONDS.toMinutes(Instant.now().minusSeconds(startTime.epochSecond).epochSecond))
    } catch (e: HttpException) {
        FileWriter("index.html").appendHTML().html {
            head {  }
            body {
                p { +"Impossible to retrieve information. Check your Internet connection." }
            }
        }.flush()
    }
}