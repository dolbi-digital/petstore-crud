# Rest Assured + JUnit5 + Allure + JSON Schema - Petstore CRUD

## Run
```bash
mvn -q test
mvn -q -Dtest=PetApiCrudTests -DPET_ID=333 test
```
Allure report:
```bash
allure serve target/allure-results
```
