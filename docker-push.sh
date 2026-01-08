set -e
set -x

docker buildx create --use --name multi || true
docker buildx inspect --bootstrap

docker buildx build \
  --no-cache \
  --platform=linux/amd64,linux/arm64 \
  -t ghcr.io/konstantin-matrokhin/uv-index-bot:latest \
  --push .
