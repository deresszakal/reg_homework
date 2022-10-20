#FROM nginx:1.19.0
#FROM node:14-alpine
FROM ubuntu
#WORKDIR /usr/share/nginx/html
WORKDIR /app

RUN apt update -y
RUN apt install nano -y
RUN apt install nodejs npm -y

ADD frontend/time-series-app/package.json .
ADD frontend/time-series-app/package-lock.json .
ADD frontend/time-series-app/tsconfig.json .

ADD frontend/time-series-app/src ./src
ADD frontend/time-series-app/public ./public
ADD frontend/time-series-app/node_modules ./node_modules

RUN npm install

EXPOSE 3999
ENTRYPOINT ["npm", "start"]
