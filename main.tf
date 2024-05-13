terraform {
  required_providers {
    docker = {
      source = "kreuzwerker/docker"
      version = "3.0.2"
    }
  }
}

provider "docker" {
  host = "unix:///var/run/docker.sock"
}

variable "SPRING_APP_VERSION" {
  type = string
}

variable "IMAGE_URL" {
  default = "jack.hc-sc.gc.ca/devops/devops-training-w24/docker-compose-dir/"
  type = string
}

# Null resource to stop and remove all resources
resource "null_resource" "remove_resources" {
  provisioner "local-exec" {
    command = <<-EOT
      docker ps -a
      docker images
      docker stop $(docker ps -a -q) || true
      docker rm $(docker ps -a -q) || true
      docker ps -a
      docker images
      docker network ls
      echo "y" | docker system prune -a --volumes
    EOT
  }
}

# Build, tag and push Docker images to artifactory
resource "null_resource" "docker_images" {
  provisioner "local-exec" {
    command = <<-EOT

      cd db

      docker build --no-cache -t db:${var.SPRING_APP_VERSION} .
      docker tag db:${var.SPRING_APP_VERSION} ${var.IMAGE_URL}db:${var.SPRING_APP_VERSION}
      docker push ${var.IMAGE_URL}db:${var.SPRING_APP_VERSION}

      cd ..

      cd backend

      docker build --no-cache -t backend:${var.SPRING_APP_VERSION} .
      docker tag backend:${var.SPRING_APP_VERSION} ${var.IMAGE_URL}backend:${var.SPRING_APP_VERSION}
      docker push ${var.IMAGE_URL}backend:${var.SPRING_APP_VERSION}

      cd ..

      cd frontend

      docker build --no-cache -t frontend:${var.SPRING_APP_VERSION} .
      docker tag frontend:${var.SPRING_APP_VERSION} ${var.IMAGE_URL}frontend:${var.SPRING_APP_VERSION}
      docker push ${var.IMAGE_URL}frontend:${var.SPRING_APP_VERSION}

      cd ..

      cp haproxy.cfg /var/opt/devops/ops/haproxy.cfg
      docker create --name haproxy \
          -v /var/opt/devops/ops/haproxy.cfg:/usr/local/etc/haproxy/ha-body.cfg:ro \
          -v /var/opt/devops/ops/certs.d/:/usr/local/etc/haproxy/certs.d:ro \
          -p 80:80 \
          -p 443:443 \
          -p 127.0.0.1:8400:8400 \
          jack.hc-sc.gc.ca/base/haproxy:5.0.118-http
    EOT
  }
  depends_on = [
    null_resource.remove_resources
  ]
}

# Docker Network Configuration
resource "null_resource" "create_app_network" {
  provisioner "local-exec" {
    command = <<-EOT
      docker network create app_network || true
  EOT
  }
  depends_on = [
    null_resource.docker_images
  ]
}

# Docker Volume Configuration
resource "docker_volume" "db_data" {
  name = "db_data"
  depends_on = [
    null_resource.docker_images
  ]
}

# Create db container 
resource "null_resource" "db_container" {
  triggers = {
    image = "${var.IMAGE_URL}/db:${var.SPRING_APP_VERSION}"
  }

  provisioner "local-exec" {
    command = <<-EOT
    docker run -d \
      --name db \
      --network app_network \
      -e MYSQL_DATABASE=Expendi \
      -e MYSQL_USER=sa \
      -e MYSQL_PASSWORD=password \
      -e MYSQL_ROOT_PASSWORD=password \
      -v ${docker_volume.db_data.name}:/var/lib/mysql \
      --health-cmd="mysqladmin ping -h localhost" \
      --health-interval=10s \
      --health-timeout=5s \
      --health-retries=5 \
      ${var.IMAGE_URL}db:${var.SPRING_APP_VERSION}
    EOT
  }
  depends_on = [
    null_resource.docker_images,
    null_resource.create_app_network,
    docker_volume.db_data
  ]
}

resource "null_resource" "backend_container" {
  triggers = {
    image = "${var.IMAGE_URL}backend:${var.SPRING_APP_VERSION}"
  }

  provisioner "local-exec" {
    command = <<-EOT
    docker run -d \
      --name backend \
      --network app_network \
      -p 8080:8080 \
      -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/Expendi?useSSL=false"&"allowPublicKeyRetrieval=true"&"serverTimezone=UTC \
      -e SPRING_DATASOURCE_USERNAME=sa \
      -e SPRING_DATASOURCE_PASSWORD=password \
      ${var.IMAGE_URL}backend:${var.SPRING_APP_VERSION}
    EOT
  }
  depends_on = [
    null_resource.docker_images,
    null_resource.create_app_network,
    null_resource.db_container
  ]
}


resource "null_resource" "frontend_container" {
  triggers = {
    image = "${var.IMAGE_URL}frontend:${var.SPRING_APP_VERSION}"
  }

  provisioner "local-exec" {
    command = <<-EOT
    docker run -d \
      --name frontend \
      --network app_network \
      -p 3000:3000 \
      ${var.IMAGE_URL}frontend:${var.SPRING_APP_VERSION}
    EOT
  }
  depends_on = [
    null_resource.docker_images,
    null_resource.create_app_network,
    null_resource.db_container,
    null_resource.backend_container
  ]
}


resource "null_resource" "haproxy_container" {
  triggers = {
    image = "jack.hc-sc.gc.ca/base/haproxy:5.0.118-http"
  }

  provisioner "local-exec" {
    command = <<-EOT
    docker network connect app_network haproxy
    docker start haproxy
    EOT
  }
  depends_on = [
    null_resource.docker_images,
    null_resource.create_app_network,
    null_resource.db_container,
    null_resource.backend_container,
    null_resource.frontend_container
  ]
}




