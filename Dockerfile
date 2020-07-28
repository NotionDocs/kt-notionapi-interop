# We select the base image from. Locally available or from https://hub.docker.com/
FROM openjdk:12

# We define the user we will use in this instance to prevent using root that even in a container, can be a security risk.
ENV APPLICATION_USER ktor

# Then we add the user, create the /app folder and give permissions to our user.
RUN mkdir /app
# RUN chown -R $APPLICATION_USER /app
COPY . /app
WORKDIR /app

# Marks this container to use the specified $APPLICATION_USER
# USER $APPLICATION_USER

RUN ./gradlew build --stacktrace

# We launch java to execute the jar, with good defauls intended for containers.
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "my-application.jar"]