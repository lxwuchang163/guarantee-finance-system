FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="guarantee-finance"
LABEL description="担保集团业务财务系统后端服务"

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

ARG JAR_FILE=target/guarantee-finance-backend-1.0.0.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]
