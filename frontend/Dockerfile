# Stage 1: Build the React/Vite app
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci

# Copy the rest of the frontend source code
COPY . .

# Build-time environment variables for Vite SPA
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ARG VITE_APP_NAME=CampusGuide
ENV VITE_APP_NAME=$VITE_APP_NAME
ARG VITE_APP_VERSION=1.0.0-MVP
ENV VITE_APP_VERSION=$VITE_APP_VERSION

RUN npm run build

# Stage 2: Serve the app with Nginx
FROM nginx:1.25-alpine

# Remove default static pages
RUN rm -rf /usr/share/nginx/html/*

# Copy build output to Nginx HTML path
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose Nginx default HTTP port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
