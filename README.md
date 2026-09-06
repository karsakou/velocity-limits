# velocity-limits

Accepts or declines fund loads against per-customer limits. Reads one JSON load per line,
writes one decision per line.

## Run

    ./gradlew bootJar
    java -jar build/libs/velocity-limits.jar input.txt                # decisions to stdout
    java -jar build/libs/velocity-limits.jar input.txt --output=out.txt

Needs JDK 21. Decisions go to stdout; logs go to stderr.
Exit code: 0 ok, 1 if some line was invalid, 2 for wrong args, 3 for an I/O error, 4 if the run stopped early.

## Test

    ./gradlew test

`GoldenFileTest` runs the app over `input.txt` and compares with `output.txt`. The rest are unit tests
for the rules, the day/week logic, the parser and the DB query.

## Limits

- $5,000 per day, $20,000 per week, 3 loads per day. Set in `application.yaml`.
- The limit itself is allowed; one cent over is declined.
- A day is UTC; a week starts Monday.

## Notes

- A load id is unique per customer. A repeat produces no output line (it is ignored).
- Only accepted loads count toward the limits. The expected output only matches this way.
- A bad line is skipped and logged and the run goes on. Anything unexpected stops it with exit code 4.
- H2 in-memory, schema by Liquibase. Each attempt is stored (declined ones with the rules they broke),
  so dedup and totals come from the database. Should work on Postgres too, but only H2 is tested.

## If this were production

- Add an HTTP endpoint over the same service.
- The runner is single-threaded. A concurrent service would lock per customer (e.g. a Postgres advisory
  lock, or loc on aggregate row like customer) or use `SERIALIZABLE` with retry. The unique key on (customer_id, load_id) already helps.
