#  Dockerize APP
The code snippet you provided is a Dockerfile that describes the steps to build a Docker image for a Ruby application. Let's go through the Dockerfile and understand each step:

```
FROM ruby:3.0.6
```
This line specifies the base image for the Docker image, which is ruby:3.0.6. This means the resulting Docker image will be based on the Ruby 3.0.6 image.

```
RUN apt-get update
RUN apt-get install software-properties-common -y
```
These two lines update the package list and install the software-properties-common package using the package manager.

```
RUN apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xF1656F24C74CD1D8
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
RUN apt-get update
```
These lines add GPG keys for package repositories and update the package list again.

```
RUN apt-get install -y \
    build-essential \
    curl \
    libmariadb-dev-compat \
    libmariadb-dev \
    openssl \
    sqlite3 \
    libsqlite3-dev \
    vim \
    nodejs
```
This command installs several packages required for building and running the Ruby application. The packages include build-essential, curl, libmariadb-dev-compat, libmariadb-dev, openssl, sqlite3, libsqlite3-dev, vim, and nodejs.

```
RUN useradd -r -d /opt/postal -m -s /bin/bash -u 999 postal
USER postal
RUN mkdir -p /opt/postal/app /opt/postal/config
WORKDIR /opt/postal/app
```
These lines create a user named postal and set up the working directory at /opt/postal/app within the Docker image. The postal user is given a non-root system account for running the application.

```
RUN gem install bundler --no-doc
RUN bundle config frozen 1
RUN bundle config build.sassc --disable-march-tune-native
```
These commands install the bundler gem and configure it. The bundle config frozen 1 command ensures that the installed gems are frozen and cannot be modified later. The bundle config build.sassc --disable-march-tune-native command configures the sassc gem to disable native architecture optimization.

```
COPY Gemfile Gemfile.lock ./
RUN bundle install -j 4
```
These lines copy the Gemfile and Gemfile.lock files into the Docker image and run bundle install -j 4 to install the application dependencies specified in those files. The -j 4 option specifies parallel installation using four threads.

```
COPY --chown=postal . .
RUN mkdir -p tmp/pids/
```
These lines copy the application source code into the Docker image, setting the ownership to the postal user. It also creates a tmp/pids/ directory within the application.

```
EXPOSE 3000
```
This line exposes port 3000, indicating that the application inside the Docker container listens on that port.

```
CMD ["bundle", "exec", "puma", "-C", "config/puma.rb"]
```
This command specifies the default command to run when the Docker container launches. It runs the puma command with bundle exec, using the config/puma.rb configuration file.

Overall, this Dockerfile sets up a Ruby environment, installs dependencies, configures the application, and defines the command to start the application when the container is launched.






