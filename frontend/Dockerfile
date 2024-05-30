# Build stage: Use Node.js image to install dependencies and build the project
FROM node:20.11-bullseye-slim AS build
WORKDIR /app

# Copy project definition files and install dependencies
COPY package.json package-lock.json ./
RUN npm install

# Copy source code and build static files
COPY . .

# Run frontend tests
# RUN npm test

# Run npm
# RUN npm run start:docker

# Build frontend
RUN npm run build:docker

# Run the app
CMD ["npm", "start"]

# RUN serve -s build -l 3000

# # Production stage: Use Nginx to serve the static files
# FROM nginx:alpine

# # Copy the custom nginx.conf file to the container
# COPY .docker/nginx.conf /etc/nginx/nginx.conf

# # Copy static files from build stage to Nginx serve directory
# COPY --from=build /app/build /usr/share/nginx/html

# # Start Nginx in the foreground
# CMD ["nginx", "-g", "daemon off;"]
