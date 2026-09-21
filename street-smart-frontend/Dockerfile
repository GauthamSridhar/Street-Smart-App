FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
FROM caddy:2-alpine
COPY --from=build /app/dist/street-smart/browser /srv
COPY docker-entrypoint.sh /usr/local/bin/street-smart-entrypoint
RUN chmod +x /usr/local/bin/street-smart-entrypoint
ENTRYPOINT ["/usr/local/bin/street-smart-entrypoint"]
CMD ["caddy", "run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
