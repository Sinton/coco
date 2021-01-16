FROM centos:7.2.1511

# author
MAINTAINER Sinton<https://github.com/Sinton>

WORKDIR /data

ADD ./Coco.jar ./Coco.jar
ADD ./docker-entrypoint.sh ./docker-entrypoint.sh

RUN yum install -y unzip && \
    yum clean all && \
    # download docker-compose
    DOCKER_COMPOSE_VERSION=1.27.4 && \
    DOCKER_COMPOSE_PROJECT_DOWNLOAD_URL="https://github.com/docker/compose/releases/download"
    curl -L "${DOCKER_COMPOSE_PROJECT_DOWNLOAD_URL}/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && \
    chmod a+x /usr/local/bin/docker-compose
    # download arthas
    ARTHAS_VERSION=3.4.6 && \
    ARTHAS_PROJECT_DOWNLOAD_URL="https://github.com/alibaba/arthas/releases/download" && \
    curl -L "${ARTHAS_PROJECT_DOWNLOAD_URL}/arthas-all-${ARTHAS_VERSION}/arthas-bin.zip" -o /usr/local/arthas-bin.zip && \
    unzip /usr/local/arthas-bin.zip -d /usr/local/arthas && \
    rm -rf /usr/local/arthas-bin.zip

# api server port
EXPOSE 8080
# socket server port
EXPOSE 9099

ENTRYPOINT ["./docker-entrypoint.sh"]
