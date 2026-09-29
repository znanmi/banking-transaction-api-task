# Banking Transactions API

A REST API for a simple bank, built with Java 21 and Spring Boot. It supports three operations:

1. Create an account with an initial balance
2. Transfer money between two accounts
3. View an account's transaction history

All data is stored in memory, so it resets when the application restarts.

---

## Tech stack

| Tool              | Version                      | Purpose                                 |
| ----------------- | ---------------------------- | --------------------------------------- |
| Java              | 21                           | Language                                |
| Spring Boot       | 4.1.1                        | Web framework (Spring Web + Validation) |
| Maven             | via included wrapper         | Build and dependency management         |
| JUnit 5 + AssertJ | via Spring Boot test starter | Unit tests                              |

---

## Getting started

### Prerequisites

- **Java 21** or later. Check with `java -version`.
- No Maven installation needed. The project includes the Maven wrapper (`mvnw`).

### Run the application

```bash
git clone https://github.com/znanmi/banking-transaction-api-task.git
cd banking-transaction-api-task
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.

The API starts at `http://localhost:8080`.

### Run the tests

```bash
./mvnw test
```

### Build a runnable JAR (optional)

```bash
./mvnw clean package
java -jar target/banking-transactions-api-0.0.1-SNAPSHOT.jar
```

---

## API endpoints

| Method | Path                                     | Description                          | Success       |
| ------ | ---------------------------------------- | ------------------------------------ | ------------- |
| `POST` | `/accounts`                              | Create an account                    | `201 Created` |
| `GET`  | `/accounts/{accountNumber}`              | Get an account's details and balance | `200 OK`      |
| `POST` | `/transfers`                             | Transfer money between two accounts  | `201 Created` |
| `GET`  | `/accounts/{accountNumber}/transactions` | Get an account's transaction history | `200 OK`      |

### Create an account

`POST /accounts`

```json
{
  "accountHolderName": "Alice",
  "initialBalance": 100.0
}
```

Response `201 Created`:

```json
{
  "accountNumber": "ACCNUM0001",
  "accountHolderName": "Alice",
  "balance": 100.0
}
```

### Get an account

`GET /accounts/ACCNUM0001`

Response `200 OK`:

```json
{
  "accountNumber": "ACCNUM0001",
  "accountHolderName": "Alice",
  "balance": 100.0
}
```

### Transfer money

`POST /transfers`

```json
{
  "fromAccountNumber": "ACCNUM0001",
  "toAccountNumber": "ACCNUM0002",
  "amount": 30.0
}
```

Response `201 Created`:

```json
{
  "transferReference": "3f1c9a2e-7b4d-4e8a-9c1f-2d6b8e5a4c3b",
  "fromAccountNumber": "ACCNUM0001",
  "toAccountNumber": "ACCNUM0002",
  "amount": 30.0,
  "transferredAt": "2026-09-28T18:30:00.123Z"
}
```

### Get transaction history

`GET /accounts/ACCNUM0001/transactions`

Response `200 OK`:

```json
[
  {
    "transferReference": null,
    "type": "CREDIT",
    "amount": 100.0,
    "balanceAfter": 100.0,
    "counterpartyAccountNumber": null,
    "timestamp": "2026-09-28T18:29:00.456Z"
  },
  {
    "transferReference": "3f1c9a2e-7b4d-4e8a-9c1f-2d6b8e5a4c3b",
    "type": "DEBIT",
    "amount": 30.0,
    "balanceAfter": 70.0,
    "counterpartyAccountNumber": "ACCNUM0002",
    "timestamp": "2026-09-28T18:30:00.123Z"
  }
]
```

The first entry is the opening deposit, so it has no transfer reference or counterparty.

---

## Error handling

All errors return the same JSON shape:

```json
{
  "status": 422,
  "error": "Unprocessable Content",
  "message": "Insufficient funds in account ACCNUM0001",
  "fieldErrors": null,
  "timestamp": "2026-09-28T18:31:00.789Z"
}
```

| Situation                                                                                                                                            | Status                                                                  |
| ---------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| Invalid input (blank name or account number, missing amount, negative initial balance, zero or negative transfer amount, more than 2 decimal places) | `400 Bad Request`, with `fieldErrors` listing each invalid field        |
| Malformed JSON or wrong data type (for example `"amount": "fifty"`)                                                                                  | `400 Bad Request`                                                       |
| Transfer to the same account                                                                                                                         | `400 Bad Request`                                                       |
| Account not found                                                                                                                                    | `404 Not Found`                                                         |
| Insufficient funds                                                                                                                                   | `422 Unprocessable Content`                                             |
| Wrong HTTP method on an existing path                                                                                                                | `405 Method Not Allowed`                                                |
| Any unexpected error                                                                                                                                 | `500 Internal Server Error` (logged, with no internal details returned) |

---

## Design

### Architecture

The code is organized into three layers:

```
Controller  ->  Service  ->  Repository
(HTTP)          (rules)      (storage)
```

- **Controllers** receive requests, trigger validation with `@Valid`, and return responses. They contain no business logic.
- **Services** hold the business rules: checking funds, preventing same-account transfers, and recording the ledger.
- **Repositories** store and retrieve data. Each has an interface and an in-memory implementation, so storage can be swapped for a database without changing the services.

Dependencies are provided through **constructor injection**.

```
src/main/java/io/github/znanmi/banking/
├── controller/   REST endpoints
├── dto/          Request and response objects (Java records)
├── exception/    Custom exceptions and the global error handler
├── model/        Domain objects: Account, TransactionEntry, TransactionType
├── repository/   Repository interfaces and in-memory implementations
└── service/      Business logic
```

### Key decisions

**DTOs at the API boundary.** Requests and responses use DTOs (Java records). The service converts them to domain objects, and repositories store domain objects. This keeps the API contract separate from internal data, so either can change without breaking the other. It also follows the assignment's guideline of Spring Data JPA style repositories, which store domain entities.

**The account protects its own balance.** `Account` has no setter for its balance. It can only change through `debit()` and `credit()`, which validate the amount and check for sufficient funds.

**Double-entry ledger.** Each transfer creates two linked entries: a `DEBIT` on the sender and a `CREDIT` on the receiver, sharing one `transferReference`. Each entry records the balance after the change. Opening an account records the initial balance as a `CREDIT`. As a result, an account's entries always add up to its balance, and the total money across all accounts never changes through transfers.

**Debit before credit.** The debit is the only step that can fail (insufficient funds), so it runs first. If it fails, nothing has changed, which keeps each transfer all-or-nothing.

**Concurrency.** Several requests can run at the same time. To stop two transfers from reading the same balance and overdrawing an account, a transfer locks both accounts until it finishes. Locks are always taken in the same order (by account number) to prevent deadlocks, where two transfers each wait for the other forever. Storage uses `ConcurrentHashMap` and `AtomicLong`, and the transaction repository's methods are synchronized.

**Errors are separated from HTTP.** Services throw plain business exceptions such as `InsufficientFundsException`. A single `@RestControllerAdvice` class maps each one to the correct status code and a consistent response body.

### Maintainability and reusability

- **Swappable storage.** Services depend on repository interfaces, not the in-memory classes. Moving to a database means adding new implementations, with no changes to business logic.
- **One place for each concern.** Controllers handle HTTP, services hold business rules, and repositories handle storage. Each change has an obvious home.
- **Centralized error handling.** One `@RestControllerAdvice` class turns exceptions into responses, so every endpoint returns errors in the same format, and new endpoints get this for free.
- **Messages defined once.** Each custom exception builds its own message, so the text lives in one place no matter where the exception is thrown.
- **Reusable business operations.** `Account.debit()` and `Account.credit()` hold the balance rules in one place. New features such as deposits or withdrawals can reuse them without duplicating checks.
- **Loose coupling through constructor injection.** Dependencies are passed in, which makes classes easy to test in isolation. The unit tests create services without starting Spring.
- **Tests as a safety net.** Unit tests cover the core rules, so future changes can be made with confidence.

### Data types

- `BigDecimal` for money, to avoid the rounding errors of `double`
- `Instant` for timestamps, always in UTC
- Account numbers are strings generated by the server (`ACCNUM0001`, `ACCNUM0002`, ...)

### Alternatives considered

- **H2 database instead of in-memory maps:** the brief asked for in-memory storage, and plain maps kept the focus on business logic rather than database setup.
- **`ReentrantLock` instead of `synchronized`:** `synchronized` is simpler and releases the lock automatically. `ReentrantLock` would be the choice if transfers needed a timeout instead of waiting.
- **A single transaction record instead of double-entry:** one record per transfer is simpler, but each account's history would have to be filtered from shared records. Two linked entries give each account its own complete history.
- **`double` instead of `BigDecimal`:** rejected, because floating-point rounding errors are unacceptable for money.

---

## Assumptions

- A single bank and a single currency.
- No authentication or authorization. Any caller can view any account, transfer money out of it, and read its transaction history.
- Account numbers are generated by the server, not supplied by the client.
- Account holder names do not need to be unique. Accounts cannot be updated or closed.
- An account can be opened with an initial balance of `0`.
- After opening, transfers are the only way to change a balance. Standalone deposits and withdrawals are out of scope.
- Transfers happen only between accounts in this system, with no external or interbank transfers.
- Transfers complete immediately. There are no pending or scheduled transfers, transfer limits, or fees.
- No overdrafts: a balance can never go below `0`.
- Amounts must be positive and have at most 2 decimal places.
- Transaction history is returned oldest first, without pagination.
- Data is in memory only and is lost when the application restarts.

---

## Testing

Unit tests cover the business logic in the service layer, using the real in-memory repositories:

- **Account creation:** the balance is set, and the opening deposit appears in the history.
- **Transfers:** money moves correctly, and linked debit and credit entries are recorded.
- **Boundaries:** transferring the exact balance succeeds, leaving zero. Transferring one cent more fails.
- **Failures:** insufficient funds, same-account transfers, and unknown accounts are rejected, and no balances or history are changed.

Input validation and error responses were tested manually with Postman.

---

## Limitations and future improvements

### Production readiness (in priority order)

1. **Security:** add authentication, so users can only access their own accounts.
2. **Persistence:** replace the in-memory repositories with a database (for example, Spring Data JPA with PostgreSQL). The repository interfaces allow this without changing the services.
3. **Multiple instances:** the current locks only work within one running application. With a database, transfers would use database transactions with row-level locking.
4. **Idempotent transfers:** accept a client-supplied idempotency key, so a retried request cannot send the same transfer twice.
5. **Consistent reads:** balance and history reads are not locked, so a read made during a transfer could briefly see one side of it.
6. **Controller tests:** add MockMvc tests to automate the validation and error-response checks currently done in Postman.
7. **Operations:** add structured logging, monitoring, and a Dockerfile.

### Maintainability

- **Mapper classes:** move DTO conversion out of the services into dedicated mappers, so the same conversion can be reused across services.
- **Centralized validation messages:** move validation messages into a `ValidationMessages.properties` file, so each message is defined once and can be translated later.
- **API versioning and docs:** prefix endpoints with `/api/v1` and generate OpenAPI (Swagger) documentation.

### Feature extensions

- **Deposits and withdrawals:** add endpoints that reuse `Account.credit()` and `Account.debit()`, each recording a single ledger entry.
- **Transaction verification:** require a PIN to confirm each transfer, stored as a BCrypt hash, with a limit on failed attempts.
- **Multiple currencies:** store a currency on each account, keep amount and currency together in a `Money` value object, and reject or convert cross-currency transfers.
- **Pagination:** page through long transaction histories.
