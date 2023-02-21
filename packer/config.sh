#!/bin/bash

### Update ############################
sudo yum update -y

### JAVA setup ############################
echo "Installing Java"
sudo amazon-linux-extras enable java-openjdk11
sudo yum clean metadata && sudo yum install java-11-openjdk -y
echo "Java version"
java --version

### MAVEN setup ############################
echo "Installing MAVEN"
sudo wget https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven

### POSTGRES setup ############################
echo "Installing POSTGRES"
sudo amazon-linux-extras enable postgresql14
sudo yum clean metadata && sudo yum install postgresql -y

sudo yum install postgresql-server -y
sudo postgresql-setup initdb

sudo sed -i 87s/ident/md5/g /var/lib/pgsql/data/pg_hba.conf

sudo systemctl start postgresql
sudo systemctl enable postgresql

### CREATING DATABASE & USER ############################
#sudo -u postgres psql -c 'CREATE DATABASE userdatabase;'
#sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'chavan';"
echo "Creating DATABASE"
sudo -u postgres psql -c 'CREATE DATABASE userdatabase;'
sudo -u postgres psql -c "CREATE USER shivanichavan WITH PASSWORD 'Chavan123';"
sudo -u postgres psql -c 'GRANT ALL PRIVILEGES ON DATABASE userdatabase TO shivanichavan;'

##Creating a new folder webapp and moving the jar file in webapp
pwd
cd
cd ~/
sudo mkdir -m755 webapp
sudo mv /home/ec2-user/UserWebApp-0.0.1-SNAPSHOT.jar webapp/UserWebApp-0.0.1-SNAPSHOT.jar
cd webapp

##Moving the service file to systemd location
sudo mv /tmp/userWebApp.service /etc/systemd/system/userWebApp.service
sudo systemctl enable userWebApp.service
sudo systemctl start userWebApp.service


