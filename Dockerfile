# مرحله اول: Build پروژه
FROM gradle:8.13-jdk21 AS build
COPY . /home/app
WORKDIR /home/app

RUN chmod +x ./gradlew
RUN ./gradlew build --no-daemon -x test

# مرحله دوم: اجرای فایل jar
FROM eclipse-temurin:21
EXPOSE 9090
COPY --from=build /home/app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

