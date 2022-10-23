FROM mariadb:10.3.36

RUN apt update -y
RUN apt install nano -y

ADD ./mysql/my.cnf /etc
