#!/bin/sh
set -eu

# DevTools reinicia Spring cuando cambian las clases compiladas.
mvn -B -ntp -DskipTests compile

# El polling también detecta cambios en bind mounts de Docker Desktop/Windows,
# donde los eventos inotify del host no siempre llegan al contenedor.
source_checksum() {
    find /app/src -type f -print0 | sort -z | xargs -0 -r sha256sum | sha256sum
}

(
    previous=$(source_checksum)
    while sleep 2; do
        current=$(source_checksum)
        if [ "$current" != "$previous" ]; then
            mvn -B -ntp -DskipTests compile || true
            previous=$current
        fi
    done
) &
watcher=$!
mvn -B -ntp spring-boot:run &
application=$!
trap 'kill "$watcher" "$application" 2>/dev/null || true' EXIT
trap 'exit 0' INT TERM
wait "$application"
