FROM node:24-alpine AS build
WORKDIR /build
COPY . .
RUN npm ci --no-audit --no-fund
RUN npm test
RUN npx ng build --configuration production

FROM nginx:alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/paperless-ui/browser /usr/share/nginx/html
EXPOSE 80
