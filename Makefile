COMPOSE = docker compose

.PHONY: up down reset-db logs test dev

up:
	$(COMPOSE) up --build -d --wait

down:
	$(COMPOSE) down --remove-orphans

reset-db:
	$(COMPOSE) down --volumes --remove-orphans

logs:
	$(COMPOSE) logs -f

test:
	docker build --target test --progress plain -t barstock-backend-tests ./backend
	docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal --add-host=host.docker.internal:host-gateway barstock-backend-tests

dev:
	$(COMPOSE) -f docker-compose.yml -f docker-compose.dev.yml up --build -d --wait
