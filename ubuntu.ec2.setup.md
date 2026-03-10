# Spring Boot + Maven Server Setup (Ubuntu EC2)

This guide explains how to configure a fresh **Ubuntu EC2 instance** to run a **Spring Boot application using Maven** behind **Nginx**.

## 1. Connect to the EC2 Instance

```bash
ssh ubuntu@YOUR_EC2_PUBLIC_IP
```

Update the system:

```bash
sudo apt update
sudo apt upgrade -y
```

---

# 2. Install Required Packages

Install base utilities:

```bash
sudo apt install -y \
git \
curl \
wget \
unzip \
build-essential
```

---

# 3. Install Java (Required for Spring Boot)

Spring Boot requires Java.

Install OpenJDK 21:

```bash
sudo apt install openjdk-21-jdk -y
```

Verify installation:

```bash
java --version
```

---

# 4. Install Maven

Maven is used to build and run the Spring Boot project.

```bash
sudo apt install maven -y
```

Verify installation:

```bash
mvn --version
```

---

# 5. Install Nginx (Reverse Proxy)

Nginx will serve HTTPS traffic and proxy requests to Spring Boot.

```bash
sudo apt install nginx -y
```

Start and enable Nginx:

```bash
sudo systemctl start nginx
sudo systemctl enable nginx
```

Verify Nginx:

```
http://YOUR_EC2_IP
```

---

# 6. Install Certbot (SSL)

For HTTPS certificates:

```bash
sudo apt install certbot python3-certbot-nginx -y
```

---

# 7. Clone Your Project

Clone the repository containing the Spring Boot project:

```bash
git clone YOUR_REPOSITORY_URL
cd YOUR_PROJECT_DIRECTORY
```

---

# 8. Build the Spring Boot Application

Build using Maven:

```bash
mvn clean package
```

The compiled `.jar` file will be created in:

```
target/
```

---

# 9. Run the Spring Boot Server

Run the application:

```bash
java -jar target/<app-name>.jar
```

The application should now run on:

```
http://localhost:8080
```

---

# 10. Configure Nginx Reverse Proxy

Edit the Nginx configuration (replace it with `nginx.default.conf` contents):

```bash
sudo nano /etc/nginx/sites-available/default
```

Test the configuration:

```bash
sudo nginx -t
```

Reload Nginx:

```bash
sudo systemctl reload nginx
```

---

# 11. Enable HTTPS

Request a certificate:

```bash
sudo certbot --nginx -d your-domain.com
```

Test automatic renewal:

```bash
sudo certbot renew --dry-run
```

---

# 12. Useful Commands

Restart nginx:

```bash
sudo systemctl restart nginx
```

Check nginx logs:

```bash
sudo tail -f /var/log/nginx/error.log
```

Check running processes:

```bash
ps aux | grep java
```

Kill running server:

```bash
kill -9 PID
```

---
