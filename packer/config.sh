#!/bin/bash

### Update ############################
echo "Updating & upgrading packages on Amazon_Linux_2"
sudo yum update -y
sudo yum upgrade -y

### JAVA setup ############################
echo "Installing Java"
sudo amazon-linux-extras enable java-openjdk11
sudo yum clean metadata && sudo yum install java-11-openjdk -y
echo "Java version"
java --version

### POSTGRES setup ############################
echo "Installing POSTGRES Client psql"
sudo amazon-linux-extras enable postgresql14
sudo yum clean metadata && sudo yum install postgresql -y

#echo "Initializing initial postgres DATABASE"
#sudo yum install postgresql-server -y
#sudo postgresql-setup initdb
#sudo sed -i 87s/ident/md5/g /var/lib/pgsql/data/pg_hba.conf

#echo "Enabling systemd service for POSTGRES"
#sudo systemctl start postgresql
#sudo systemctl enable postgresql

### CREATING DATABASE & USER ############################
#sudo -u postgres psql -c 'CREATE DATABASE userdatabase;'
#sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'chavan';"

#This works
#echo "Creating DATABASE & USER"
#sudo -u postgres psql -c 'CREATE DATABASE userdatabase;'
#sudo -u postgres psql -c "CREATE USER shivanichavan WITH PASSWORD 'Chavan123';"
#sudo -u postgres psql -c 'GRANT ALL PRIVILEGES ON DATABASE userdatabase TO shivanichavan;'

echo "Transferring Jar File & changing permissions to read and execute"
sudo mv /tmp/UserWebApp-0.0.1-SNAPSHOT.jar /usr/bin/UserWebApp-0.0.1-SNAPSHOT.jar
sudo chmod 544 /usr/bin/UserWebApp-0.0.1-SNAPSHOT.jar

##Moving the service file to systemd location
echo "Enabling Application as a Linux systemd service for auto-start on reboot"
sudo mv /tmp/userWebApp.service /etc/systemd/system/userWebApp.service
sudo systemctl daemon-reload
sudo systemctl enable userWebApp.service
#sudo systemctl start userWebApp.service


