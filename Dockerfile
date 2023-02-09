# Start with a base image containing Java runtime

FROM eclipse-temurin:11-jre-alpine

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

# Install whois service
# RUN yum install -y whois

# Add a volume pointing to /tmp
#VOLUME /tmp 

# Make port 8083 available to the world outside this container
EXPOSE 8083

# Create non-privileged user directory, and make it as workable directory
RUN mkdir -p /home/nobody/app
WORKDIR /home/nobody

# Create directory for logs
RUN mkdir /var/log/dataapp

# The application's jar file
# ARG JAR_FILE=target/*.jar
COPY target/dependency-jars /home/nobody/app/dependency-jars

# Add the application's jar to the container
# ADD ${JAR_FILE} market4.0-data-app.jar
ADD target/application.jar /home/nobody/app/application.jar

# Change ownership of non-privileged user directory and logs directories
RUN chown -R nobody:nogroup /home/nobody
RUN chown -R nobody:nogroup /var/log/dataapp

# Set non-privileged user to run commands
USER 65534

# Run the jar file 
ENTRYPOINT java -jar /home/nobody/app/application.jar

# Healthy Status
HEALTHCHECK --interval=5s --retries=12 --timeout=10s CMD curl --fail -k https://localhost:8083/actuator/health || CMD curl --fail -k https://localhost:9000/actuator/health || exit 1