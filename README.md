# Boostr Portal

## Start a development instance
1. Install Docker
2. Make sure Docker is running
3. Run the following command:
```bash
./gradlew clean runServer
```

The gradle `runServer` task has two flags that can be set:
- `--reuseContainer` will keep the database from the previous run
- `--suspend` will wait for a debug connection (on port 9095) as soon as the application server starts

