# Start with a base image containing Java runtime

FROM eclipse-temurin:11-jre-alpine

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

# Create non-privileged user directory, and make it as workable directory
RUN mkdir -p /home/nobody/app && mkdir -p /home/nobody/data/log/dataapp && mkdir -p /home/nobody/data/datalake && mkdir -p /.sigstore
RUN apk add --no-cache openssl curl cosign

WORKDIR /home/nobody

# The application's jar file
# ARG JAR_FILE=target/*.jar
COPY target/dependency-jars /home/nobody/app/dependency-jars

# Add the application's jar to the container
ADD target/application.jar /home/nobody/app/application.jar

# Change ownership of non-privileged user directory and logs directories
RUN chown -R nobody:nogroup /home/nobody && chown -R nobody:nogroup /.sigstore 

# Set non-privileged user to run commands
USER 65534

# Run the jar file 
ENTRYPOINT java -jar /home/nobody/app/application.jar

# Healthy Status
HEALTHCHECK --interval=5s --retries=12 --timeout=10s CMD curl --fail -k https://localhost:8183/about/version || exit 1