# tracerboy

tracerboy is an experimental event tracking API, built as an [Assigment](./ASSIGMENT.md) at Narrative.

## Getting started

Since this application is not yet published as a Docker image or some other wrapping I'm making an assumption that
eager engineers will have basic tooling installed on their machines. Tooling being - [SBT](https://www.scala-sbt.org/)
and [Docker](https://www.docker.com/). Developer experience can be further extended by usage
of [Nix](https://nixos.org/) it is however not mandatory requirement.

This project uses [Postgres][pg] with [TimescaleDB][timescale] extension to store all analytical events
and [continuous aggregates](https://docs.timescale.com/timescaledb/latest/how-to-guides/continuous-aggregates/)
with [hypertables](https://docs.timescale.com/getting-started/latest/create-hypertable/). The application is bundled
with embedded [Flyway][flyway] that will automatically detect the state of database and execute appropriate migrations
when booted or reloaded.

### Pre-requirements

Make sure that application can access Postgres instance or use a Docker Compose wrapper
script [`./bin/tracerboy-dev.sh`](./bin/tracerboy-dev.sh) to boot it up. This will create new Docker container with
Postgres and TimescaleDB extension installed and configured. It will also set username to `tb`, password to `tb` and
initialise new empty database with name `tb`.

```bash
./bin/tracerboy-dev.sh up pg
```

### Booting 🏃‍♂️💨

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

## Development and testing 👷

If you wish to run with reloading in **development mode** then please consider
using [sbt-revolver](https://github.com/spray/sbt-revolver).

```bash
sbt "~service/reStart"
```

The **unit tests** suite that is bundled with the application and can be run with the help of sbt.

```bash
sbt test
```

The **integration tests** are packaged as separate module and can be invoked via usage of Gatling.

```bash
sbt integration/GatlingIt/test
```

> The Gatling traffic simulation will run against the service running on localhost:9090. If this project is to become
> more serious in the future I would likely suggest usage of [Testcontainers](https://www.testcontainers.org/) and
> re-usage of existing Docker Compose setup and configuration
> as [per-the-docs](https://www.testcontainers.org/modules/docker_compose/).

## Discussion and notes

- Although the assigment identifies "user" with "username" it would be wiser to use proper UUIDs in production setup.
- Since the application is "stateless" there is no common shared state among running instances, thus making the
  application easy to scale.
- `timestamp` query parameter in the `POST /analytics` request should likely be hidden from the outside and set on the
  server when data is received and processed.
- In production use-case the business logic handling could be improved with usage
  of [ZIO Prelude](https://github.com/zio/zio-prelude)
  / [Validation](https://zio.github.io/zio-prelude/docs/functionaldatatypes/validation) or
  Scala [Cats Validated](https://typelevel.org/cats/datatypes/validated.html)
- The analytical aggregation is implemented with the help of
  TimescaleDB's [hypertables](https://docs.timescale.com/getting-started/latest/create-hypertable/) and
  real-time [continuous aggregates](https://docs.timescale.com/timescaledb/latest/how-to-guides/continuous-aggregates/).
  In real-world scenario each of the aggregates would also need a proper retention policy.
- Why I've chosen TimescaleDB and not some other alternative is neatly captured and explained
  in [this article](https://docs.timescale.com/timescaledb/latest/overview/how-does-it-compare/timescaledb-vs-postgres/)
  .
  Other interesting options would also be [InfluxDB](https://www.influxdata.com/),
  [MongoDB (time-series)](https://www.mongodb.com/docs/manual/core/timeseries-collections/) or other specialised
  time-series databases.
- Additional work should be done in this application in terms of logging and monitoring if it is to be used in the wild.

# Author

- [Oto Brglez](https://github.com/otobrglez)

[pg]: https://www.postgresql.org/

[timescale]: https://www.timescale.com/

[flyway]: https://flywaydb.org/
