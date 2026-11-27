#!/usr/bin/env bash
# Runs on the VPS: the CI's deploy job (.github/workflows/ci.yml) pipes it through ssh once it has uploaded the image's
# context to ~/frakkin-toasters/context and the settings to ~/frakkin-toasters/app.env.next.
#   bash deploy.sh <tag> <port>
# A new container that doesn't answer is replaced by the previous image again, and the deploy fails. The settings become
# app.env once the app answers with them, so a rollback starts the previous image with the settings it ran with: a broken
# setting is rolled back too.
set -euo pipefail

tag=$1
port=$2
name=frakkin-toasters
dir="$HOME/frakkin-toasters"
image="$name:$tag"

command -v curl >/dev/null || { echo "The health check needs curl on the VPS" >&2; exit 1; }

# --pull: each deploy brings the JRE's patches
docker build --pull --tag "$image" "$dir/context"
rm -rf "$dir/context"

previous=$(docker inspect --format '{{.Config.Image}}' "$name" 2>/dev/null || true)

start() {
  docker rm --force "$name" >/dev/null 2>&1 || true
  # The host's network: the VPS's Postgres is at localhost, natively or in a container that publishes its port, with
  # no pg_hba.conf entry for Docker's bridge network.
  docker run --detach --name "$name" \
    --restart unless-stopped \
    --network host \
    --env-file "$2" \
    --log-opt max-size=10m --log-opt max-file=5 \
    "$1" >/dev/null
}

# Flyway migrates the database before the app answers.
answers() {
  for _ in $(seq 90); do
    if curl --fail --silent --output /dev/null "http://127.0.0.1:$port/api/login/providers"; then
      return 0
    fi
    sleep 2
  done
  return 1
}

start "$image" "$dir/app.env.next"
if answers; then
  mv "$dir/app.env.next" "$dir/app.env"
  echo "$image answers on port $port"
  # The previous image stays for the next deploy's rollback, picked by name: images of the same jar share an id and a
  # creation time, so their order is no guide.
  docker images "$name" --format '{{.Repository}}:{{.Tag}}' | grep -v -x -F -e "$image" -e "${previous:-$image}" \
    | xargs -r docker rmi >/dev/null 2>&1 || true
  exit 0
fi

echo "$image doesn't answer on port $port after 3 minutes. Its log:" >&2
docker logs --tail 200 "$name" >&2 || true
if [ -n "$previous" ] && [ "$previous" != "$image" ] && [ -f "$dir/app.env" ]; then
  echo "Starting $previous again, with the settings it ran with" >&2
  start "$previous" "$dir/app.env"
  if answers; then echo "$previous answers again" >&2; else echo "$previous doesn't answer either" >&2; fi
fi
exit 1
