# tracerboy

tracerboy is an experimental event tracking API, built as an [Assigment](./ASSIGMENT.md) at Narrative.

## Getting started

Since this application is not yet published as a Docker image or some other wrapping I'm making an assumption that
eager engineers will have basic tooling installed on their machines. Tooling being - [SBT](https://www.scala-sbt.org/)
and [Docker](https://www.docker.com/). Developer experience can be further extended by usage
of [Nix](https://nixos.org/) it is however not mandatory requirement.

This project uses [Postgres][pg] with [TimescaleDB][timescale] extension to store all analytical events
and persistent queries. The application is bundled with embedded [Flyway][flyway] that will automatically detect the
state of database and execute appropriate migrations when booted or reloaded.

### Pre-requirements

Make sure that application can access Postgres instance or use a Docker Compose wrapper
script [`./bin/tracerboy-dev.sh`](./bin/tracerboy-dev.sh) to boot it up. This will create new Docker container with
Postgres and TimescaleDB extension installed and configured. It will also set username to `tb`, password to `tb` and
initialise new empty database with name `tb`.

```bash
./bin/tracerboy-dev.sh up pg
```

### 🏃‍♂️ Booting

Boot-up the application with `sbt run` and `DATABASE_URL` environment variable preconfigured.

```bash
# export DATABASE_URL="jdbc:postgresql://localhost:5432/tb?user=tb&password=tb"
sbt run
```

Or build Docker image and boot-up the application within the help
of [`Docker composition`](./.docker/docker-compose.yml):

```bash
sbt docker:publishLocal
./bin/tracerboy-dev.sh up tracerboy tracerboy-gw
```

If you wish to scale up or down number of replicas up use something along the following lines:

```bash
./bin/tracerboy-dev.sh up -d
./bin/tracerboy-dev.sh up --scale tracerboy=3 -d

./bin/tracerboy-dev.sh stop # To stop everything
```

When interacting with the service via Docker, please use port `4000` oppose to port `9090` when running natively on the
host operating system.

```bash
curl -D - --request POST \
  127.0.0.1:4000/analytics\?timestamp=1662981405\&user=Oto+Brglez\&event=click
```

## Development

If you wish to run with reloading in development mode then please consider using `sbt "~reStart"`.

The test suite that is bundled with the application and can be run with the help of `sbt test`.

## Discussion and notes

- Although the assigment identifies "user" with "username" it would be wiser to use proper UUIDs in production setup.
- Since the application is "stateless" there is no common shared state among running instances, thus making the
  application easy to scale.
- `timestamp` query parameter in the `POST /analytics` request should likely be hidden from the outside and set on the
  server when data is received and processed.

# Author

- [Oto Brglez](https://github.com/otobrglez)

[pg]: https://www.postgresql.org/

[timescale]: https://www.timescale.com/

[flyway]: https://flywaydb.org/
