FROM java:7
MAINTAINER ZhaoYongChun "zyc@hasor.net"

# maven
ENV MAVEN_VERSION 3.3.9
RUN curl -fsSL http://project.hasor.net/hasor/develop/tools/apache/maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
        && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
        && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
RUN mkdir -p "/home/repo" && \
    sed -i '/<!-- localRepository/i\<localRepository>/home/repo</localRepository>' $MAVEN_HOME/conf/settings.xml

#
# work
ENV EXAMPLE_HOME /usr/rsfcenter
ENV WORK_HOME /usr/rsfcenter/worker
ADD . /usr/rsfcenter/src
EXPOSE 2180
EXPOSE 2181

# === project ===
WORKDIR /usr/rsfcenter/src
RUN ./build.sh && \
    cd `find ./build -name 'bin'` && \
    cp -R ../* $EXAMPLE_HOME

WORKDIR $EXAMPLE_HOME
CMD ["$EXAMPLE_HOME/bin/run.sh"]