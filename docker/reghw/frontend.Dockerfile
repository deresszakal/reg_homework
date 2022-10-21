FROM node:18

WORKDIR /app

RUN apt-get update -y
RUN apt-get install nano -y

ADD frontend/time-series-app/node_modules ./node_modules
ADD frontend/time-series-app/public ./public
ADD frontend/time-series-app/src ./src

ADD frontend/time-series-app/package-lock.json .
ADD frontend/time-series-app/tsconfig.json .
ADD frontend/time-series-app/package.json .

# RUN export NODE_OPTIONS=--openssl-legacy-provider

RUN npm install

EXPOSE 3999
ENTRYPOINT ["npm", "run", "start-in-docker"]
