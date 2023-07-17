FROM ruby:3.0.6

RUN apt-get update
RUN apt-get install software-properties-common -y

# Setup additional repositories
RUN apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xF1656F24C74CD1D8
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
RUN apt-get update

# Install main dependencies
RUN apt-get install -y \
    build-essential  \
    curl \
    libmariadb-dev-compat \
    libmariadb-dev \
    openssl \
    sqlite3 \ 
    libsqlite3-dev \
    vim \
    nodejs



# Setup an application
RUN useradd -r -d /opt/postal -m -s /bin/bash -u 999 postal
USER postal
RUN mkdir -p /opt/postal/app /opt/postal/config
WORKDIR /opt/postal/app
    

# Install bundler
RUN gem install bundler --no-doc
RUN bundle config frozen 1
RUN bundle config build.sassc --disable-march-tune-native


# Install the latest and active gem dependencies and re-run
# the appropriate commands to handle installs.
COPY Gemfile Gemfile.lock ./
RUN bundle install -j 4

# Copy the application (and set permissions)
COPY --chown=postal . .
RUN mkdir -p tmp/pids/

# Make port 3000 available to the world outside this container
EXPOSE 3000

# Define environment variable
# ENV RAILS_ENV=production
# ENV RACK_ENV=production

# Run when the container launches
CMD ["bundle", "exec", "puma", "-C", "config/puma.rb"]
