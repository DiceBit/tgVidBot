#FROM maven:3.8.1-openjdk-17
#
#COPY src /home/app/src
#COPY pom.xml /home/app
#
#RUN mvn -f /home/app/pom.xml package
#
#WORKDIR /usr/local/lib
#
#COPY --from=build /home/app/target/*.jar .
#
#ENTRYPOINT ["java", "-jar", "*.jar"]
#
#CMD ["*.jar"]
#
#LABEL name="download-video-from-youtube"
#LABEL version="1.0.0"

#FROM maven:3.8.1-openjdk-17
#
#LABEL name="download-video-from-youtube"
#LABEL version="1.0.0"
#
#RUN mvn -f /Users/danii/IdeaProjects/DownloadVidFromYTBot/pom.xml package
#ARG JAR_FILE=target/*.jar
#ENV BOT_NAME=VidDownloadBot
#ENV BOT_TOKEN=6771128851:AAE8liWD9f2hkmmS4VG6WYUiWip0agTzth4
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java", "-Dbot.username=${BOT_NAME}", "-Dbot.token=${BOT_TOKEN}", "-jar", "/app.jar"]

# Use the official maven/Java image to create a build artifact.
FROM maven:3.8.1-openjdk-17 as builder
# Set the working directory in the builder container
WORKDIR /workspace/app
# Copy the pom.xml file to download dependencies
COPY pom.xml .
# Download the dependencies
RUN mvn dependency:go-offline
# Copy the rest of the working directory contents into the container at /workspace/app
COPY src ./src
# Build a JAR file
RUN mvn package -DskipTests

# Use OpenJDK to run the JAR
FROM openjdk:17-jdk-alpine
# Set the working directory in the final container
WORKDIR /app
# Copy the jar file from the builder to the final container
COPY --from=builder /workspace/app/target/*.jar app.jar
# Set necessary environment variables
ENV BOT_USERNAME=VidDownloadBot
ENV BOT_TOKEN=6771128851:AAE8liWD9f2hkmmS4VG6WYUiWip0agTzth4

# Run the application
ENTRYPOINT ["java", "-Dbot.username=${BOT_USERNAME}", "-Dbot.token=${BOT_TOKEN}", "-jar", "/app.jar"]