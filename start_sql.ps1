$Env:MYSQL_ROOT_PASSWORD = "root"
$Env:MYSQL_USER = "reg"
$Env:MYSQL_PASSWORD = "reg"
$Env:PMA_HOST = "mysql-server"
$Env:PMA_USER = "root"
$Env:PMA_PASSWORD = "root"
cd .\docker\reghw
docker-compose -f docker-compose-sql.yaml up -d
cd ..\..