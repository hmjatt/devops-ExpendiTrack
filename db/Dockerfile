# Using the official MySQL image as the base image
FROM mysql:8.0

# Setting environment variables for MySQL
ENV MYSQL_DATABASE=Expendi \
    MYSQL_USER=sa \
    MYSQL_PASSWORD=password \
    MYSQL_ROOT_PASSWORD=password

# # Setting healthcheck for MySQL service
# HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
#     CMD mysqladmin ping -h localhost
