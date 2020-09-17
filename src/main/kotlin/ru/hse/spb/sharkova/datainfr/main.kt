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

// obvious flaws: gets response code 202 frequently and does not handle it in any manner
fun main(args: Array<String>) {
    val oauth = args[0]
    val login = args[1]

    try {
        val github = GitHubBuilder.fromEnvironment().withOAuthToken(oauth, login).build()
        val user = github.getUser("JetBrains-Research")
        val repositories = user.repositories
        val i = OffsetDateTime.now(ZoneOffset.UTC)
        val lastWeek = i.minusDays(i.dayOfWeek.value.toLong()).truncatedTo(ChronoUnit.DAYS)
        var commitsOverLastWeek = 0
        var reposWithCommitsOverLastWeek = 0

        repositories.forEach { (_, repo) ->
            val commitActivity = repo.statistics.commitActivity
            val lastWeekCommits = commitActivity.filter { it.week == lastWeek.toEpochSecond() }
            if (lastWeekCommits.isNotEmpty()) {
                reposWithCommitsOverLastWeek++
                lastWeekCommits.forEach {
                    commitsOverLastWeek += it.total
                }
            }
        }

        FileWriter("index.html").appendHTML().html {
            head {  }
            body {
                p { +"${LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId())}" }
                p { +"$commitsOverLastWeek commits over the last week in $reposWithCommitsOverLastWeek repositories." }
            }
        }.flush()
    } catch (e: HttpException) {
        FileWriter("index.html").appendHTML().html {
            head {  }
            body {
                p { +"Impossible to retrieve information. Check your Internet connection." }
            }
        }.flush()
    }
}