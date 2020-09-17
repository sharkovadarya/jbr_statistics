# jbr_statistics
Get activity statistics for JetBrains-Research on GitHub and display them on a webpage deployed using Docker.

## Build & run

First, please provide a GitHub login and a personal access token in `docker-compose.yml` in `GITHUB_LOGIN` and `GITHUB_OAUTH` arguments respectively. In order to find out how to generate a personal access token for GitHub, please visit [this](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token) page. (I am aware this is a terrible hack. I will try to fix this somehow?)
 
Then, use `docker-compose` to run in one command, like so: `docker-compose up`. Visit `localhost:8083` to see the results.
