# Using the official Selenium Standalone Chrome image as the base image
FROM selenium/standalone-chrome:latest

# Setting environment variables for Selenium Standalone Chrome
ENV SE_NODE_OVERRIDE_MAX_SESSIONS=true \
    SE_NODE_MAX_SESSIONS=5

# # Setting healthcheck for Selenium Service
# HEALTHCHECK --interval=10s --timeout=10s --retries=5 \
#     CMD curl -f http://localhost:4444/wd/hub/status || exit 1
