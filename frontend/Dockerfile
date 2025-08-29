# syntax=docker/dockerfile:1

### Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

### Run stage (Nginx)
FROM nginx:1.27-alpine
# 靜態檔放入預設根目錄
COPY --from=build /app/dist /usr/share/nginx/html
# 用 Nginx proxy 將 /api 轉給後端（compose 內的 backend:8080）
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx","-g","daemon off;"]
