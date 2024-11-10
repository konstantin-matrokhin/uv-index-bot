set -e
set -x

docker buildx build --no-cache --platform=linux/amd64 -t ghcr.io/konstantin-matrokhin/uv-index-bot:latest --push .
