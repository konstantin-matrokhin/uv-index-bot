docker buildx build --platform=linux/amd64 -t uv-index-bot .
docker tag uv-index-bot ghcr.io/konstantin-matrokhin/uv-index-bot:latest
docker push ghcr.io/konstantin-matrokhin/uv-index-bot
