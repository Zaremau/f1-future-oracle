# Этап 1: Сборка React-приложения
FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Этап 2: Раздача через Nginx
FROM nginx:alpine
# Копируем собранную статику из папки dist в стандартную папку Nginx
COPY --from=build /app/dist /usr/share/nginx/html
# Копируем наш конфиг Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]