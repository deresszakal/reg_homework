# FROM mysql:8.0.19
# FROM mysql:5.7.39-debian
FROM mariadb:10.3.36

RUN apt update -y
RUN apt install nano -y

ADD ./mysql/my.cnf /etc


