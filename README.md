# Command I used for testing:
```bash
    kotlinc parser.kt -include-runtime -d parser.jar && java -ea -jar parser.jar
```

# Info
This is a simple prototype I created for parsing and generating a SIM file using Kotlin data classes. To integrate it into the main library, everything but the test cases (optional) just needs to be copied over.